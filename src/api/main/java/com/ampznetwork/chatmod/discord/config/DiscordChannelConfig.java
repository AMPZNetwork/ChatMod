package com.ampznetwork.chatmod.discord.config;

import lombok.Builder;
import lombok.Builder.Default;
import lombok.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

@Value
@Builder
public class DiscordChannelConfig {
    String channelName;
    @Nullable @Default Long    channelId  = null;
    @Nullable @Default String  webhookUrl = null;
    @Nullable @Default String  inviteUrl  = null;
    @Nullable @Default Formats format     = null;

    public @NotNull Formats getFormat() {
        return Objects.requireNonNullElseGet(format, Formats::new);
    }
}
