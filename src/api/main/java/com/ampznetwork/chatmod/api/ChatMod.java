package com.ampznetwork.chatmod.api;

import com.ampznetwork.chatmod.api.formatting.MessageFormatter;
import com.ampznetwork.chatmod.api.model.ChannelConfiguration;
import com.ampznetwork.chatmod.api.model.ChatMessage;
import com.ampznetwork.chatmod.api.model.ChatMessagePacket;
import com.ampznetwork.chatmod.api.model.CompatibilityLayer;
import com.ampznetwork.libmod.api.SubMod;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public interface ChatMod extends SubMod {
    String getServerName();

    MessageFormatter getFormatter();

    List<ChannelConfiguration> getChannels();

    String getMainRabbitUri();

    String getAurionChatRabbitUri();

    Set<CompatibilityLayer<?>> getCompatibilityLayers();

    void relayInbound(ChatMessagePacket packet);

    @Override
    default TextColor getThemeColor() {
        return NamedTextColor.GREEN;
    }

    default String applyPlaceholders(UUID playerId, String input) {
        var player = getLib().getPlayerAdapter().getPlayer(playerId).orElseThrow();
        return input.replace("%server_name%", getServerName())
                .replace("%player_name%", getLib().getPlayerAdapter().getDisplayName(playerId));
    }

    void send(String channelName, ChatMessage message);

    interface Strings {
        String AddonName = "ChatMod";
        String AddonId = "chatmod";
    }
}
