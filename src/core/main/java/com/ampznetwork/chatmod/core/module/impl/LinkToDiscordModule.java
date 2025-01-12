package com.ampznetwork.chatmod.core.module.impl;

import club.minnced.discord.webhook.WebhookClient;
import club.minnced.discord.webhook.WebhookClientBuilder;
import club.minnced.discord.webhook.send.WebhookMessageBuilder;
import com.ampznetwork.chatmod.api.model.config.ChatModules;
import com.ampznetwork.chatmod.api.model.config.channel.Channel;
import com.ampznetwork.chatmod.api.model.config.discord.DiscordChannel;
import com.ampznetwork.chatmod.api.model.formatting.DefaultPlaceholder;
import com.ampznetwork.chatmod.api.model.formatting.FormatPlaceholder;
import com.ampznetwork.chatmod.api.model.module.ModuleContainer;
import com.ampznetwork.chatmod.api.model.protocol.ChatMessage;
import com.ampznetwork.chatmod.api.model.protocol.ChatMessagePacket;
import com.ampznetwork.chatmod.api.model.protocol.internal.ChatMessagePacketImpl;
import com.ampznetwork.chatmod.api.model.protocol.internal.PacketType;
import com.ampznetwork.chatmod.core.module.IdentityModule;
import lombok.SneakyThrows;
import lombok.ToString;
import lombok.Value;
import lombok.experimental.NonFinal;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.EventListener;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.comroid.api.func.util.Debug;
import org.comroid.api.func.util.DelegateStream;
import org.comroid.api.java.ResourceLoader;
import org.comroid.api.tree.UncheckedCloseable;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

import static com.ampznetwork.chatmod.api.model.formatting.FormatPlaceholder.*;
import static net.kyori.adventure.text.Component.*;
import static net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer.*;

@Value
@NonFinal
@ToString(callSuper = true)
public class LinkToDiscordModule extends IdentityModule<ChatModules.DiscordProviderConfig> {
    public static final String WEBHOOK_NAME = "Minecraft Chat Link";
    JDA jda;

    public LinkToDiscordModule(ModuleContainer mod, ChatModules.DiscordProviderConfig config, JDA jda) {
        super(mod, config);

        this.jda = jda;
    }

    @SneakyThrows
    public LinkToDiscordModule(ModuleContainer mod, ChatModules.DiscordProviderConfig config) {
        super(mod, config);

        var tokenRes = ResourceLoader.fromResourceString(config.getToken());
        if (tokenRes == null)
            throw new IllegalArgumentException("Discord token not found");
        this.jda = JDABuilder.createLight(DelegateStream.readAll(tokenRes))
                .enableIntents(GatewayIntent.getIntents(GatewayIntent.ALL_INTENTS))
                .addEventListeners((EventListener) event -> {
                    if (event instanceof MessageReceivedEvent mre) mod.getChannels()
                            .stream()
                            .map(Channel::getDiscord)
                            .filter(Objects::nonNull)
                            .filter(channel -> channel.getChannelId() == mre.getChannel().getIdLong())
                            .map(channel -> new ChatMessagePacketImpl(PacketType.CHAT,
                                    mod.getServerName(),
                                    channel.getName(),
                                    convertMessage(mre, channel)))
                            .forEach(this::relayOutbound);
                })
                .build();
        this.<UncheckedCloseable>addChild(jda::shutdownNow);
    }

    @Override
    public boolean isAvailable() {
        return jda.getStatus() == JDA.Status.CONNECTED;
    }

    @Override
    public boolean acceptInbound(ChatMessagePacket packet) {
        return super.acceptInbound(packet) && !Objects.equals(mod.getServerName(), packet.getSource());
    }

    @Override
    public void relayInbound(ChatMessagePacket packet) {
        //noinspection OptionalOfNullableMisuse
        mod.getChannels()
                .stream()
                .flatMap(channel -> Stream.ofNullable(channel.getDiscord())
                        .filter(dc -> Optional.ofNullable(dc.getName()).orElseGet(channel::getName).equals(packet.getChannel())))
                .forEach(channel -> {
                    var message = new WebhookMessageBuilder().setUsername(DEFAULT_CONTEXT.apply(mod, packet, channel.getFormat().getMessageAuthor()))
                            .setContent(FormatPlaceholder.override(DefaultPlaceholder.MESSAGE, switch (packet.getPacketType()) {
                                case CHAT -> plainText().serialize(packet.getMessage().getText());
                                case JOIN, LEAVE -> DEFAULT_CONTEXT.apply(mod, packet, packet.getPacketType().getFormat(channel.getFormat()));
                            }).apply(mod, packet, channel.getFormat().getMessageContent()))
                            .setAvatarUrl(DEFAULT_CONTEXT.apply(mod, packet, channel.getFormat().getMessageUserAvatar()))
                            .build();

                    obtainWebhook(channel, jda.getTextChannelById(channel.getChannelId())).thenCompose(webhook -> webhook.send(message))
                            .exceptionally(Debug.exceptionLogger("Could not send Message using Webhook"));
                });
    }

    @Override
    public void relayOutbound(ChatMessagePacket packet) {
        broadcastInbound(packet);
    }

    private CompletableFuture<WebhookClient> obtainWebhook(DiscordChannel config, TextChannel channel) {
        //noinspection DataFlowIssue -> we want the exception here for .exceptionallyCompose()
        return CompletableFuture.supplyAsync(() -> WebhookClient.withUrl(config.getWebhookUrl()))
                .exceptionallyCompose(ignored -> channel.retrieveWebhooks()
                        .submit()
                        .thenCompose(webhooks -> webhooks.stream()
                                .filter(webhook -> WEBHOOK_NAME.equals(webhook.getName()))
                                .findAny()
                                .map(CompletableFuture::completedFuture)
                                .orElseGet(() -> channel.createWebhook(WEBHOOK_NAME).submit()))
                        .thenApply(webhook -> WebhookClientBuilder.fromJDA(webhook).build()))
                .exceptionally(Debug.exceptionLogger("Internal Exception when obtaining Webhook"));
    }

    private ChatMessage convertMessage(MessageReceivedEvent event, DiscordChannel channel) {
        var discord   = text("DISCORD ", TextColor.color(86, 98, 246));
        var inviteUrl = channel.getInviteUrl();
        if (inviteUrl != null) discord = discord.clickEvent(ClickEvent.openUrl(inviteUrl)).hoverEvent(HoverEvent.showText(text("Click for Invite link...")));
        var str = event.getMessage().getContentDisplay();
        var component = text().append(discord)
                .append(text(event.getAuthor().getEffectiveName().trim(),
                        Optional.ofNullable(event.getMember()).map(Member::getColorRaw).map(TextColor::color).orElse(NamedTextColor.WHITE)))
                .append(text(": " + str, NamedTextColor.WHITE).clickEvent(ClickEvent.openUrl(event.getJumpUrl()))
                        .hoverEvent(HoverEvent.showText(text("Jump to Message..."))));
        return new ChatMessage(null, event.getAuthor().getName(), str, component.build());
    }
}
