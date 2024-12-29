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
        @Nullable String        discordWebhookUrl;
        @Nullable String        discordInviteUrl;
        @Nullable CustomFormats format;
    }

    @Value
    public static class CustomFormats {
        public static final String DEFAULT_DISCORD_JOINMESSAGE  = "> %player_name% has joined %server_name%";
        public static final String DEFAULT_DISCORD_LEAVEMESSAGE = "> %player_name% has left %server_name%";
        public static final String DEFAULT_WEBHOOK_USERNAME     = "%player_name%";
        public static final String DEFAULT_WEBHOOK_MESSAGE      = "%message%";
        public static final String DEFAULT_WEBHOOK_AVATAR       = "https://mc-heads.net/avatar/%%player_name%%";
        @Nullable String discordJoinMessage;
        @Nullable String discordLeaveMessage;
        @Nullable String webhookUsername;
        @Nullable String webhookMessage;
        @Nullable String webhookAvatar;
    }
}
