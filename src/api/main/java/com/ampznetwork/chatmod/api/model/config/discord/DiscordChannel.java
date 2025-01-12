package com.ampznetwork.chatmod.api.model.config.discord;

import com.ampznetwork.chatmod.api.model.config.ChatModules;
import com.ampznetwork.chatmod.api.model.config.format.Formats;
import lombok.Builder.Default;
import lombok.Value;
import lombok.experimental.SuperBuilder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

@Value
@SuperBuilder
public class DiscordChannel extends ChatModules.FormatProviderConfig {
    @NotNull           Long   channelId;
    @Nullable @Default String webhookUrl = null;
    @Nullable @Default String inviteUrl  = null;

    public @NotNull Formats getFormat() {
        return Objects.requireNonNullElseGet(format, () -> Formats.DEFAULT);
    }

    @Override
    public String providerType() {
        return "discordChannel";
    }
}
