package com.ampznetwork.chatmod.api.model;

import lombok.Value;

@Value
public class ChatMessagePacket {
    String source;
    String channel;
    ChatMessage message;
}
