package com.ampznetwork.chatmod.api.model.protocol;

import com.ampznetwork.chatmod.lite.model.abstr.ChatModConfig;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
public final class ChatMessagePacket {
    private PacketType          packetType;
    private String              source;
    private String              channel;
    private ChatMessage         message;
    private List<String>        route;
    private List<String>        recipients;
    private Map<String, String> context;

    public ChatMessagePacket(PacketType packetType, String source, String channel, ChatMessage message) {
        this(packetType, source, channel, message, new ArrayList<>());
    }

    /**
     * constructor with route for auto-serialization
     */
    public ChatMessagePacket(
            PacketType packetType, String source, String channel, ChatMessage message, List<String> route) {
        this.packetType = packetType;
        this.source     = source;
        this.channel    = channel;
        this.message    = message;
        this.route      = route == null ? new ArrayList<>() : route;
    }

    @JsonIgnore
    public boolean isSystem() {
        return ChatModConfig.SYSTEM_CHANNEL_NAME.equals(channel);
    }
}
