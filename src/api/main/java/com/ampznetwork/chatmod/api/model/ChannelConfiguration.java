package com.ampznetwork.chatmod.api.model;

import com.ampznetwork.chatmod.api.ChatMod;
import com.ampznetwork.libmod.api.entity.Player;
import lombok.Builder;
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
    @lombok.Builder.Default @Nullable String    alias      = null;
    @lombok.Builder.Default @Nullable String    permission = null;
    @lombok.Builder.Default           boolean   publish    = true;
    @lombok.Builder.Default           Set<UUID> playerIDs  = new HashSet<>();
    @lombok.Builder.Default           Set<UUID> spyIDs     = new HashSet<>();

    public ChatMessage formatMessage(ChatMod mod, Player sender, String message) {
        var msg = new ChatMessage(sender, sender.getName(), message, message, Component.text(message));
        mod.getFormatter().accept(mod, msg);
        return msg;
    }
}
