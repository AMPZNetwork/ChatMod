package com.ampznetwork.chatmod.discord.config;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

@Value
@AllArgsConstructor
@NoArgsConstructor(force = true)
public class Formats {
    public static final String DEFAULT_DISCORD_JOIN_MESSAGE  = "> %player_name% has joined %server_name%";
    public static final String DEFAULT_DISCORD_LEAVE_MESSAGE = "> %player_name% has left %server_name%";
    public static final String DEFAULT_WEBHOOK_USERNAME      = "%player_name%";
    public static final String DEFAULT_WEBHOOK_MESSAGE       = "%message%";
    public static final String DEFAULT_WEBHOOK_AVATAR        = "https://mc-heads.net/avatar/%player_name%";
    @Nullable           String discordJoinMessage;
    @Nullable           String discordLeaveMessage;
    @Nullable           String webhookUsername;
    @Nullable           String webhookMessage;
    @Nullable           String webhookAvatar;

    public @NotNull String getDiscordJoinMessage() {
        return Objects.requireNonNullElse(discordJoinMessage, DEFAULT_DISCORD_JOIN_MESSAGE);
    }

    public @NotNull String getDiscordLeaveMessage() {
        return Objects.requireNonNullElse(discordLeaveMessage, DEFAULT_DISCORD_LEAVE_MESSAGE);
    }

    public @NotNull String getWebhookUsername() {
        return Objects.requireNonNullElse(webhookUsername, DEFAULT_WEBHOOK_USERNAME);
    }

    public @NotNull String getWebhookMessage() {
        return Objects.requireNonNullElse(webhookMessage, DEFAULT_WEBHOOK_MESSAGE);
    }

    public @NotNull String getWebhookAvatar() {
        return Objects.requireNonNullElse(webhookAvatar, DEFAULT_WEBHOOK_AVATAR);
    }
}
