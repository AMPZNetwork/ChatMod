package com.ampznetwork.chatmod.api;

import com.ampznetwork.chatmod.api.model.ChatMessage;
import com.ampznetwork.chatmod.api.model.ChatMessagePacket;
import org.comroid.api.net.Rabbit;

public interface ChatMod {
    String getServerName();

    Rabbit.Exchange.Route<ChatMessagePacket> getRabbit();

    interface Strings {
        String AddonName = "ChatMod";
        String AddonId = "chatmod";
    }
}
