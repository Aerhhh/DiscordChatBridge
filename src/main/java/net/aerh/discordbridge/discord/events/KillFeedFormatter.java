package net.aerh.discordbridge.discord.events;

import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.FormattedMessage;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.asset.type.item.config.Item;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.inventory.Inventory;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.modules.entity.component.DisplayNameComponent;
import com.hypixel.hytale.server.core.modules.entity.damage.Damage;
import com.hypixel.hytale.server.core.modules.entity.damage.DamageCause;
import com.hypixel.hytale.server.core.modules.i18n.I18nModule;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.MessageUtil;
import net.aerh.discordbridge.config.EventMessageConfig;
import net.aerh.discordbridge.config.EventsConfig;
import net.aerh.discordbridge.config.PlayerKillConfig;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.function.Supplier;

public final class KillFeedFormatter {

    private static final String DEFAULT_LOCALE = "en-US";
    private static final String DEFAULT_ENTITY_NAME = "Unknown Entity";
    private final ComponentType<EntityStore, PlayerRef> playerRefComponent = PlayerRef.getComponentType();
    private final ComponentType<EntityStore, Player> playerComponent = Player.getComponentType();
    private final ComponentType<EntityStore, DisplayNameComponent> displayNameComponent = DisplayNameComponent.getComponentType();
    private final Supplier<EventsConfig> eventsSupplier;
    private final MessageSender messageSender;
    private final Supplier<String> localeSupplier;
    private final Supplier<Boolean> debugSupplier;

    public KillFeedFormatter(
            @NotNull Supplier<EventsConfig> eventsSupplier,
            @NotNull MessageSender messageSender,
            @NotNull Supplier<String> localeSupplier,
            @NotNull Supplier<Boolean> debugSupplier
    ) {
        this.eventsSupplier = eventsSupplier;
        this.messageSender = messageSender;
        this.localeSupplier = localeSupplier;
        this.debugSupplier = debugSupplier;
    }

    void dispatchDeathMessage(
            @NotNull Damage damage,
            @Nullable PlayerRef victimPlayer,
            @Nullable DisplayNameComponent victimDisplayName,
            @NotNull Store<EntityStore> store
    ) {
        EventsConfig events = eventsSupplier.get();
        String locale = resolveLocale();
        String victimName = resolveEntityName(victimPlayer, victimDisplayName, locale);
        if (victimName.isBlank()) {
            return;
        }

        PlayerRef killerPlayer = resolveKillerPlayer(damage, store);
        String killerName = killerPlayer != null ? killerPlayer.getUsername() : null;
        if (killerName == null && isDebugEnabled()) {
            killerName = resolveKillerName(damage, store, locale);
        }

        if (killerName != null) {
            PlayerKillConfig killConfig = events.getPlayerKill();
            String projectile = resolveProjectileName(damage, store, locale);
            String item = resolveKillerItemName(damage, store, locale);
            String cause = resolveDeathCause(damage, store, locale);
            boolean hasProjectile = projectile != null && !projectile.isBlank();
            boolean hasItem = item != null && !item.isBlank();
            boolean isProjectileSource = damage.getSource() instanceof Damage.ProjectileSource;

            String message;
            if (hasProjectile) {
                message = killConfig.getMessageWithProjectile();
            } else if (isProjectileSource) {
                message = killConfig.getMessageWithProjectileUnknown();
            } else if (hasItem) {
                message = killConfig.getMessageWithItem();
            } else {
                message = killConfig.getMessage();
            }

            EventMessageConfig config = new EventMessageConfig(killConfig.isEnabled(), message);
            messageSender.send(config,
                    "%killer%", killerName,
                    "%victim%", victimName,
                    "%player%", victimName,
                    "%cause%", cause,
                    "%projectile%", projectile == null ? "" : projectile,
                    "%item%", item == null ? "" : item
            );
        } else {
            EventMessageConfig config = events.getPlayerDeath();
            String cause = resolveDeathCause(damage, store, locale);
            messageSender.send(config,
                    "%player%", victimName,
                    "%cause%", cause
            );
        }
    }

    @Nullable
    private PlayerRef resolveKillerPlayer(@NotNull Damage damage, @NotNull Store<EntityStore> store) {
        if (damage.getSource() instanceof Damage.EntitySource entitySource) {
            if (entitySource.getRef().isValid()) {
                return store.getComponent(entitySource.getRef(), playerRefComponent);
            }
        }

        return null;
    }

    private String resolveLocale() {
        String locale = localeSupplier.get();
        return locale == null || locale.isBlank() ? DEFAULT_LOCALE : locale;
    }

    private boolean isDebugEnabled() {
        Boolean debug = debugSupplier.get();
        return debug != null && debug;
    }

    @NotNull
    private String resolveEntityName(
            @Nullable PlayerRef playerRef,
            @Nullable DisplayNameComponent displayNameComponent,
            @NotNull String locale
    ) {
        if (playerRef != null) {
            return playerRef.getUsername();
        }

        if (displayNameComponent != null) {
            Message displayName = displayNameComponent.getDisplayName();
            if (displayName != null) {
                return renderMessage(displayName, locale);
            }
        }

        return DEFAULT_ENTITY_NAME;
    }

