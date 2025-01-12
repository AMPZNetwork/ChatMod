package com.ampznetwork.chatmod.api.model.protocol.internal;

import com.ampznetwork.chatmod.api.model.config.format.Formats;
import com.ampznetwork.libmod.api.entity.Player;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.comroid.api.attr.Named;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static net.kyori.adventure.text.Component.*;

public enum PacketType implements Named {
    CHAT {
        @Override
        public String getFormat(Formats format) {
            return format == null ? null : format.getMessageContent();
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
        public String getFormat(Formats format) {
            return format == null ? null : format.getJoinMessage();
        }

        @Override
        public TextComponent createDefaultText(Player player, @Nullable String $) {
            return text(player.getName()).append(text(" joined the game")).color(NamedTextColor.YELLOW);
        }
    },
    LEAVE {
        @Override
        public String getFormat(Formats format) {
            return format == null ? null : format.getLeaveMessage();
        }

        @Override
        public TextComponent createDefaultText(Player player, @Nullable String $) {
            return text(player.getName()).append(text(" left the game")).color(NamedTextColor.YELLOW);
        }
    };

    @Contract("null -> null; !null -> !null")
    public abstract String getFormat(Formats format);

    public abstract TextComponent createDefaultText(Player player, String detail);
}
