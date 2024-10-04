package com.ampznetwork.chatmod.api;

import com.ampznetwork.chatmod.api.formatting.MessageFormatter;
import com.ampznetwork.chatmod.api.model.ChannelConfiguration;
import com.ampznetwork.chatmod.api.model.ChatMessage;
import com.ampznetwork.chatmod.api.model.ChatMessagePacket;
import com.ampznetwork.libmod.api.SubMod;
import org.comroid.api.net.Rabbit;

import java.util.List;
import java.util.UUID;

public interface ChatMod extends SubMod {
    String getServerName();

    MessageFormatter getFormatter();

    List<ChannelConfiguration> getChannels();

    Rabbit.Exchange.Route<ChatMessagePacket> getRabbit();

    default String applyPlaceholders(UUID playerId, String input) {
        var player = getLib().getPlayerAdapter().getPlayer(playerId).orElseThrow();
        return input.replace("%server_name%", getServerName())
                .replace("%player_name%", player.getName());
    }

    void send(String channelName, ChatMessage message);

    interface Strings {
        String AddonName = "ChatMod";
        String AddonId = "chatmod";
    }
}
