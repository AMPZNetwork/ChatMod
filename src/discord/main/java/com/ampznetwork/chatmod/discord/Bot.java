package com.ampznetwork.chatmod.discord;

import club.minnced.discord.webhook.WebhookClient;
import club.minnced.discord.webhook.WebhookClientBuilder;
import club.minnced.discord.webhook.send.WebhookMessageBuilder;
import com.ampznetwork.chatmod.api.ChatModCompatibilityLayerAdapter;
import com.ampznetwork.chatmod.api.model.ChatMessage;
import com.ampznetwork.chatmod.api.model.ChatMessagePacket;
import com.ampznetwork.chatmod.api.model.CompatibilityLayer;
import com.ampznetwork.chatmod.core.compatibility.DefaultCompatibilityLayer;
import com.ampznetwork.chatmod.discord.model.Config;
import com.ampznetwork.libmod.api.entity.Player;
import com.ampznetwork.libmod.api.interop.game.PlayerIdentifierAdapter;
import com.ampznetwork.libmod.core.adapter.HeadlessPlayerAdapter;
import com.fasterxml.jackson.databind.ObjectMapper;
import emoji4j.EmojiUtils;
import lombok.SneakyThrows;
import lombok.Value;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.EventListener;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.TextColor;
import org.comroid.annotations.Alias;
import org.comroid.api.func.util.Command;
import org.comroid.api.func.util.Debug;
import org.comroid.api.io.FileHandle;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static net.kyori.adventure.text.Component.*;

@Value
public class Bot implements ChatModCompatibilityLayerAdapter, EventListener {
    public static final FileHandle DIR          = new FileHandle("/srv/chatmod", true);
    public static final FileHandle CONFIG       = DIR.createSubFile("config.json");
    public static final String     WEBHOOK_NAME = "Minecraft Chat Link";
    public static final String     SOURCE       = "discord";
    public static       Bot        INSTANCE;

    @SneakyThrows
    public static void main(String[] args) {
        try {
            var config = new ObjectMapper().readValue(CONFIG, Config.class);
            INSTANCE = new Bot(config);
        } catch (Throwable t) {
            t.printStackTrace();
            System.exit(1);
        }
    }

    Config                                config;
    JDA                                   jda;
    Command.Manager                       cmdr;
    PlayerIdentifierAdapter               playerAdapter             = HeadlessPlayerAdapter.INSTANCE;
    CompatibilityLayer<ChatMessagePacket> defaultCompatibilityLayer = new DefaultCompatibilityLayer(this);

    @SneakyThrows
    private Bot(Config config) {
        this.config = config;
        this.jda    = JDABuilder.createLight(config.getDiscordToken())
                .enableIntents(GatewayIntent.getIntents(GatewayIntent.ALL_INTENTS))
                .addEventListeners(this)
                .build()
                .awaitReady();
        this.cmdr   = new Command.Manager() {{
            register(Bot.this);
            this.new Adapter$JDA(jda);
        }};

        reload();
    }

    @Override
    public String getMainRabbitUri() {
        return config.getRabbitMqUri();
    }

    @Override
    public String getAurionChatRabbitUri() {
        return "none";
    }

    @Override
    public void relayInbound(ChatMessagePacket packet) {
        if (SOURCE.equals(packet.getSource()))
            return;
        var chatMessage = packet.getMessage();
        var sender      = chatMessage.getSender();
        var message = new WebhookMessageBuilder()
                .setContent(chatMessage.getMessageString())
                .setUsername(Optional.ofNullable(sender).map(Player::getName).orElseGet(chatMessage::getSenderName))
                .setAvatarUrl(Optional.ofNullable(sender).map(Player::getHeadUrl).orElse(null))
                .build();
        config.getChannels().stream()
                .filter(mapping -> Objects.equals(packet.getChannel(), mapping.getGameChannelName()))
                .map(mapping -> obtainWebhook(mapping, jda.getTextChannelById(mapping.getDiscordChannelId())))
                .forEach(webhook -> webhook.thenCompose(wh -> wh.send(message))
                        .exceptionally(Debug.exceptionLogger("Could not send Message using Webhook")));
    }

    @Override
    public void send(String channelName, ChatMessage message) {
        defaultCompatibilityLayer.send(new ChatMessagePacket(SOURCE, channelName, message));
    }

    @Command
    @Alias("reconnect")
    public void reload() {
        defaultCompatibilityLayer.reload();
    }

    @Override
    public void onEvent(@NotNull GenericEvent event) {
        if (event instanceof MessageReceivedEvent mre)
            relayOutbound(mre);
    }

    public void relayOutbound(MessageReceivedEvent event) {
        if (event.getAuthor().isBot())
            return;
        config.getChannels().stream()
                .filter(channel -> channel.getDiscordChannelId() == event.getChannel().getIdLong())
                .forEach(channel -> send(channel.getGameChannelName(), convertMessage(event, channel)));
    }

    private CompletableFuture<WebhookClient> obtainWebhook(Config.DiscordChannelMapping config, TextChannel channel) {
        //noinspection DataFlowIssue -> we want the exception here for .exceptionallyCompose()
        return CompletableFuture.supplyAsync(() -> WebhookClient.withUrl(config.getDiscordWebhookUrl()))
                .exceptionallyCompose(
                        ignored -> channel.retrieveWebhooks().submit()
                                .thenCompose(webhooks -> webhooks.stream()
                                        .filter(webhook -> WEBHOOK_NAME.equals(webhook.getName()))
                                        .findAny()
                                        .map(CompletableFuture::completedFuture)
                                        .orElseGet(() -> channel.createWebhook(WEBHOOK_NAME).submit()))
                                .thenApply(webhook -> WebhookClientBuilder.fromJDA(webhook).build()))
                .exceptionally(Debug.exceptionLogger("Internal Exception when obtaining Webhook"));
    }

    private ChatMessage convertMessage(MessageReceivedEvent event, Config.DiscordChannelMapping channel) {
        var discord   = text(/*'#' + event.getChannel().getName() + " : */"DISCORD ", TextColor.color(86, 98, 246));
        var inviteUrl = channel.getDiscordInviteUrl();
        if (inviteUrl != null)
            discord = discord.clickEvent(ClickEvent.openUrl(inviteUrl));
        var str = event.getMessage().getContentDisplay();
        var component = text()
                .append(discord)
                .append(text(EmojiUtils.removeAllEmojis(event.getAuthor().getEffectiveName()).trim(),
                        Optional.ofNullable(event.getMember())
                                .map(Member::getColorRaw)
                                .map(TextColor::color)
                                .orElse(TextColor.color(0xffffff))))
                .append(text(": " + str, TextColor.color(0xFF_FF_FF))
                        .clickEvent(ClickEvent.openUrl(event.getJumpUrl())));
        return new ChatMessage(null, event.getAuthor().getName(), str, event.getMessage().getContentRaw(), component.build());
    }
}
