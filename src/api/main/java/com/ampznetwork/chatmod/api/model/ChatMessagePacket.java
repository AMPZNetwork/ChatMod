package com.ampznetwork.chatmod.api.model;

import lombok.Value;

@Value
public class ChatMessagePacket {
    MessageType type;
    String      source;
    String      channel;
    ChatMessage message;
}
