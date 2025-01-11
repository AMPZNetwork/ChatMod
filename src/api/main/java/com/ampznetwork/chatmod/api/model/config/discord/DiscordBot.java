package com.ampznetwork.chatmod.api.model.config.discord;

import org.jetbrains.annotations.NotNull;

public interface DiscordBot extends IFormatContext {
    @NotNull String getDiscordToken();
}
