package com.ampznetwork.chatmod.api.model;

import lombok.Value;
import org.comroid.api.ByteConverter;

@Value
public class ChatMessagePacket {
    public static final ByteConverter<ChatMessagePacket> CONVERTER = ByteConverter.jackson(ChatMessagePacket.class);
    String source;
    String channel;
    ChatMessage message;
}
