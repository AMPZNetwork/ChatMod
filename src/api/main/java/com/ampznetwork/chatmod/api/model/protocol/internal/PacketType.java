package com.ampznetwork.chatmod.api.model.protocol.internal;

import com.ampznetwork.chatmod.api.model.config.format.Formats;
import com.ampznetwork.libmod.api.entity.Player;
import com.mineaurion.aurionchat.api.AurionPacket;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.comroid.api.attr.Named;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

import static net.kyori.adventure.text.Component.*;

@Getter
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public enum PacketType implements Named {
    CHAT(AurionPacket.Type.CHAT) {
        @Override
        public String getFormat(Formats format) {
            return format == null ? null : format.getMessageContent();
        }

        @Override
        public TextComponent createDefaultText(Player player, @NotNull String detail) {
            return text("<").append(text(player.getName())).append(text("> ")).append(text(detail));
        }
    }, JOIN(AurionPacket.Type.EVENT_JOIN) {
        @Override
        public String getFormat(Formats format) {
            return format == null ? null : format.getJoinMessage();
        }

        @Override
        public TextComponent createDefaultText(Player player, @Nullable String $) {
            return text(player.getName()).append(text(" joined the game")).color(NamedTextColor.YELLOW);
        }
    }, LEAVE(AurionPacket.Type.EVENT_JOIN) {
        @Override
        public String getFormat(Formats format) {
            return format == null ? null : format.getLeaveMessage();
        }

        @Override
        public TextComponent createDefaultText(Player player, @Nullable String $) {
            return text(player.getName()).append(text(" left the game")).color(NamedTextColor.YELLOW);
        }
    };

    public static PacketType of(AurionPacket.Type type) {
        return Arrays.stream(values()).filter(any -> any.aurionPacketType == type).findAny().orElseThrow();
    }

    AurionPacket.Type aurionPacketType;

    @Contract("null -> null; !null -> !null")
    public abstract String getFormat(Formats format);

    public abstract TextComponent createDefaultText(Player player, String detail);
}
