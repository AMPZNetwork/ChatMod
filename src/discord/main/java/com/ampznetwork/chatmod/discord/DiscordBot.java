package com.ampznetwork.chatmod.discord;

import club.minnced.discord.webhook.WebhookClient;
import club.minnced.discord.webhook.WebhookClientBuilder;
import club.minnced.discord.webhook.send.WebhookMessageBuilder;
import com.ampznetwork.chatmod.api.ChatModCompatibilityLayerAdapter;
import com.ampznetwork.chatmod.api.model.ChatMessage;
import com.ampznetwork.chatmod.api.model.ChatMessagePacket;
import com.ampznetwork.chatmod.api.model.CompatibilityLayer;
import com.ampznetwork.chatmod.core.compatibility.builtin.DefaultCompatibilityLayer;
import com.ampznetwork.chatmod.discord.config.Config;
import com.ampznetwork.chatmod.discord.config.DiscordChannelConfig;
import com.ampznetwork.libmod.api.interop.game.PlayerIdentifierAdapter;
import com.ampznetwork.libmod.core.adapter.HeadlessPlayerAdapter;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.json.JsonReadFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.comroid.api.func.util.Command;
import org.comroid.api.func.util.Debug;
import org.comroid.api.func.util.DelegateStream;
import org.comroid.api.func.util.Tuple;
import org.comroid.api.info.Log;
import org.comroid.api.io.FileHandle;
import org.comroid.api.java.ResourceLoader;
import org.comroid.api.tree.Component;
import org.comroid.api.tree.Reloadable;
import org.comroid.api.tree.UncheckedCloseable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;

import java.io.InputStreamReader;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.regex.Pattern;

import static net.kyori.adventure.text.Component.*;
import static net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer.*;
import static net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer.*;

@Value
@EqualsAndHashCode(of = "config")
public class DiscordBot extends Component.Base implements ChatModCompatibilityLayerAdapter, CompatibilityLayer<ChatMessagePacket> {
    private static final Map<String, Function<ChatMessagePacket, @NotNull String>> PLACEHOLDERS = Map.of(
            "server_name", pkt -> plainText().serialize(legacyAmpersand().deserialize(pkt.getSource())),
            "channel_name", ChatMessagePacket::getChannel,
            "player_id", pkt -> {
                var player = pkt.getMessage().getSender();
                return player == null ? "" : player.getId().toString();
            },
            "player_name", pkt -> {
                var player = pkt.getMessage().getSender();
                return player == null ? pkt.getMessage().getSenderName() : player.getName();
            },
            "player_displayname", pkt -> pkt.getMessage().getSenderName(),
            "message", pkt -> plainText().serialize(pkt.getMessage().getText())
    );
    public static final  Pattern                                                   PLACEHOLDER  = Pattern.compile("%(?<key>[a-zA-Z0-9_]+)%");
    public static final  String                                                    WEBHOOK_NAME = "Minecraft Chat Link";
    public static final  String                                                    SOURCE       = "discord";
    public static FileHandle DIR;
    public static FileHandle CONFIG;
    public static        DiscordBot                                                INSTANCE;

