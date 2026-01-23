package net.aerh.discordbridge.config;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;

/**
 * Root configuration holder for the Discord bridge plugin.
 */
public final class DiscordBridgeConfig {

    public static final BuilderCodec<DiscordBridgeConfig> CODEC = BuilderCodec
            .builder(DiscordBridgeConfig.class, DiscordBridgeConfig::new)
            .append(new KeyedCodec<>("Enabled", Codec.BOOLEAN),
                    (cfg, value) -> cfg.enabled = value,
                    cfg -> cfg.enabled)
            .add()
            .append(new KeyedCodec<>("RelayGameToDiscord", Codec.BOOLEAN),
                    (cfg, value) -> cfg.relayGameToDiscord = value,
                    cfg -> cfg.relayGameToDiscord)
            .add()
            .append(new KeyedCodec<>("RelayDiscordToGame", Codec.BOOLEAN),
                    (cfg, value) -> cfg.relayDiscordToGame = value,
                    cfg -> cfg.relayDiscordToGame)
            .add()
            .append(new KeyedCodec<>("Debug", Codec.BOOLEAN),
                    (cfg, value) -> cfg.debug = value,
                    cfg -> cfg.debug)
            .add()
            .append(new KeyedCodec<>("Discord", DiscordConfig.CODEC),
                    (cfg, value) -> cfg.discordConfig = value,
                    cfg -> cfg.discordConfig)
            .add()
            .append(new KeyedCodec<>("Events", EventsConfig.CODEC),
                    (cfg, value) -> cfg.eventsConfig = value,
                    cfg -> cfg.eventsConfig)
            .add()
            .append(new KeyedCodec<>("Messages", MessagesConfig.CODEC),
                    (cfg, value) -> cfg.messagesConfig = value,
                    cfg -> cfg.messagesConfig)
            .add()
            .build();

    private boolean enabled = true;
    private boolean relayGameToDiscord = true;
    private boolean relayDiscordToGame = true;
    private boolean debug = false;
    private DiscordConfig discordConfig = new DiscordConfig();
    private EventsConfig eventsConfig = new EventsConfig();
    private MessagesConfig messagesConfig = new MessagesConfig();

    public boolean isEnabled() {
        return enabled;
    }

    public boolean isRelayGameToDiscord() {
        return relayGameToDiscord;
    }

    public boolean isRelayDiscordToGame() {
        return relayDiscordToGame;
    }

    public boolean isDebug() {
        return debug;
    }

    public DiscordConfig getDiscordConfig() {
        return discordConfig;
    }

    public EventsConfig getEventsConfig() {
        return eventsConfig;
    }

    public MessagesConfig getMessagesConfig() {
        return messagesConfig;
    }

    /**
     * @return {@code true} if we have enough information to start the Discord bot.
     */
    public boolean canStartBot() {
        return isEnabled() && discordConfig.isValid();
    }
}