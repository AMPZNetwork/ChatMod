package com.ampznetwork.chatmod.core.model;

import com.ampznetwork.chatmod.api.model.ChatMessage;
import com.ampznetwork.chatmod.api.model.ChatMessagePacket;
import com.ampznetwork.chatmod.api.model.PacketType;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
public final class ChatMessagePacketImpl implements ChatMessagePacket {
    @JsonProperty PacketType   packetType;
    @JsonProperty String       source;
    @JsonProperty String       channel;
    @JsonProperty ChatMessage  message;
    @JsonProperty List<String> route;

    public ChatMessagePacketImpl(PacketType packetType, String source, String channel, ChatMessage message) {
        this(packetType, source, channel, message, new ArrayList<>());
    }

    /**
     * constructor with route for auto-serialization
     */
    public ChatMessagePacketImpl(PacketType packetType, String source, String channel, ChatMessage message, List<String> route) {
        this.packetType = packetType;
        this.source     = source;
        this.channel    = channel;
        this.message    = message;
        this.route      = route == null ? new ArrayList<>() : route;
    }
}
