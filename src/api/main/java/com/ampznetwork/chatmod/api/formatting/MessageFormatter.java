package com.ampznetwork.chatmod.api.formatting;

import com.ampznetwork.chatmod.api.ChatMod;
import com.ampznetwork.chatmod.api.model.ChatMessage;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

public interface MessageFormatter extends BiConsumer<ChatMod, ChatMessage> {
}
