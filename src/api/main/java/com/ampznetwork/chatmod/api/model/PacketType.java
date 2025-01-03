package com.ampznetwork.chatmod.api.model;

import com.ampznetwork.chatmod.api.ChatMod;
import com.ampznetwork.libmod.api.entity.Player;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.comroid.api.attr.Named;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static net.kyori.adventure.text.Component.*;

public enum PacketType implements Named {
    CHAT {
        @Override
        public String getCustomFormat(ChatMod mod) {
            return mod.getFormatter().getFormat();
        }

        @Override
        public TextComponent createDefaultText(Player player, @NotNull String detail) {
            return text("<")
                    .append(text(player.getName()))
                    .append(text("> "))
                    .append(text(detail));
        }
    },
    JOIN {
        @Override
        public String getCustomFormat(ChatMod mod) {
            return mod.getCustomJoinMessageFormat();
        }

        @Override
        public TextComponent createDefaultText(Player player, @Nullable String $) {
            return text(player.getName()).append(text(" joined the game")).color(NamedTextColor.YELLOW);
        }
    },
    LEAVE {
        @Override
        public String getCustomFormat(ChatMod mod) {
            return mod.getCustomLeaveMessageFormat();
        }

        @Override
        public TextComponent createDefaultText(Player player, @Nullable String $) {
            return text(player.getName()).append(text(" left the game")).color(NamedTextColor.YELLOW);
        }
    };

    public abstract String getCustomFormat(ChatMod mod);

    public abstract TextComponent createDefaultText(Player player, String detail);
}
