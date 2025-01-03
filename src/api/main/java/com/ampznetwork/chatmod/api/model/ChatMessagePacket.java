package com.ampznetwork.chatmod.api.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public interface ChatMessagePacket {
    @JsonProperty
    PacketType getPacketType();

    @JsonProperty
    String getSource();

    @JsonProperty
    String getChannel();

    @JsonProperty
    ChatMessage getMessage();

    @JsonProperty
    java.util.List<String> getRoute();
}
