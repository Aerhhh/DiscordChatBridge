package net.aerh.discordbridge;

import com.hypixel.hytale.event.EventPriority;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.event.events.BootEvent;
import com.hypixel.hytale.server.core.event.events.player.*;
import com.hypixel.hytale.server.core.modules.i18n.I18nModule;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.WorldMapTracker;
import com.hypixel.hytale.server.core.util.Config;
import net.aerh.discordbridge.config.*;
import net.aerh.discordbridge.discord.DiscordBotConnection;
import net.aerh.discordbridge.discord.MessageSanitizer;
import net.aerh.discordbridge.discord.PendingMessageHandler;
import net.aerh.discordbridge.discord.events.KillFeed;
import net.aerh.discordbridge.discord.events.KillFeedFormatter;
import net.aerh.discordbridge.discord.events.ZoneDiscovery;
import net.aerh.discordbridge.discord.model.DiscordMessage;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class DiscordBridgePlugin extends JavaPlugin {

    private static final Pattern INBOUND_PLACEHOLDER = Pattern.compile("%(label|role|username|message)%");
    private static final String DEFAULT_I18N_LANGUAGE = "en-US";

    private final Config<DiscordBridgeConfig> config = withConfig(DiscordBridgeConfig.CODEC);
    private final Map<UUID, String> playerWorlds = new ConcurrentHashMap<>();

    private DiscordBotConnection botConnection;
    private PendingMessageHandler startMessageHandler;

    public DiscordBridgePlugin(@NotNull JavaPluginInit init) {
        super(init);
    }

    @NotNull
    private static Message buildInboundMessage(
            @NotNull MessagesConfig msgConfig,
            @NotNull DiscordMessage discordMessage,
            @NotNull String content
    ) {
        String template = msgConfig.getInboundTemplate();
        Color contentColor = Color.decode(msgConfig.getContentColor());
        Color labelColor = Color.decode(msgConfig.getLabelColor());
        Color defaultRoleColor = Color.decode(msgConfig.getDefaultRoleColor());
        String discordLabel = msgConfig.getDiscordLabel();

        Message root = Message.empty();
        Matcher matcher = INBOUND_PLACEHOLDER.matcher(template);
        int last = 0;
        boolean hasMessageToken = false;

        while (matcher.find()) {
            if (matcher.start() > last) {
                appendTextSegment(root, template.substring(last, matcher.start()), contentColor);
            }

            switch (matcher.group(1)) {
                case "label" -> appendTextSegment(root, discordLabel, labelColor);
                case "role" -> appendRole(root, discordMessage, defaultRoleColor);
                case "username" -> appendUsername(root, discordMessage, defaultRoleColor);
                case "message" -> {
                    appendTextSegment(root, content, contentColor);
                    hasMessageToken = true;
                }
                default -> {
                }
            }

            last = matcher.end();
        }

        if (last < template.length()) {
            appendTextSegment(root, template.substring(last), contentColor);
        }

        if (!hasMessageToken) {
            appendTextSegment(root, content, contentColor);
        }

        return root;
    }

    private static void appendRole(@NotNull Message root, @NotNull DiscordMessage discordMessage, @NotNull Color defaultRoleColor) {
        if (discordMessage.topRoleName() == null || discordMessage.topRoleName().isBlank()) {
            return;
        }

        Color color = discordMessage.roleColor() != null ? discordMessage.roleColor() : defaultRoleColor;
        appendTextSegment(root, "[" + discordMessage.topRoleName() + "]", color);
    }

    @Override
    protected void shutdown() {
        getLogger().at(Level.INFO).log("Shutting down Discord bridge...");
        sendEventMessage(config.get().getEventsConfig().getServerStop());
        if (botConnection != null) {
            botConnection.shutdown();
            botConnection = null;
            getLogger().at(Level.INFO).log("Discord bot disconnected");
        }
    }

    private static void appendUsername(@NotNull Message root, @NotNull DiscordMessage discordMessage, @NotNull Color defaultRoleColor) {
        Color color = discordMessage.displayColor() != null ? discordMessage.displayColor() : defaultRoleColor;
        appendTextSegment(root, discordMessage.authorName(), color);
    }

    @Override
    protected void setup() {
        config.save();
        getLogger().at(Level.INFO).log("Configuration loaded successfully");

        getEventRegistry().registerGlobal(EventPriority.NORMAL, PlayerChatEvent.class, this::onPlayerChat);
        getEventRegistry().registerGlobal(BootEvent.class, this::onServerBoot);
        getEventRegistry().registerGlobal(PlayerConnectEvent.class, this::onPlayerConnect);
        getEventRegistry().registerGlobal(PlayerDisconnectEvent.class, this::onPlayerDisconnect);
        getEventRegistry().registerGlobal(AddPlayerToWorldEvent.class, this::onPlayerEnterWorld);
        getEventRegistry().registerGlobal(DrainPlayerFromWorldEvent.class, this::onPlayerLeaveWorld);
        DiscordBridgeConfig cfg = config.get();
        KillFeedFormatter killFeed = new KillFeedFormatter(
                cfg::getEventsConfig,
                this::sendEventMessage,
                () -> cfg.getDiscordConfig().getLocale(),
                cfg::isDebug
        );
        getEntityStoreRegistry().registerSystem(new KillFeed(killFeed, cfg::isDebug));
        getEntityStoreRegistry().registerSystem(new ZoneDiscovery(this::sendZoneDiscoveryMessage));
        getLogger().at(Level.INFO).log("Event listeners registered");

        if (!cfg.canStartBot()) {
            getLogger().at(Level.WARNING).log("Discord bridge disabled - missing bot token or channel id");
            return;
        }

        this.botConnection = new DiscordBotConnection(cfg, this::relayDiscordMessage);
        startBotConnection(cfg);
        getLogger().at(Level.INFO).log("Discord bot connection initialized");
    }

    @Override
    protected void start() {
        if (startMessageHandler != null) {
            startMessageHandler.reset();
        }
    }

    private void onPlayerChat(@NotNull PlayerChatEvent event) {
        DiscordBridgeConfig cfg = config.get();
        if (event.isCancelled()
                || botConnection == null
                || !cfg.isRelayGameToDiscord()) {
            return;
        }

        String cleaned = MessageSanitizer.sanitizeOutgoing(event.getContent(), cfg.getDiscordConfig().isAllowMentions());
        if (cleaned.isEmpty()) {
            return;
        }

        if (botConnection.hasWebhook()) {
            botConnection.sendWebhookMessage(
                    event.getSender().getUsername(),
                    event.getSender().getUuid(),
                    cleaned
            );
        } else {
            String payload = cfg.getMessagesConfig().getOutboundTemplate()
                    .replace("%player%", event.getSender().getUsername())
                    .replace("%message%", cleaned);
            sendToDiscord(payload, cfg);
        }
    }

    private void onPlayerConnect(@NotNull PlayerConnectEvent event) {
        DiscordBridgeConfig cfg = config.get();
        EventsConfig events = cfg.getEventsConfig();
        sendEventMessage(events.getPlayerJoin(), "%player%", event.getPlayerRef().getUsername());
    }

    private void onPlayerDisconnect(@NotNull PlayerDisconnectEvent event) {
        DiscordBridgeConfig cfg = config.get();
        EventsConfig events = cfg.getEventsConfig();
        PlayerRef playerRef = event.getPlayerRef();
        sendEventMessage(events.getPlayerLeave(), "%player%", playerRef.getUsername());
        String worldName = playerWorlds.remove(playerRef.getUuid());
        if (worldName != null) {
            sendEventMessage(events.getWorldLeave(), "%player%", playerRef.getUsername(), "%world%", worldName);
        }
    }

    private void onPlayerEnterWorld(@NotNull AddPlayerToWorldEvent event) {
        DiscordBridgeConfig cfg = config.get();
        EventsConfig events = cfg.getEventsConfig();

        PlayerRef playerRef = event.getHolder().getComponent(PlayerRef.getComponentType());
        if (playerRef == null) {
            return;
        }

        String worldName = resolveWorldName(event.getWorld());
        String previousWorld = playerWorlds.put(playerRef.getUuid(), worldName);
        if (previousWorld == null) {
            sendEventMessage(events.getWorldEnter(),
                    "%player%", playerRef.getUsername(),
                    "%world%", worldName);
            return;
        }

        if (!previousWorld.equals(worldName)) {
            sendEventMessage(events.getWorldChange(),
                    "%player%", playerRef.getUsername(),
                    "%from%", previousWorld,
                    "%to%", worldName);
        }
    }

    private void onPlayerLeaveWorld(@NotNull DrainPlayerFromWorldEvent event) {
        PlayerRef playerRef = event.getHolder().getComponent(PlayerRef.getComponentType());
        if (playerRef == null) {
            return;
        }

        World world = event.getWorld();
        if (world == null) {
            return;
        }

        String worldName = resolveWorldName(world);
        playerWorlds.put(playerRef.getUuid(), worldName);
    }

    private void onServerBoot(@NotNull BootEvent event) {
        if (startMessageHandler != null) {
            startMessageHandler.onConditionMet();
        }
    }

    private void relayDiscordMessage(@NotNull DiscordMessage message) {
        DiscordBridgeConfig cfg = config.get();
        if (!cfg.isRelayDiscordToGame()) {
            return;
        }

        Universe universe = Universe.get();
        if (universe == null) {
            return;
        }

        String content = MessageSanitizer.sanitizeIncoming(message);
        if (content.isEmpty()) {
            return;
        }

        Message formatted = buildInboundMessage(cfg.getMessagesConfig(), message, content);
        universe.sendMessage(formatted);
    }

    private static void appendTextSegment(@NotNull Message root, @NotNull String text, @NotNull Color color) {
        if (text.isEmpty()) {
            return;
        }

        root.insert(Message.raw(text).color(color));
    }

    private void sendToDiscord(@NotNull String message, @NotNull DiscordBridgeConfig cfg) {
        if (message.isBlank()) {
            return;
        }

        DiscordConfig discordConfig = cfg.getDiscordConfig();
        String finalMessage = discordConfig.isAllowMentions() ? message : MessageSanitizer.preventMentions(message);
        if (botConnection == null || !botConnection.isReady()) {
            return;
        }

        botConnection.sendMessage(finalMessage);
    }

    /**
     * Sends an event message to Discord if enabled.
     *
     * @param eventConfig  the event configuration
     * @param replacements pairs of placeholder and value (e.g., "%player%", "Steve")
     */
    private void sendEventMessage(@NotNull EventMessageConfig eventConfig, @NotNull String... replacements) {
        if (!eventConfig.isEnabled()) {
            return;
        }

        String message = eventConfig.getMessage();
        for (int i = 0; i < replacements.length - 1; i += 2) {
            message = message.replace(replacements[i], replacements[i + 1]);
        }

        sendToDiscord(message, config.get());
    }

    private void startBotConnection(@NotNull DiscordBridgeConfig cfg) {
        if (botConnection == null) {
            return;
        }

        this.startMessageHandler = new PendingMessageHandler(
                botConnection::isReady,
                () -> sendEventMessage(cfg.getEventsConfig().getServerStart())
        );

        getLogger().at(Level.INFO).log("Starting Discord bot connection...");
        this.botConnection.start()
                .thenRun(() -> {
                    getLogger().at(Level.INFO).log("Discord bot connected successfully to channel %s", cfg.getDiscordConfig().getChannelId());
                    startMessageHandler.onReady();
                })
                .exceptionally(throwable -> {
                    getLogger().at(Level.SEVERE)
                            .withCause(throwable)
                            .log("Failed to start Discord bridge bot");
                    return null;
                });
    }

    private void sendZoneDiscoveryMessage(@NotNull PlayerRef player, @NotNull WorldMapTracker.ZoneDiscoveryInfo info) {
        DiscordBridgeConfig cfg = config.get();
        EventMessageConfig zoneConfig = cfg.getEventsConfig().getZoneDiscovery();
        String locale = cfg.getDiscordConfig().getLocale();
        sendEventMessage(zoneConfig,
                "%player%", player.getUsername(),
                "%zone%", resolveZoneDisplayName(locale, info.zoneName(), true),
                "%region%", resolveZoneDisplayName(locale, info.regionName(), false));
    }

    private String resolveZoneDisplayName(@NotNull String locale, @NotNull String id, boolean isZone) {
        String key = (isZone ? "server.map.zone." : "server.map.region.") + id;
        String lang = locale.isBlank() ? DEFAULT_I18N_LANGUAGE : locale;
        String translated = I18nModule.get().getMessage(lang, key);

        return translated == null || translated.isBlank() ? id : translated;
    }

    @NotNull
    private String resolveWorldName(@NotNull World world) {
        String displayName = world.getWorldConfig().getDisplayName();
        if (displayName != null && !displayName.isBlank()) {
            return displayName;
        }

        return world.getName();
    }
}
