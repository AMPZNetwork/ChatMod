package com.ampznetwork.chatmod.lite.model.abstr;

import org.comroid.api.net.Rabbit;

public interface ChatModConfig {
    String SYSTEM_CHANNEL_NAME = "system";

    String getServerName();

    Rabbit getRabbit();

    String getFormattingScheme();

    default boolean isCompatibilityMode() {
        return false;
    }
}
