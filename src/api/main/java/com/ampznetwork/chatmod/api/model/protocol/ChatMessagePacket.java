package com.ampznetwork.chatmod.api.model.protocol;

import com.ampznetwork.chatmod.api.model.module.Module;
import com.ampznetwork.chatmod.api.model.protocol.internal.ChatMessagePacketImpl;
import com.ampznetwork.chatmod.api.model.protocol.internal.PacketType;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import lombok.Value;

import java.io.ByteArrayInputStream;

public interface ChatMessagePacket {
    @JsonProperty
    PacketType getPacketType();

    /**
     * @return name of the source server of this packet
     * @see com.ampznetwork.libmod.api.LibMod#getServerName()
     */
    @JsonProperty
    String getSource();

    @JsonProperty
    String getChannel();

    @JsonProperty
    ChatMessage getMessage();

    /**
     * @return list of endpoints that this packet has been routed through
     * @see Module#getEndpointName()
     */
    @JsonProperty
    java.util.List<String> getRoute();

    interface ByteConverter extends org.comroid.api.ByteConverter<ChatMessagePacket> {}

    @Value
    class JacksonByteConverter implements ByteConverter {
        ObjectMapper mapper;

        @Override
        @SneakyThrows
        public byte[] toBytes(ChatMessagePacket packet) {
            return mapper.writeValueAsBytes(packet);
        }

        @Override
        @SneakyThrows
        public ChatMessagePacket fromBytes(byte[] bytes) {
            return mapper.readValue(new ByteArrayInputStream(bytes), ChatMessagePacketImpl.class);
        }
    }
}
