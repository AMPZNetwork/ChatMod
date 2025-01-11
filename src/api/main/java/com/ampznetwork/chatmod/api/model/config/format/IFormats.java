package com.ampznetwork.chatmod.api.model.config.format;

import org.jetbrains.annotations.NotNull;

public interface IFormats {
    @NotNull String getMessageAuthor();

    @NotNull String getMessageContent();

    @NotNull String getMessageUserAvatar();

    String getJoinMessage();

    String getLeaveMessage();
}