    @SneakyThrows
    public static void main(String[] args) {
        DIR    = new FileHandle("/srv/chatmod", true);
        CONFIG = DIR.createSubFile("config.json5");

        if (!CONFIG.exists())
            try (
                    var res = DiscordBot.class.getClassLoader().getResourceAsStream("config.json5");
                    var isr = new InputStreamReader(Objects.requireNonNull(res, "Could not load default config resource"));
                    var out = CONFIG.openWriter()
            ) {
                isr.transferTo(out);
            } catch (Throwable t) {
                Log.at(Level.WARNING, "Could not save default config file: " + t);
            }

        try {
            var config = new ObjectMapper(JsonFactory.builder()
                    .enable(JsonReadFeature.ALLOW_TRAILING_COMMA)
                    .build()
                    .enable(JsonParser.Feature.ALLOW_COMMENTS)
                    .enable(JsonParser.Feature.ALLOW_YAML_COMMENTS)
                    .enable(JsonParser.Feature.ALLOW_SINGLE_QUOTES)
                    .enable(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES)
                    .enable(JsonParser.Feature.STRICT_DUPLICATE_DETECTION)
            ).readValue(CONFIG, Config.class);
            INSTANCE = new DiscordBot(config);
        } catch (Throwable t) {
            Log.at(Level.SEVERE, "Unable to initialize DiscordBot module", t);
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
        var tokenResource = ResourceLoader.fromResourceString(config.getDiscordToken());
        this.jda = JDABuilder.createLight(DelegateStream.readAll(tokenResource))
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
    public @Range(from = -1, to = Integer.MAX_VALUE) int getAutoReconnectDelaySeconds() {
        return config.getAutoReconnectDelay();
    }

    @Override
    public void relayInbound(ChatMessagePacket packet) {
        if (SOURCE.equals(packet.getSource())) return;
        config.getChannels().stream()
                .filter(channel -> Objects.equals(packet.getChannel(), channel.getChannelName()))
                .forEach(channel -> {
                    var message = new WebhookMessageBuilder()
                            .setUsername(applyPlaceholders(channel.getFormat().getWebhookUsername(), packet))
                            .setContent(applyPlaceholders(channel.getFormat().getWebhookMessage(), packet, switch (packet.getType()) {
                                case CHAT -> plainText().serialize(packet.getMessage().getText());
                                case JOIN -> applyPlaceholders(channel.getFormat().getDiscordJoinMessage(), packet);
                                case LEAVE -> applyPlaceholders(channel.getFormat().getDiscordLeaveMessage(), packet);
                            }))
                            .setAvatarUrl(applyPlaceholders(channel.getFormat().getWebhookAvatar(), packet))
                            .build();
                    obtainWebhook(channel, jda.getTextChannelById(channel.getChannelId()))
                            .thenCompose(wh -> wh.send(message))
                            .exceptionally(Debug.exceptionLogger("Could not send Message using Webhook"));
                });
    }

    @Override
    public void relayOutbound(ChatMessagePacket packet) {
        defaultCompatibilityLayer.send(packet);
    }

    @Override
    public boolean skip(ChatMessagePacket packet) {
        return false;
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

    private String applyPlaceholders(String format, ChatMessagePacket packet) {
        return applyPlaceholders(format, packet, null);
    }

    private String applyPlaceholders(String format, ChatMessagePacket packet, @Nullable String message) {
        var placeholders = PLACEHOLDERS.entrySet().stream()
                .map(Tuple.N2::new)
                .map(e -> !"message".equals(e.a) ? e : new Tuple.N2<String, Function<ChatMessagePacket, @NotNull String>>(e.a,
                        pkt -> Objects.requireNonNullElseGet(message, () -> e.b.apply(pkt))))
                .collect(Tuple.N2.toMap());
        var matcher = PLACEHOLDER.matcher(format);
        var buf     = new StringBuilder();
        var lastEnd = 0;

        while (matcher.find()) {
            var value = placeholders.get(matcher.group("key")).apply(packet);
            buf.append(format, lastEnd, matcher.start())
                    .append(value);
            lastEnd = matcher.end();
        }

        // if had no placeholders; use plain string
        if (buf.isEmpty())
            buf.append(format);
        else buf.append(format, lastEnd, format.length());

        return buf.toString();
    }

    @Command
    @Override
    public void reload() {
        child(CompatibilityLayer.class).ifPresent(Reloadable::reload);
    }

    public void relayOutbound(MessageReceivedEvent event) {
        if (event.getAuthor().isBot())
            return;
        config.getChannels().stream()
                .filter(channel -> channel.getChannelId() == event.getChannel().getIdLong())
                .forEach(channel -> sendChat(channel.getChannelName(), convertMessage(event, channel)));
    }

    private CompletableFuture<WebhookClient> obtainWebhook(DiscordChannelConfig config, TextChannel channel) {
        //noinspection DataFlowIssue -> we want the exception here for .exceptionallyCompose()
        return CompletableFuture.supplyAsync(() -> WebhookClient.withUrl(config.getWebhookUrl()))
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

    private ChatMessage convertMessage(MessageReceivedEvent event, DiscordChannelConfig channel) {
        var discord   = text("DISCORD ", TextColor.color(86, 98, 246));
        var inviteUrl = channel.getInviteUrl();
        if (inviteUrl != null)
            discord = discord.clickEvent(ClickEvent.openUrl(inviteUrl))
                    .hoverEvent(HoverEvent.showText(text("Click for Invite link...")));
        var str = event.getMessage().getContentDisplay();
        var component = text()
                .append(discord)
                .append(text(event.getAuthor().getEffectiveName().trim(),
                        Optional.ofNullable(event.getMember())
                                .map(Member::getColorRaw)
                                .map(TextColor::color)
                                .orElse(NamedTextColor.WHITE)))
                .append(text(": " + str, NamedTextColor.WHITE)
                        .clickEvent(ClickEvent.openUrl(event.getJumpUrl()))
                        .hoverEvent(HoverEvent.showText(text("Jump to Message..."))));
        return new ChatMessage(null, event.getAuthor().getName(), str, component.build());
    }
}
