package com.ampznetwork.chatmod.api;

import com.ampznetwork.chatmod.api.formatting.MessageFormatter;
import com.ampznetwork.chatmod.api.model.ChannelConfiguration;
import com.ampznetwork.chatmod.api.model.ChatMessage;
import com.ampznetwork.chatmod.api.model.ChatMessagePacket;
import com.ampznetwork.libmod.api.SubMod;
import org.comroid.api.net.Rabbit;

import java.util.List;

public interface ChatMod extends SubMod {
    String getServerName();

    List<ChannelConfiguration> getChannels();

    Rabbit.Exchange.Route<ChatMessagePacket> getRabbit();

    void send(String channelName, ChatMessage message);

    MessageFormatter[] buildFormatterChain();

    interface Strings {
        String AddonName = "ChatMod";
        String AddonId = "chatmod";
    }
}
