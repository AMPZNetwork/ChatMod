package com.ampznetwork.chatmod.discord.config;

import lombok.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

@Value
public class DiscordChannelMapping {
    String gameChannelName;
    long   discordChannelId;
    @Nullable String  discordWebhookUrl;
    @Nullable String  discordInviteUrl;
    @Nullable Formats format;

    public @NotNull Formats getFormat() {
        return Objects.requireNonNullElseGet(format, Formats::new);
    }
}
