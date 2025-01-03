package com.ampznetwork.chatmod.api.model;

import com.ampznetwork.chatmod.core.model.ChatMessagePacketImpl;
import com.ampznetwork.libmod.api.entity.Player;
import com.ampznetwork.libmod.api.interop.game.PlayerIdentifierAdapter;
import net.kyori.adventure.text.TextComponent;
import org.comroid.api.attr.Named;
import org.jetbrains.annotations.Range;

import static net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer.*;
import static net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer.*;

public interface ChatModCompatibilityLayerAdapter extends Named {
    String getSourceName();

    PlayerIdentifierAdapter getPlayerAdapter();

    String getMainRabbitUri();

    String getAurionChatRabbitUri();

    @Range(from = -1, to = Integer.MAX_VALUE)
    int getAutoReconnectDelaySeconds();

    CompatibilityLayer<ChatMessagePacket> getDefaultCompatibilityLayer();

    default String getServerName() {
        return plainText().serialize(legacyAmpersand().deserialize(getSourceName()));
    }

    void relayInbound(ChatMessagePacket packet);

    void relayOutbound(ChatMessagePacket packet);

    default boolean skip(ChatMessagePacket packet) {
        return packet.getRoute().contains(getSourceName());
    }

    default void sendChat(String channelName, ChatMessage message) {
        relayOutbound(new ChatMessagePacketImpl(PacketType.CHAT, getSourceName(), channelName, message));
    }

    default void sendEvent(String channelName, Player player, PacketType packetType, TextComponent text) {
        relayOutbound(new ChatMessagePacketImpl(packetType, getSourceName(), channelName, new ChatMessage(player,
                getPlayerAdapter().getDisplayName(player.getId()), plainText().serialize(text), text)));
    }
}
