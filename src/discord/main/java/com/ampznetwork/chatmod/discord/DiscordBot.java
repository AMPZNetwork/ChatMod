package com.ampznetwork.chatmod.discord;

import club.minnced.discord.webhook.WebhookClient;
import club.minnced.discord.webhook.WebhookClientBuilder;
import club.minnced.discord.webhook.send.WebhookMessageBuilder;
import com.ampznetwork.chatmod.api.ChatModCompatibilityLayerAdapter;
import com.ampznetwork.chatmod.api.model.ChatMessage;
import com.ampznetwork.chatmod.api.model.ChatMessagePacket;
import com.ampznetwork.chatmod.api.model.CompatibilityLayer;
import com.ampznetwork.chatmod.core.compatibility.builtin.DefaultCompatibilityLayer;
import com.ampznetwork.chatmod.discord.model.Config;
import com.ampznetwork.libmod.api.entity.Player;
import com.ampznetwork.libmod.api.interop.game.PlayerIdentifierAdapter;
import com.ampznetwork.libmod.core.adapter.HeadlessPlayerAdapter;
import com.fasterxml.jackson.databind.ObjectMapper;
import emoji4j.EmojiUtils;
import lombok.EqualsAndHashCode;
import lombok.SneakyThrows;
import lombok.Value;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.EventListener;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.TextColor;
import org.comroid.annotations.Alias;
import org.comroid.api.func.util.Command;
import org.comroid.api.func.util.Debug;
import org.comroid.api.io.FileHandle;
import org.comroid.api.tree.Component;
import org.comroid.api.tree.UncheckedCloseable;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static net.kyori.adventure.text.Component.*;

@Value
@EqualsAndHashCode(of = "config")
public class DiscordBot extends Component.Base implements ChatModCompatibilityLayerAdapter, CompatibilityLayer<ChatMessagePacket> {
    public static final FileHandle DIR          = new FileHandle("/srv/chatmod", true);
    public static final FileHandle CONFIG       = DIR.createSubFile("config.json");
    public static final String     WEBHOOK_NAME = "Minecraft Chat Link";
    public static final String     SOURCE       = "discord";
    public static DiscordBot INSTANCE;

    @SneakyThrows
    public static void main(String[] args) {
        try {
            var config = new ObjectMapper().readValue(CONFIG, Config.class);
            INSTANCE = new DiscordBot(config);
        } catch (Throwable t) {
            t.printStackTrace();
            System.exit(1);
        }
    }

    Config                                config;
    PlayerIdentifierAdapter               playerAdapter;
    CompatibilityLayer<ChatMessagePacket> defaultCompatibilityLayer;
    JDA                                   jda;
    Command.Manager                       cmdr;

    public DiscordBot(Config config) {
        this(config, null, null);
    }

    @SneakyThrows
    public DiscordBot(Config config, @Nullable PlayerIdentifierAdapter playerAdapter, @Nullable CompatibilityLayer<ChatMessagePacket> baseLayer) {
        this.config        = config;
        this.playerAdapter = playerAdapter != null ? playerAdapter : HeadlessPlayerAdapter.INSTANCE;
        if (baseLayer != null)
            this.defaultCompatibilityLayer = baseLayer;
        else {
            this.defaultCompatibilityLayer = new DefaultCompatibilityLayer(this);
            addChildren(defaultCompatibilityLayer);
        }
        this.jda  = JDABuilder.createLight(config.getDiscordToken())
                .enableIntents(GatewayIntent.getIntents(GatewayIntent.ALL_INTENTS))
                .addEventListeners((EventListener) event -> {
                    if (event instanceof MessageReceivedEvent mre)
                        relayOutbound(mre);
                })
                .build()
                .awaitReady();
        this.cmdr = new Command.Manager() {{
            register(DiscordBot.this);
            this.new Adapter$JDA(jda);
            this.new Adapter$StdIO();
        }};
        addChildren((UncheckedCloseable) jda::shutdownNow, cmdr);

        reload();
    }

    @Override
    public String getSourceName() {
        return SOURCE;
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
        if (SOURCE.equals(packet.getSource())) return;
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

    @Override
    public ChatModCompatibilityLayerAdapter getMod() {
        return this;
    }

    @Override
    public ChatMessagePacket convertToChatModPacket(ChatMessagePacket packet) {
        return packet;
    }

    @Override
    public ChatMessagePacket convertToNativePacket(ChatMessagePacket packet) {
        return packet;
    }

    @Override
    public void doSend(ChatMessagePacket packet) {
        relayInbound(packet);
    }

    @Command
    @Alias("reconnect")
    public void reload() {
        defaultCompatibilityLayer.reload();
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
        return new ChatMessage(null, event.getAuthor().getName(), str, component.build());
    }
}
