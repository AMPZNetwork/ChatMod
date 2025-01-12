package com.ampznetwork.chatmod.api.model.config.format;

import com.ampznetwork.chatmod.api.model.formatting.DefaultPlaceholder;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import lombok.experimental.FieldDefaults;
import org.comroid.api.attr.Named;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Objects;

@Value
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
public class Formats implements IFormats {
    public static final Formats  DEFAULT           = new Formats();
    @NotNull @Default   IFormats defaults          = Defaults.MINECRAFT_CHAT;
    @Nullable @Default  String   joinMessage       = null;
    @Nullable @Default  String   leaveMessage      = null;
    @Nullable @Default  String   messageAuthor     = null;
    @Nullable @Default  String   messageContent    = null;
    @Nullable @Default  String   messageUserAvatar = null;

    @JsonIgnore
    public @NotNull IFormats getDefaults() {
        return defaults;
    }

    @Override
    public @NotNull String getMessageAuthor() {
        return Objects.requireNonNullElse(messageAuthor, defaults.getMessageAuthor());
    }

    @Override
    public @NotNull String getMessageContent() {
        return Objects.requireNonNullElse(messageContent, defaults.getMessageContent());
    }

    @Override
    public @NotNull String getMessageUserAvatar() {
        return Objects.requireNonNullElse(messageUserAvatar, defaults.getMessageUserAvatar());
    }

    @Getter
    @RequiredArgsConstructor
    @FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
    public enum Defaults implements IFormats, Named {
        MINECRAFT_CHAT("&2&l+ %player_name%&a has joined %server_name%",
                "&4&l- %player_name%&c has left %server_name%",
                "%player_displayname%",
                "&7[%server_name%&7] &f%player_displayname%&f: %message%",
                "https://mc-heads.net/avatar/%player_name%"),
        DISCORD_WEBHOOK("> %player_name% has joined %server_name%",
                "> %player_name% has left %server_name%",
                "%player_displayname%",
                "%message%",
                "https://mc-heads.net/avatar/%player_name%");
        @NotNull String joinMessage;
        @NotNull String leaveMessage;
        @NotNull String messageAuthor;
        @NotNull String messageContent;
        @NotNull String messageUserAvatar;
    }

    @Value
    public class Wrapper {
        String                         format;
        Collection<DefaultPlaceholder> placeholders;
    }
}
