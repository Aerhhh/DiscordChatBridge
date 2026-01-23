package net.aerh.discordbridge.config;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import org.jetbrains.annotations.NotNull;

/**
 * Configuration for Discord bot connection settings.
 */
public final class DiscordConfig {

    public static final BuilderCodec<DiscordConfig> CODEC = BuilderCodec
            .builder(DiscordConfig.class, DiscordConfig::new)
            .append(new KeyedCodec<>("BotToken", Codec.STRING),
                    (cfg, value) -> cfg.botToken = value,
                    cfg -> cfg.botToken)
            .add()
            .append(new KeyedCodec<>("ChannelId", Codec.STRING),
                    (cfg, value) -> cfg.channelId = value,
                    cfg -> cfg.channelId)
            .add()
            .append(new KeyedCodec<>("PresenceMessage", Codec.STRING),
                    (cfg, value) -> cfg.presenceMessage = value,
                    cfg -> cfg.presenceMessage)
            .add()
            .append(new KeyedCodec<>("IgnoreBotMessages", Codec.BOOLEAN),
                    (cfg, value) -> cfg.ignoreBotMessages = value,
                    cfg -> cfg.ignoreBotMessages)
            .add()
            .append(new KeyedCodec<>("IgnoreWebhookMessages", Codec.BOOLEAN),
                    (cfg, value) -> cfg.ignoreWebhookMessages = value,
                    cfg -> cfg.ignoreWebhookMessages)
            .add()
            .append(new KeyedCodec<>("AllowMentions", Codec.BOOLEAN),
                    (cfg, value) -> cfg.allowMentions = value,
                    cfg -> cfg.allowMentions)
            .add()
            .append(new KeyedCodec<>("Locale", Codec.STRING),
                    (cfg, value) -> cfg.locale = value,
                    cfg -> cfg.locale)
            .add()
            .append(new KeyedCodec<>("WebhookUrl", Codec.STRING),
                    (cfg, value) -> cfg.webhookUrl = value,
                    cfg -> cfg.webhookUrl)
            .add()
            .append(new KeyedCodec<>("UseWebhookForChat", Codec.BOOLEAN),
                    (cfg, value) -> cfg.useWebhookForChat = value,
                    cfg -> cfg.useWebhookForChat)
            .add()
            .build();

    private static final String DEFAULT_PRESENCE = "Watching the server";

    private String botToken = "";
    private String channelId = "";
    private String presenceMessage = DEFAULT_PRESENCE;
    private boolean ignoreBotMessages = true;
    private boolean ignoreWebhookMessages = true;
    private boolean allowMentions = false;
    private String locale = "en-US";
    private String webhookUrl = "";
    private boolean useWebhookForChat = false;

    @NotNull
    public String getBotToken() {
        return botToken == null ? "" : botToken;
    }

    @NotNull
    public String getChannelId() {
        return channelId == null ? "" : channelId;
    }

    @NotNull
    public String getPresenceMessage() {
        return presenceMessage == null || presenceMessage.isBlank()
                ? DEFAULT_PRESENCE
                : presenceMessage;
    }

    public boolean isIgnoreBotMessages() {
        return ignoreBotMessages;
    }

    public boolean isIgnoreWebhookMessages() {
        return ignoreWebhookMessages;
    }

    public boolean isAllowMentions() {
        return allowMentions;
    }

    @NotNull
    public String getLocale() {
        return locale == null || locale.isBlank() ? "en-US" : locale;
    }

    @NotNull
    public String getWebhookUrl() {
        return webhookUrl == null ? "" : webhookUrl;
    }

    public boolean isUseWebhookForChat() {
        return useWebhookForChat;
    }

    /**
     * @return {@code true} if the bot can be started with these settings.
     */
    public boolean isValid() {
        return !getBotToken().isBlank() && !getChannelId().isBlank();
    }
}