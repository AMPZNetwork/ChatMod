package com.ampznetwork.chatmod.api.model;

import com.ampznetwork.chatmod.api.ChatMod;
import com.ampznetwork.chatmod.discord.config.DiscordChannelConfig;
import com.ampznetwork.libmod.api.entity.Player;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Value;
import net.kyori.adventure.text.Component;
import org.comroid.api.attr.Named;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Value
@Builder
public class ChannelConfiguration implements Named {
    String name;
    @Default @Nullable String               alias      = null;
    @Default @Nullable String               permission = null;
    @Default @Nullable DiscordChannelConfig discord    = null;
    @Default           boolean              publish    = true;
    Set<UUID> playerIDs = new HashSet<>();
    Set<UUID> spyIDs    = new HashSet<>();

    public ChatMessage formatMessage(ChatMod mod, Player sender, String message) {
        var msg = new ChatMessage(sender, mod.getPlayerAdapter().getDisplayName(sender.getId()), message, Component.text(message));
        mod.getFormatter().accept(mod, msg);
        return msg;
    }
}
