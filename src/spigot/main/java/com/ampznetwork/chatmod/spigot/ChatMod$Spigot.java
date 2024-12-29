package com.ampznetwork.chatmod.spigot;

import com.ampznetwork.chatmod.api.ChatMod;
import com.ampznetwork.chatmod.api.model.ChannelConfiguration;
import com.ampznetwork.chatmod.api.model.ChatMessagePacket;
import com.ampznetwork.chatmod.api.model.CompatibilityLayer;
import com.ampznetwork.chatmod.core.ChatModCommands;
import com.ampznetwork.chatmod.core.compatibility.aurionchat.AurionChatCompatibilityLayer;
import com.ampznetwork.chatmod.core.compatibility.builtin.DefaultCompatibilityLayer;
import com.ampznetwork.chatmod.core.formatting.ChatMessageFormatter;
import com.ampznetwork.chatmod.discord.DiscordBot;
import com.ampznetwork.chatmod.discord.config.Config;
import com.ampznetwork.chatmod.discord.config.DiscordChannelMapping;
import com.ampznetwork.chatmod.generated.PluginYml;
import com.ampznetwork.chatmod.spigot.adp.SpigotEventDispatch;
import com.ampznetwork.libmod.api.entity.DbObject;
import com.ampznetwork.libmod.spigot.SubMod$Spigot;
import lombok.Getter;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.configuration.MemorySection;
import org.comroid.api.Polyfill;
import org.comroid.api.func.util.Command;
import org.comroid.api.func.util.Streams;
import org.comroid.api.info.Log;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.comroid.api.text.Word.*;

@Getter
@Slf4j(topic = ChatMod.Strings.AddonName)
public class ChatMod$Spigot extends SubMod$Spigot implements ChatMod {
    List<ChannelConfiguration> channels            = new ArrayList<>();
    Set<CompatibilityLayer<?>> compatibilityLayers = new HashSet<>() {{
        add(new DefaultCompatibilityLayer(ChatMod$Spigot.this));
        add(new AurionChatCompatibilityLayer(ChatMod$Spigot.this));
    }};
    @NonFinal           ChatMessageFormatter formatter;
    @NonFinal           Set<String> joinLeaveChannels;
    @NonFinal @Nullable DiscordBot  discordBot;
    @NonFinal @Nullable boolean              hasPlaceholderApi;

    public ChatMod$Spigot() {
        super(Set.of(), Set.of());
    }

    @Override
    public String getSourceName() {
        return getConfig().getString("server.name", "&eMC");
    }

    @Override
    public String getMainRabbitUri() {
        return getConfig().getString("rabbitmq.uri");
    }

    @Override
    public String getAurionChatRabbitUri() {
        return getConfig().getString("compatibility.aurionchat");
    }

    @Override
    public boolean isListenerCompatibilityMode() {
        return getConfig().getBoolean("compatibility.listeners", false);
    }

    @Override
    public boolean isJoinLeaveEnabled() {
        return getConfig().getBoolean("events.join_leave.enable", false);
    }

    @Override
    public boolean isReplaceDefaultJoinLeaveMessages() {
        return getConfig().getBoolean("events.join_leave.replace", false);
    }

    @Override
    public @Nullable String getCustomJoinMessageFormat() {
        return getConfig().getString("events.join_leave.format_join", null);
    }

    @Override
    public @Nullable String getCustomLeaveMessageFormat() {
        return getConfig().getString("events.join_leave.format_leave", null);
    }

    @Override
    public Class<?> getModuleType() {
        return ChatMod.class;
    }

    @Override
    public void relayInbound(ChatMessagePacket packet) {
        if (isListenerCompatibilityMode() && getSourceName().equals(packet.getSource())) return;
        getLogger().info(packet.getMessage().getPlaintext());
        var targetChannel = packet.getChannel();
        channels.stream()
                .filter(channel -> channel.getName().equals(targetChannel))
                .flatMap(channel -> Stream.concat(channel.getPlayerIDs().stream(), channel.getSpyIDs().stream()))
                .forEach(id -> lib.getPlayerAdapter().send(id, packet.getMessage().getFullText()));
    }

    @Override
    public String applyPlaceholders(UUID playerId, String input) {
        var player = getServer().getOfflinePlayer(playerId);
        return hasPlaceholderApi
               ? PlaceholderAPI.setPlaceholders(player, input)
               : ChatMod.super.applyPlaceholders(playerId, input);
    }

