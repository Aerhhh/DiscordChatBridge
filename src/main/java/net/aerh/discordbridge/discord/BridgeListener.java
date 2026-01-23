package net.aerh.discordbridge.discord;

import com.hypixel.hytale.logger.HytaleLogger;
import net.aerh.discordbridge.config.DiscordBridgeConfig;
import net.aerh.discordbridge.config.DiscordConfig;
import net.aerh.discordbridge.discord.model.DiscordMessage;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.logging.Level;

final class BridgeListener extends ListenerAdapter {

    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    private final DiscordBridgeConfig config;
    private final CompletableFuture<Void> readyFuture;
    private final Consumer<DiscordMessage> relayToGameChat;
    private final Consumer<TextChannel> discordChannelUpdater;

    BridgeListener(
            @NotNull DiscordBridgeConfig config,
            @NotNull CompletableFuture<Void> readyFuture,
            @NotNull Consumer<DiscordMessage> relayToGameChat,
            @NotNull Consumer<TextChannel> discordChannelUpdater
    ) {
        this.config = config;
        this.readyFuture = readyFuture;
        this.relayToGameChat = relayToGameChat;
        this.discordChannelUpdater = discordChannelUpdater;
    }

    @Override
    public void onReady(@NotNull ReadyEvent event) {
        DiscordConfig discordConfig = config.getDiscordConfig();
        TextChannel channel = event.getJDA().getTextChannelById(discordConfig.getChannelId());
        if (channel == null) {
            IllegalStateException exception = new IllegalStateException(
                    "Unable to find text channel with id " + discordConfig.getChannelId());
            LOGGER.at(Level.SEVERE).withCause(exception).log("Discord bridge channel missing");
            readyFuture.completeExceptionally(exception);
            return;
        }

        discordChannelUpdater.accept(channel);
        LOGGER.at(Level.INFO).log("Discord bot connected as %s", event.getJDA().getSelfUser().getAsTag());
        readyFuture.complete(null);
    }

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        DiscordConfig discordConfig = config.getDiscordConfig();

        if (!config.isRelayDiscordToGame()
                || event.isFromType(ChannelType.PRIVATE)
                || !event.getChannel().getId().equals(discordConfig.getChannelId())) {
            return;
        }

        if (event.getAuthor().getIdLong() == event.getJDA().getSelfUser().getIdLong()) {
            return;
        }

        if (event.getAuthor().isBot() && discordConfig.isIgnoreBotMessages()) {
            return;
        }

        if (event.isWebhookMessage() && discordConfig.isIgnoreWebhookMessages()) {
            return;
        }

        List<String> attachments = new ArrayList<>();
        for (Message.Attachment attachment : event.getMessage().getAttachments()) {
            attachments.add(attachment.getUrl());
        }

        Member member = event.getMember();
        String displayName = member != null ? member.getEffectiveName() : event.getAuthor().getName();
        Role topRole = member != null && !member.getRoles().isEmpty() ? member.getRoles().getFirst() : null;
        String topRoleName = topRole != null ? topRole.getName() : null;
        Color roleColor = topRole != null ? topRole.getColors().getPrimary() : null;
        Color displayColor = member != null ? member.getColors().getPrimary() : null;

        DiscordMessage bridgeMessage = new DiscordMessage(
                displayName,
                event.getMessage().getContentDisplay(),
                attachments,
                topRoleName,
                roleColor,
                displayColor
        );

        relayToGameChat.accept(bridgeMessage);
    }
}
