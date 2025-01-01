package com.ampznetwork.chatmod.api;

import com.ampznetwork.chatmod.api.model.ChatMessage;
import com.ampznetwork.chatmod.api.model.ChatMessagePacket;
import com.ampznetwork.chatmod.api.model.CompatibilityLayer;
import com.ampznetwork.chatmod.api.model.MessageType;
import com.ampznetwork.libmod.api.entity.Player;
import com.ampznetwork.libmod.api.interop.game.PlayerIdentifierAdapter;
import net.kyori.adventure.text.TextComponent;
import org.jetbrains.annotations.Range;

import static net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer.*;

public interface ChatModCompatibilityLayerAdapter {
    String getSourceName();

    PlayerIdentifierAdapter getPlayerAdapter();

    String getMainRabbitUri();

    String getAurionChatRabbitUri();

    @Range(from = -1, to = Integer.MAX_VALUE)
    int getAutoReconnectDelaySeconds();

    CompatibilityLayer<ChatMessagePacket> getDefaultCompatibilityLayer();

    void relayInbound(ChatMessagePacket packet);

    void relayOutbound(ChatMessagePacket packet);

    default boolean skip(ChatMessagePacket packet) {
        return false;
    }

    default void sendChat(String channelName, ChatMessage message) {
        relayOutbound(new ChatMessagePacket(MessageType.CHAT, getSourceName(), channelName, message));
    }

    default void sendEvent(String channelName, Player player, MessageType type, TextComponent text) {
        relayOutbound(new ChatMessagePacket(type, getSourceName(), channelName, new ChatMessage(player,
                getPlayerAdapter().getDisplayName(player.getId()), plainText().serialize(text), text)));
    }
}
