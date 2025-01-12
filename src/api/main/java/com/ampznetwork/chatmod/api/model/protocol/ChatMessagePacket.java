package com.ampznetwork.chatmod.api.model.protocol;

import com.ampznetwork.chatmod.api.model.module.Module;
import com.ampznetwork.chatmod.api.model.module.ModuleContainer;
import com.ampznetwork.chatmod.api.model.protocol.internal.PacketType;
import com.fasterxml.jackson.annotation.JsonProperty;

public interface ChatMessagePacket {
    @JsonProperty
    PacketType getPacketType();

    /**
     * @return name of the source server of this packet
     * @see ModuleContainer#getServerName()
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
}
