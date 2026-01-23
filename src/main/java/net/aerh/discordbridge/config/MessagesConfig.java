package net.aerh.discordbridge.config;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import org.jetbrains.annotations.NotNull;

/**
 * Configuration for message formatting between Discord and the game.
 */
public final class MessagesConfig {

    public static final BuilderCodec<MessagesConfig> CODEC = BuilderCodec
            .builder(MessagesConfig.class, MessagesConfig::new)
            .append(new KeyedCodec<>("DiscordLabel", Codec.STRING),
                    (cfg, value) -> cfg.discordLabel = value,
                    cfg -> cfg.discordLabel)
            .add()
            .append(new KeyedCodec<>("InboundTemplate", Codec.STRING),
                    (cfg, value) -> cfg.inboundTemplate = value,
                    cfg -> cfg.inboundTemplate)
            .add()
            .append(new KeyedCodec<>("OutboundTemplate", Codec.STRING),
                    (cfg, value) -> cfg.outboundTemplate = value,
                    cfg -> cfg.outboundTemplate)
            .add()
            .append(new KeyedCodec<>("LabelColor", Codec.STRING),
                    (cfg, value) -> cfg.labelColor = value,
                    cfg -> cfg.labelColor)
            .add()
            .append(new KeyedCodec<>("DefaultRoleColor", Codec.STRING),
                    (cfg, value) -> cfg.defaultRoleColor = value,
                    cfg -> cfg.defaultRoleColor)
            .add()
            .append(new KeyedCodec<>("ContentColor", Codec.STRING),
                    (cfg, value) -> cfg.contentColor = value,
                    cfg -> cfg.contentColor)
            .add()
            .append(new KeyedCodec<>("AvatarUrlFormat", Codec.STRING),
                    (cfg, value) -> cfg.avatarUrlFormat = value,
                    cfg -> cfg.avatarUrlFormat)
            .add()
            .build();

    private String discordLabel = "[Discord]";
    private String inboundTemplate = "%label% %role% %username%: %message%";
    private String outboundTemplate = "**%player%**: %message%";
    private String labelColor = "#5865F2";
    private String defaultRoleColor = "#99AAB5";
    private String contentColor = "#FFFFFF";
    private String avatarUrlFormat = "https://crafthead.net/hytale/cube/%s";

    @NotNull
    public String getDiscordLabel() {
        return discordLabel == null || discordLabel.isBlank() ? "[Discord]" : discordLabel;
    }

    @NotNull
    public String getInboundTemplate() {
        return inboundTemplate == null || inboundTemplate.isBlank()
                ? "%label% %role% %username%: %message%"
                : inboundTemplate;
    }

    @NotNull
    public String getOutboundTemplate() {
        return outboundTemplate == null || outboundTemplate.isBlank()
                ? "**%player%**: %message%"
                : outboundTemplate;
    }

    @NotNull
    public String getLabelColor() {
        return labelColor == null || labelColor.isBlank() ? "#5865F2" : labelColor;
    }

    @NotNull
    public String getDefaultRoleColor() {
        return defaultRoleColor == null || defaultRoleColor.isBlank() ? "#99AAB5" : defaultRoleColor;
    }

    @NotNull
    public String getContentColor() {
        return contentColor == null || contentColor.isBlank() ? "#FFFFFF" : contentColor;
    }

    @NotNull
    public String getAvatarUrlFormat() {
        return avatarUrlFormat == null || avatarUrlFormat.isBlank()
                ? "https://crafthead.net/hytale/cube/%s"
                : avatarUrlFormat;
    }
}