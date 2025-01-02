package com.ampznetwork.chatmod.api.model;

import lombok.Value;

import java.util.List;

@Value
public class ChatMessagePacket {
    MessageType  type;
    String       source;
    String       channel;
    ChatMessage  message;
    List<String> route;
}
