package com.ampznetwork.chatmod.discord.model;

import lombok.Value;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

@Value
public class Config {
    String                     discordToken;
    String                     rabbitMqUri;
    Set<DiscordChannelMapping> channels;

    @Value
    public static class DiscordChannelMapping {
        String gameChannelName;
        long   discordChannelId;
        @Nullable String discordWebhookUrl;
        @Nullable String discordInviteUrl;
    }
}
