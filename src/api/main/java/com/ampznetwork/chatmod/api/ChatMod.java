package com.ampznetwork.chatmod.api;

import com.ampznetwork.chatmod.api.formatting.MessageFormatter;
import com.ampznetwork.chatmod.api.model.ChannelConfiguration;
import com.ampznetwork.chatmod.api.model.ChatMessagePacket;
import com.ampznetwork.chatmod.api.model.CompatibilityLayer;
import com.ampznetwork.libmod.api.SubMod;
import com.ampznetwork.libmod.api.interop.game.IPlayerAdapter;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.comroid.api.Polyfill;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public interface ChatMod extends SubMod, ChatModCompatibilityLayerAdapter {
    @Override
    default IPlayerAdapter getPlayerAdapter() {
        return SubMod.super.getPlayerAdapter();
    }

    boolean isListenerCompatibilityMode();

    MessageFormatter getFormatter();

    List<ChannelConfiguration> getChannels();

    Set<CompatibilityLayer<?>> getCompatibilityLayers();

    @Override
    default CompatibilityLayer<ChatMessagePacket> getDefaultCompatibilityLayer() {
        return getCompatibilityLayers().stream()
                .filter(CompatibilityLayer::isDefault)
                .findAny()
                .map(Polyfill::<CompatibilityLayer<ChatMessagePacket>>uncheckedCast)
                .orElseThrow();
    }

    boolean isJoinLeaveEnabled();

    boolean isReplaceDefaultJoinLeaveMessages();

    Set<String> getJoinLeaveChannels();

    @Nullable String getCustomJoinMessageFormat();

    @Nullable String getCustomLeaveMessageFormat();

    @Override
    default TextColor getThemeColor() {
        return NamedTextColor.GREEN;
    }

    default String applyPlaceholders(UUID playerId, String input) {
        var player = getLib().getPlayerAdapter().getPlayer(playerId).orElseThrow();
        return input.replace("%server_name%", getSourceName())
                .replace("%player_name%", getLib().getPlayerAdapter().getDisplayName(playerId));
    }

    interface Strings {
        String AddonName = "ChatMod";
        String AddonId = "chatmod";
    }
}
