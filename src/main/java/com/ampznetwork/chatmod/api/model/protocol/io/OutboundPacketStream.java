package com.ampznetwork.chatmod.api.model.protocol.io;

public interface OutboundPacketStream<PKT> extends PacketStream {
    /**
     * @return whether an outbound packet should be accepted by this provider; *regardless* of config or availability
     */
    default boolean acceptOutbound(PKT packet) {
        return true;
    }

    /**
     * this method should be called by message listeners underlying to this implementation
     */
    void relayOutbound(PKT packet);
}
