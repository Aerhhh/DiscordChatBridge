package net.aerh.discordbridge.config;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import org.jetbrains.annotations.NotNull;

/**
 * Per-event configuration for toggling and formatting notifications.
 */
public final class EventMessageConfig {

    public static final BuilderCodec<EventMessageConfig> CODEC = BuilderCodec
            .builder(EventMessageConfig.class, EventMessageConfig::new)
            .append(new KeyedCodec<>("Enabled", Codec.BOOLEAN),
                    (cfg, value) -> cfg.enabled = value,
                    cfg -> cfg.enabled)
            .add()
            .append(new KeyedCodec<>("Message", Codec.STRING),
                    (cfg, value) -> cfg.message = value,
                    cfg -> cfg.message)
            .add()
            .build();

    private boolean enabled = true;
    private String message = "";

    public EventMessageConfig() {
    }

    public EventMessageConfig(boolean enabled, @NotNull String message) {
        this.enabled = enabled;
        this.message = message;
    }

    public boolean isEnabled() {
        return enabled;
    }

    @NotNull
    public String getMessage() {
        return message == null ? "" : message;
    }
}