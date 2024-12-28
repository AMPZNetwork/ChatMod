package com.ampznetwork.chatmod.api;

import com.ampznetwork.chatmod.api.model.ChatMessage;
import com.ampznetwork.chatmod.api.model.ChatMessagePacket;
import com.ampznetwork.chatmod.api.model.CompatibilityLayer;
import com.ampznetwork.libmod.api.interop.game.PlayerIdentifierAdapter;

public interface ChatModCompatibilityLayerAdapter {
    String getSourceName();

    PlayerIdentifierAdapter getPlayerAdapter();

    String getMainRabbitUri();

    String getAurionChatRabbitUri();

    CompatibilityLayer<ChatMessagePacket> getDefaultCompatibilityLayer();

    void relayInbound(ChatMessagePacket packet);

    void send(String channelName, ChatMessage message);
}
