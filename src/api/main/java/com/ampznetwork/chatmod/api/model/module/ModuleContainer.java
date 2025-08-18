package com.ampznetwork.chatmod.api.model.module;

import com.ampznetwork.chatmod.api.model.config.ChatModules;
import com.ampznetwork.chatmod.api.model.config.channel.Channel;
import com.ampznetwork.chatmod.api.model.protocol.ChatMessage;
import com.ampznetwork.chatmod.api.model.protocol.internal.ChatMessagePacketImpl;
import com.ampznetwork.chatmod.api.model.protocol.internal.PacketType;
import com.ampznetwork.libmod.api.LibMod;
import com.ampznetwork.libmod.api.SubMod;
import com.ampznetwork.libmod.api.entity.Player;
import com.ampznetwork.libmod.api.interop.game.PlayerIdentifierAdapter;
import com.ampznetwork.libmod.api.model.info.ServerInfoProvider;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.comroid.api.attr.Named;
import org.comroid.api.tree.Container;
import org.comroid.api.tree.Startable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public interface ModuleContainer extends Container, Named {
    ServerInfoProvider getLib();

    PlayerIdentifierAdapter getPlayerAdapter();

    ChatModules getChatModules();

    List<Channel> getChannels();

    boolean isListenerCompatibilityMode();

    Module<?> getDefaultModule();

    Stream<Module<?>> createModules();

    default void initModules() {
        var ls = createModules().toList();
        ls.forEach(this::addChildren);
        ls.stream().filter(Module::isEnabled).forEach(Startable::start);
    }

    default Optional<LibMod> wrapLib() {
        return this instanceof SubMod sub ? Optional.of(sub.getLib()) : Optional.empty();
    }

    default void sendChat(String channelName, ChatMessage message) {
        var module = getDefaultModule();
        module.broadcastInbound(new ChatMessagePacketImpl(PacketType.CHAT,
                getLib().getServerName(),
                channelName,
                message,
                new ArrayList<>()));
    }

    default void sendEvent(String channelName, Player player, PacketType type, TextComponent text) {
        var module = getDefaultModule();
        module.broadcastInbound(new ChatMessagePacketImpl(type,
                getLib().getServerName(),
                channelName,
                new ChatMessage(player, getPlayerAdapter().getDisplayName(player.getId()), PlainTextComponentSerializer.plainText().serialize(text), text),
                new ArrayList<>()));
    }
}
