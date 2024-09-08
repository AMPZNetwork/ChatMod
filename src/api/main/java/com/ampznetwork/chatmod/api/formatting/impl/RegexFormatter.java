package com.ampznetwork.chatmod.api.formatting.impl;

import com.ampznetwork.chatmod.api.formatting.MessageFormatter;
import com.ampznetwork.chatmod.api.model.ChatMessage;
import lombok.Value;

@Value
public class RegexFormatter implements MessageFormatter {
    @Override
    public void accept(ChatMessage chatMessage) {

    }
}
