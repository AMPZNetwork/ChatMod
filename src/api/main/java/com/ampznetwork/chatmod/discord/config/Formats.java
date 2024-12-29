package com.ampznetwork.chatmod.discord.config;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.NoArgsConstructor;
import lombok.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

@Value
@Builder
@AllArgsConstructor
@NoArgsConstructor(force = true)
@SuppressWarnings("DataFlowIssue"/* false-positive */)
public class Formats {
    public static final String DEFAULT_JOIN_MESSAGE     = "> %player_name% has joined %server_name%";
    public static final String DEFAULT_LEAVE_MESSAGE    = "> %player_name% has left %server_name%";
    public static final String DEFAULT_WEBHOOK_USERNAME = "%player_name%";
    public static final String DEFAULT_WEBHOOK_MESSAGE  = "%message%";
    public static final String DEFAULT_WEBHOOK_AVATAR   = "https://mc-heads.net/avatar/%player_name%";
    @Nullable @Default  String joinMessage              = null;
    @Nullable @Default  String leaveMessage             = null;
    @Nullable @Default  String webhookUsername          = null;
    @Nullable @Default  String webhookMessage           = null;
    @Nullable @Default  String webhookAvatar            = null;

    public @NotNull String getDiscordJoinMessage() {
        return Objects.requireNonNullElse(joinMessage, DEFAULT_JOIN_MESSAGE);
    }

    public @NotNull String getDiscordLeaveMessage() {
        return Objects.requireNonNullElse(leaveMessage, DEFAULT_LEAVE_MESSAGE);
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
