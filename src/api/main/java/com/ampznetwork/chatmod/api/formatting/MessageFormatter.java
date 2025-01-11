package com.ampznetwork.chatmod.api.formatting;

import com.ampznetwork.chatmod.api.ChatMod;
import com.ampznetwork.chatmod.api.model.protocol.ChatMessage;

import java.util.function.BiConsumer;

public interface MessageFormatter extends BiConsumer<ChatMod, ChatMessage> {
    String getFormat();
}
