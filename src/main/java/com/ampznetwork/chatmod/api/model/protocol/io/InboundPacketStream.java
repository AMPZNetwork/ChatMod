package com.ampznetwork.chatmod.api.model.protocol.io;

public interface InboundPacketStream<PKT> extends PacketStream {
    /**
     * @return whether an inbound packet should be accepted by this provider; *regardless* of config or availability
     */
    default boolean acceptInbound(PKT packet) {
        return true;
    }

    /**
     * this method should be implemented as a listener for incoming messages
     */
    void relayInbound(PKT packet);
}
