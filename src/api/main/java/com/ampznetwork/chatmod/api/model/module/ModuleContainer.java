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
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.comroid.api.attr.Named;
import org.comroid.api.tree.Container;
import org.comroid.api.tree.Startable;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public interface ModuleContainer extends Container, Named {
    PlayerIdentifierAdapter getPlayerAdapter();

    ChatModules getChatModules();

    List<Channel> getChannels();

    String getServerName();

    Module<?> getDefaultModule();

    Stream<Module<?>> createModules();

    default void initModules() {
        createModules().peek(Startable::start).forEach(this::addChildren);
    }

    default Optional<LibMod> wrapLib() {
        return this instanceof SubMod sub ? Optional.of(sub.getLib()) : Optional.empty();
    }

    default void sendChat(String channelName, ChatMessage message) {
        getDefaultModule().broadcastInbound(new ChatMessagePacketImpl(PacketType.CHAT, getServerName(), channelName, message, List.of(getServerName())));
    }

    default void sendEvent(String channelName, Player player, PacketType type, TextComponent text) {
        getDefaultModule().broadcastInbound(new ChatMessagePacketImpl(type,
                getServerName(),
                channelName,
                new ChatMessage(player,
                        getPlayerAdapter().getDisplayName(player.getId()),
                        PlainTextComponentSerializer.plainText().serialize(text),
                        text),
                List.of(getServerName())));
    }
}
