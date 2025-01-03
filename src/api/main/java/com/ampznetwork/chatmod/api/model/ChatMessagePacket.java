package com.ampznetwork.chatmod.api.model;

import lombok.Value;

import java.util.ArrayList;
import java.util.List;

@Value
public class ChatMessagePacket {
    MessageType  type;
    String       source;
    String       channel;
    ChatMessage  message;
    List<String> route;

    public ChatMessagePacket(MessageType type, String source, String channel, ChatMessage message) {
        this(type, source, channel, message, new ArrayList<>());
    }

    /**
     * constructor with route for auto-serialization
     */
    public ChatMessagePacket(MessageType type, String source, String channel, ChatMessage message, List<String> route) {
        this.type    = type;
        this.source  = source;
        this.channel = channel;
        this.message = message;
        this.route   = route == null ? new ArrayList<>() : route;
    }
}
