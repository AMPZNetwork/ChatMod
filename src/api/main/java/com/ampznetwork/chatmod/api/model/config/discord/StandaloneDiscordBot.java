package com.ampznetwork.chatmod.api.model.config.discord;

import com.ampznetwork.chatmod.api.model.config.ChatModules;
import com.ampznetwork.chatmod.api.model.config.channel.Channels;
import com.ampznetwork.chatmod.api.model.config.format.Formats;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Singular;
import lombok.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Value
public class StandaloneDiscordBot implements DiscordBot {
    @NotNull                          String      discordToken;
    @NotNull                          String      rabbitMqUri;
    @NotNull                          ChatModules chatModules;
    @Nullable                         Formats     defaultFormat;
    @Singular                         Channels    channels;
    @JsonProperty(defaultValue = "5") int         autoReconnectDelay;
}
