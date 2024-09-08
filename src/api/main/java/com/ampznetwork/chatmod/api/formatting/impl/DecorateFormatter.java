package com.ampznetwork.chatmod.api.formatting.impl;

import com.ampznetwork.chatmod.api.formatting.MessageFormatter;
import com.ampznetwork.chatmod.api.model.ChatMessage;
import lombok.Builder;
import lombok.Value;

import java.util.Map;

@Value
@Builder
public class DecorateFormatter implements MessageFormatter {
    @lombok.Builder.Default String scheme = "<%username%> %message%";

    public static MessageFormatter of(Map<String, ?> config) {
        var builder = builder();
        if (config.get("scheme") instanceof String s) builder.scheme(s);
        return builder.build();
    }

    @Override
    public void accept(ChatMessage chatMessage) {

    }
}
