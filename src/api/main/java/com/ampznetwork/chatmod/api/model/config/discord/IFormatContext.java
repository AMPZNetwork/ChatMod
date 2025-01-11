package com.ampznetwork.chatmod.api.model.config.discord;

import com.ampznetwork.chatmod.api.model.config.format.Formats;
import org.jetbrains.annotations.Nullable;

public interface IFormatContext {
    @Nullable Formats getDefaultFormat();
}