    @Override
    public void relayOutbound(ChatMessagePacket packet) {
        compatibilityLayers.stream()
                .filter(CompatibilityLayer::isEnabled)
                .forEach(layer -> layer.send(packet));
    }

    @Command
    public @NotNull TextComponent reload() {
        reloadConfig();
        var config = getConfig();

        // reload channel configuration
        channels.clear();
        loadChannels();

        // sync discord bot config
        if (discordBot != null) discordBot.close();
        compatibilityLayers.removeIf(DiscordBot.class::isInstance);
        var token = config.getString("discord_bot_token", null);
        if (token != null && !token.isBlank() && !"none".equals(token)) {
            var botConfig = new Config(token, getMainRabbitUri(), channels.stream()
                    .flatMap(Streams.filter(channel -> channel.getDiscordChannelId() != null,
                            channel -> getLogger().warning(
                                    "Cannot load discord integration for channel '%s' because it has no Discord Channel ID configured"
                                            .formatted(channel.getName()))))
                    .map(channel -> new DiscordChannelMapping(channel.getName(),
                            channel.getDiscordChannelId(), channel.getDiscordWebhookUrl(), channel.getDiscordInviteUrl(), null/*todo*/))
                    .collect(Collectors.toUnmodifiableSet()));
            discordBot = new DiscordBot(botConfig, getPlayerAdapter(), getDefaultCompatibilityLayer());
            compatibilityLayers.add(discordBot);
            Log.at(Level.INFO, "Discord Bot module loaded and initialized with %s".formatted(
                    plural("channel", "s", botConfig.getChannels().size())));
        }

        // rejoin current players
        var mainChannel = channels.getFirst();
        getLib().getPlayerAdapter().getCurrentPlayers()
                .map(DbObject::getId)
                .forEach(mainChannel.getPlayerIDs()::add);

        this.formatter = ChatMessageFormatter.of(Polyfill.<MemorySection>uncheckedCast(config.get("formatting")).getValues(true));

        var values = Stream.<String>empty();
        var key    = "events.join_leave.channels";

        if (config.isString(key))
            values = Stream.ofNullable(config.getString(key, null));
        if (config.isList(key))
            values = config.getStringList(key).stream();

        this.joinLeaveChannels = values
                .flatMap(str -> "*".equals(str)
                                ? getChannels().stream()
                                        .map(ChannelConfiguration::getName)
                                : Stream.of(str))
                .collect(Collectors.toUnmodifiableSet());

        return Component.text("Reloading ChatMod configuration complete").color(NamedTextColor.GREEN);
    }

    @Override
    public void onLoad() {
        cmdr.register(ChatModCommands.class);
        cmdr.register(this);

        super.onLoad();
        reload();

        hasPlaceholderApi = getServer().getPluginManager().getPlugin("PlaceholderAPI") != null;
    }

    @Override
    public void onEnable() {
        super.onEnable();
        getServer().getPluginManager().registerEvents(new SpigotEventDispatch(this), this);
        reconnect();
    }

    @Command(permission = PluginYml.Permission.chatmod.RECONNECT)
    public void reconnect() {
        compatibilityLayers.forEach(CompatibilityLayer::reload);
    }

    private void loadChannels() {
        var cfg = getConfig();
        var ls  = Polyfill.<List<Map<String, ?>>>uncheckedCast(cfg.getList("channels"));
        for (var $0 : ls) {
            var name   = $0.keySet().stream().findAny().orElseThrow();
            var config = Polyfill.<Map<String, Object>>uncheckedCast($0.get(name));
            channels.add(ChannelConfiguration.builder()
                    .name(name)
                    .alias((String) config.getOrDefault("alias", name.substring(0, 1)))
                    .permission((String) config.getOrDefault("permission", null))
                    .discordChannelId((Long) config.getOrDefault("discord_channel_id", null))
                    .discordWebhookUrl((String) config.getOrDefault("discord_webhook_url", null))
                    .discordInviteUrl((String) config.getOrDefault("discord_invite_url", null))
                    .publish(Boolean.parseBoolean(String.valueOf(config.getOrDefault("publish", true))))
                    .build());
        }
        getLogger().info("Loaded " + channels.size() + " channels");
    }
}
