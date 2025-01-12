package com.ampznetwork.chatmod.api;

import com.ampznetwork.chatmod.api.formatting.MessageFormatter;
import com.ampznetwork.chatmod.api.model.TextResourceProvider;
import com.ampznetwork.chatmod.api.model.module.ModuleContainer;
import com.ampznetwork.libmod.api.SubMod;
import com.ampznetwork.libmod.api.interop.game.IPlayerAdapter;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;

import java.util.UUID;

public interface ChatMod extends SubMod, ModuleContainer {
    @Override
    default IPlayerAdapter getPlayerAdapter() {
        return SubMod.super.getPlayerAdapter();
    }

    boolean isListenerCompatibilityMode();

    MessageFormatter getFormatter();

    boolean isJoinLeaveEnabled();

    boolean isReplaceDefaultJoinLeaveMessages();

    @Override
    default TextColor getThemeColor() {
        return NamedTextColor.GREEN;
    }

    TextResourceProvider text();

    default String applyPlaceholderApi(UUID playerId, String input) {
        return input.replace("%server_name%", getServerName())
                .replace("%player_name%", getLib().getPlayerAdapter().getDisplayName(playerId));
    }

    interface Strings {
        String AddonName = "ChatMod";
        String AddonId = "chatmod";
    }
}
