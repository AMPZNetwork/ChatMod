package com.ampznetwork.chatmod.api.model.config.discord;

import com.ampznetwork.chatmod.api.model.config.ChatModules;
import com.ampznetwork.chatmod.api.model.config.format.Formats;
import lombok.Builder.Default;
import lombok.Value;
import lombok.experimental.SuperBuilder;
import org.comroid.api.attr.Named;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.beans.ConstructorProperties;
import java.util.Objects;

@Value
@SuperBuilder
public class DiscordChannel extends ChatModules.FormatProviderConfig implements Named {
    @NotNull String name;
    @NotNull           Long   channelId;
    @Nullable @Default String webhookUrl = null;
    @Nullable @Default String inviteUrl  = null;

    @ConstructorProperties({ "format", "name", "channelId", "webhookUrl", "inviteUrl" })
    public DiscordChannel(@Nullable Formats format, @NotNull String name, @NotNull Long channelId, @Nullable String webhookUrl, @Nullable String inviteUrl) {
        super(format);
        this.name       = name;
        this.channelId  = channelId;
        this.webhookUrl = webhookUrl;
        this.inviteUrl  = inviteUrl;
    }

    public @NotNull Formats getFormat() {
        return Objects.requireNonNullElseGet(format, () -> Formats.DEFAULT);
    }

    @Override
    public String providerType() {
        return "discordChannel";
    }
}
