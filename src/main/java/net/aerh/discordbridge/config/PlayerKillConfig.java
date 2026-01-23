package net.aerh.discordbridge.config;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import org.jetbrains.annotations.NotNull;

/**
 * Configuration variants for player kill notifications.
 */
public final class PlayerKillConfig {

    public static final BuilderCodec<PlayerKillConfig> CODEC = BuilderCodec
            .builder(PlayerKillConfig.class, PlayerKillConfig::new)
            .append(new KeyedCodec<>("Enabled", Codec.BOOLEAN),
                    (cfg, value) -> cfg.enabled = value,
                    cfg -> cfg.enabled)
            .add()
            .append(new KeyedCodec<>("Message", Codec.STRING),
                    (cfg, value) -> cfg.message = value,
                    cfg -> cfg.message)
            .add()
            .append(new KeyedCodec<>("MessageWithItem", Codec.STRING),
                    (cfg, value) -> cfg.messageWithItem = value,
                    cfg -> cfg.messageWithItem)
            .add()
            .append(new KeyedCodec<>("MessageWithProjectile", Codec.STRING),
                    (cfg, value) -> cfg.messageWithProjectile = value,
                    cfg -> cfg.messageWithProjectile)
            .add()
            .append(new KeyedCodec<>("MessageWithProjectileUnknown", Codec.STRING),
                    (cfg, value) -> cfg.messageWithProjectileUnknown = value,
                    cfg -> cfg.messageWithProjectileUnknown)
            .add()
            .build();

    private boolean enabled = true;
    private String message = "";
    private String messageWithItem = "";
    private String messageWithProjectile = "";
    private String messageWithProjectileUnknown = "";

    public PlayerKillConfig() {
    }

    public PlayerKillConfig(
            boolean enabled,
            @NotNull String message,
            @NotNull String messageWithItem,
            @NotNull String messageWithProjectile,
            @NotNull String messageWithProjectileUnknown
    ) {
        this.enabled = enabled;
        this.message = message;
        this.messageWithItem = messageWithItem;
        this.messageWithProjectile = messageWithProjectile;
        this.messageWithProjectileUnknown = messageWithProjectileUnknown;
    }

    public boolean isEnabled() {
        return enabled;
    }

    @NotNull
    public String getMessage() {
        return message == null ? "" : message;
    }

    @NotNull
    public String getMessageWithItem() {
        return messageWithItem == null ? "" : messageWithItem;
    }

    @NotNull
    public String getMessageWithProjectile() {
        return messageWithProjectile == null ? "" : messageWithProjectile;
    }

    @NotNull
    public String getMessageWithProjectileUnknown() {
        return messageWithProjectileUnknown == null ? "" : messageWithProjectileUnknown;
    }
}