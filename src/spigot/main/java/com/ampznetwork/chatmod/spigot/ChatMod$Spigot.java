package com.ampznetwork.chatmod.spigot;

import com.ampznetwork.chatmod.api.ChatMod;
import com.ampznetwork.chatmod.core.formatting.ChatMessageFormatter;
import com.ampznetwork.chatmod.api.model.ChannelConfiguration;
import com.ampznetwork.chatmod.api.model.ChatMessage;
import com.ampznetwork.chatmod.api.model.ChatMessagePacket;
import com.ampznetwork.chatmod.core.ChatModCommands;
import com.ampznetwork.chatmod.spigot.adp.SpigotEventDispatch;
import com.ampznetwork.chatmod.spigot.serialization.ChatMessagePacketByteConverter;
import com.ampznetwork.libmod.api.entity.DbObject;
import com.ampznetwork.libmod.spigot.SubMod$Spigot;
import lombok.Getter;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.configuration.MemorySection;
import org.comroid.api.Polyfill;
import org.comroid.api.func.util.Command;
import org.comroid.api.net.Rabbit;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;

@Getter
@Slf4j(topic = ChatMod.Strings.AddonName)
public class ChatMod$Spigot extends SubMod$Spigot implements ChatMod {
    List<ChannelConfiguration> channels = new ArrayList<>();
    @NonFinal ChatMessageFormatter formatter;
    @NonFinal           Rabbit.Exchange.Route<ChatMessagePacket> rabbit;
    @NonFinal @Nullable boolean hasPlaceholderApi;

    public ChatMod$Spigot() {
        super(Set.of(), Set.of());
    }

    @Override
    public String getServerName() {
        return getConfig().getString("server.name", "&eMC");
    }

    @Override
    public String applyPlaceholders(UUID playerId, String input) {
        var player = getServer().getOfflinePlayer(playerId);
        return hasPlaceholderApi
               ? PlaceholderAPI.setPlaceholders(player, input)
               : ChatMod.super.applyPlaceholders(playerId, input);
    }

    @Override
    public void send(String channelName, ChatMessage message) {
        var packet = new ChatMessagePacket(getServerName(), channelName, message);
        rabbit.send(packet);
    }

    @Command
    public @NotNull TextComponent reload() {
        // reload channel configuration
        channels.clear();
        loadChannels();

        // rejoin current players
        var mainChannel = channels.getFirst();
        getLib().getPlayerAdapter().getCurrentPlayers()
                .map(DbObject::getId)
                .forEach(mainChannel.getPlayerIDs()::add);

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

        formatter = ChatMessageFormatter.of(Polyfill.<MemorySection>uncheckedCast(getConfig().get("formatting")).getValues(true));
        rabbit = Rabbit.of(getConfig().getString("rabbitmq.url"))
                .map(rabbit -> rabbit.bind("chat", "", new ChatMessagePacketByteConverter(this)))
                .orElseThrow();
        rabbit.listen().subscribeData(this::handle);
        getServer().getPluginManager().registerEvents(new SpigotEventDispatch(this), this);
    }

    @Override
    public Class<?> getModuleType() {
        return ChatMod.class;
    }

    public void handle(ChatMessagePacket packet) {
        getLogger().info(packet.getMessage().getPlaintext());
        var targetChannel = packet.getChannel();
        channels.stream()
                .filter(channel -> channel.getName().equals(targetChannel))
                .flatMap(channel -> Stream.concat(channel.getPlayerIDs().stream(), channel.getSpyIDs().stream()))
                .forEach(id -> lib.getPlayerAdapter().send(id, packet.getMessage().getText()));
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
                    .publish(Boolean.parseBoolean(String.valueOf(config.getOrDefault("publish", true))))
                    .build());
        }
        getLogger().info("Loaded " + channels.size() + " channels");
    }
}