    @Nullable
    private String resolveKillerName(@NotNull Damage damage, @NotNull Store<EntityStore> store, @NotNull String locale) {
        Damage.Source source = damage.getSource();
        if (source instanceof Damage.EntitySource entitySource) {
            DisplayNameComponent nameComponent = store.getComponent(entitySource.getRef(), this.displayNameComponent);
            if (nameComponent != null) {
                Message displayName = nameComponent.getDisplayName();
                if (displayName != null) {
                    return renderMessage(displayName, locale);
                }
            }
        }

        return null;
    }

    private String resolveKillerItemName(@NotNull Damage damage, @NotNull Store<EntityStore> store, @NotNull String locale) {
        Damage.Source source = damage.getSource();
        if (source instanceof Damage.EntitySource entitySource) {
            Player player = store.getComponent(entitySource.getRef(), playerComponent);
            if (player != null) {
                Inventory inventory = player.getInventory();
                ItemStack itemInHand = inventory != null ? inventory.getItemInHand() : null;
                String itemName = resolveItemName(itemInHand, locale);
                if (itemName != null && !itemName.isBlank()) {
                    return itemName;
                }
            }
        }

        return null;
    }

    @Nullable
    private String resolveItemName(@Nullable ItemStack itemStack, @NotNull String locale) {
        if (itemStack == null || itemStack.isEmpty()) {
            return null;
        }

        Item item = itemStack.getItem();
        String key = item.getTranslationKey();
        String translated = I18nModule.get().getMessage(locale, key);
        if (translated != null && !translated.isBlank()) {
            return translated;
        }

        return item.getId();
    }

    private String resolveDeathCause(@NotNull Damage damage, @NotNull Store<EntityStore> store, @NotNull String locale) {
        Damage.Source source = damage.getSource();
        if (source instanceof Damage.ProjectileSource projectileSource) {
            DisplayNameComponent displayNameComponent = store.getComponent(projectileSource.getProjectile(), DisplayNameComponent.getComponentType());
            if (displayNameComponent != null) {
                Message displayName = displayNameComponent.getDisplayName();
                if (displayName != null) {
                    return renderMessage(displayName, locale);
                }
            }
        }

        if (source instanceof Damage.EntitySource entitySource) {
            DisplayNameComponent displayNameComponent = store.getComponent(entitySource.getRef(), DisplayNameComponent.getComponentType());
            if (displayNameComponent != null) {
                Message displayName = displayNameComponent.getDisplayName();
                if (displayName != null) {
                    return renderMessage(displayName, locale);
                }
            }
        } else if (source instanceof Damage.EnvironmentSource environmentSource) {
            String type = environmentSource.getType();
            String translated = I18nModule.get().getMessage(locale, type);
            if (translated != null && !translated.isBlank()) {
                return translated;
            }

            return type;
        }

        DamageCause cause = damage.getCause();
        if (cause != null) {
            String key = "server.general.damageCauses." + cause.getId().toLowerCase(Locale.ROOT);
            String translated = I18nModule.get().getMessage(locale, key);
            if (translated != null && !translated.isBlank()) {
                return translated;
            }
        }

        return "unknown";
    }

    @Nullable
    private String resolveProjectileName(@NotNull Damage damage, @NotNull Store<EntityStore> store, @NotNull String locale) {
        Damage.Source source = damage.getSource();
        if (source instanceof Damage.ProjectileSource projectileSource) {
            DisplayNameComponent displayNameComponent = store.getComponent(projectileSource.getProjectile(), DisplayNameComponent.getComponentType());
            if (displayNameComponent != null) {
                Message displayName = displayNameComponent.getDisplayName();
                if (displayName != null) {
                    return renderMessage(displayName, locale);
                }
            }
        }

        return null;
    }

    private String renderMessage(@NotNull Message message, @NotNull String locale) {
        FormattedMessage formatted = message.getFormattedMessage();
        String text = formatted.rawText;
        if (text == null) {
            String messageId = formatted.messageId;
            if (messageId == null) {
                text = "";
            } else {
                text = I18nModule.get().getMessage(locale, messageId);
                if (text == null) {
                    text = messageId;
                }
            }
        }

        Map<String, FormattedMessage> resolvedMessageParams = null;
        if (formatted.messageParams != null && !formatted.messageParams.isEmpty()) {
            resolvedMessageParams = new HashMap<>();
            for (Map.Entry<String, FormattedMessage> entry : formatted.messageParams.entrySet()) {
                String resolved = renderMessage(new Message(entry.getValue()), locale);
                FormattedMessage replacement = new FormattedMessage();
                replacement.rawText = resolved;
                resolvedMessageParams.put(entry.getKey(), replacement);
            }
        }

        String result = MessageUtil.formatText(text, formatted.params, resolvedMessageParams);
        StringBuilder builder = new StringBuilder(result);
        for (Message child : message.getChildren()) {
            builder.append(renderMessage(child, locale));
        }

        return builder.toString();
    }

    @FunctionalInterface
    public interface MessageSender {
        void send(@NotNull EventMessageConfig eventConfig, @NotNull String... replacements);
    }
}