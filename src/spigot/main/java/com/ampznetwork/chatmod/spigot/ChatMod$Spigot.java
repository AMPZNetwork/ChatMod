package com.ampznetwork.chatmod.spigot;

import com.ampznetwork.chatmod.api.ChatMod;
import com.ampznetwork.chatmod.api.formatting.MessageFormatter;
import com.ampznetwork.chatmod.core.formatting.DecorateFormatter;
import com.ampznetwork.chatmod.core.formatting.MarkdownFormatter;
import com.ampznetwork.chatmod.core.formatting.RegexFormatter;
import com.ampznetwork.chatmod.core.formatting.UrlFormatter;
import com.ampznetwork.chatmod.api.model.ChannelConfiguration;
import com.ampznetwork.chatmod.api.model.ChatMessage;
import com.ampznetwork.chatmod.api.model.ChatMessagePacket;
import com.ampznetwork.chatmod.core.ChatModCommands;
import com.ampznetwork.chatmod.spigot.adp.SpigotEventDispatch;
import com.ampznetwork.libmod.api.entity.DbObject;
import com.ampznetwork.libmod.spigot.SubMod$Spigot;
import lombok.Getter;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.comroid.api.Polyfill;
import org.comroid.api.func.util.Command;
import org.comroid.api.net.Rabbit;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

@Getter
@Slf4j(topic = ChatMod.Strings.AddonName)
public class ChatMod$Spigot extends SubMod$Spigot implements ChatMod {
    List<ChannelConfiguration> channels = new ArrayList<>();
    @NonFinal Rabbit.Exchange.Route<ChatMessagePacket> rabbit;

    public ChatMod$Spigot() {
        super(Set.of(), Set.of());
    }

    @Override
    public String getServerName() {
        return getConfig().getString("server.name", "&eMC");
    }

    @Override
    public void send(String channelName, ChatMessage message) {
        var packet = new ChatMessagePacket(getServerName(), channelName, message);
        rabbit.send(packet);
    }

    @Command
    public void reload() {
        // reload channel configuration
        channels.clear();
        loadChannels();

        // rejoin current players
        var mainChannel = channels.getFirst();
        getLib().getPlayerAdapter().getCurrentPlayers()
                .map(DbObject::getId)
                .forEach(mainChannel.getPlayerIDs()::add);
    }

    @Override
    public void onLoad() {
        cmdr.register(ChatModCommands.class);
        cmdr.register(this);

        super.onLoad();

        reload();
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

    @Override
    public void onEnable() {
        super.onEnable();

        rabbit = Rabbit.of(getConfig().getString("rabbitmq.url"))
                .map(rabbit -> rabbit.bind("chat", "", ChatMessagePacket.CONVERTER))
                .orElseThrow();
        rabbit.listen().subscribeData(this::handle);
        getServer().getPluginManager().registerEvents(new SpigotEventDispatch(this), this);
    }

    @Override
    public Class<?> getModuleType() {
        return ChatMod.class;
    }
    
    @Override
    public MessageFormatter[] buildFormatterChain() {
        var cfg = getConfig();
        var ls  = Polyfill.<List<Map<String, ?>>>uncheckedCast(cfg.getList("formatters"));
        var out = new ArrayList<MessageFormatter>();
        for (var $0 : ls) {
            var type   = $0.keySet().stream().findAny().orElseThrow();
            var config = Polyfill.<Map<String, ?>>uncheckedCast($0.get(type));
            switch (type) {
                case "urls":
                    out.add(UrlFormatter.of(config));
                    break;
                case "markdown":
                    out.add(MarkdownFormatter.of(config));
                    break;
                case "regex":
                    out.add(RegexFormatter.of(config));
                    break;
                case "decorate":
                    out.add(DecorateFormatter.of(config));
                    break;
                default:
                    throw new IllegalStateException("Unexpected value: " + type);
            }
        }
        return out.toArray(MessageFormatter[]::new);
    }

    public void handle(ChatMessagePacket packet) {
        var targetChannel = packet.getChannel();
        channels.stream()
                .filter(channel -> channel.getName().equals(targetChannel))
                .flatMap(channel -> Stream.concat(channel.getPlayerIDs().stream(), channel.getSpyIDs().stream()))
                .forEach(id -> lib.getPlayerAdapter().send(id, packet.getMessage().getText()));
    }
}
