package com.ampznetwork.chatmod.api.model.protocol.io;

public interface BidirectionalPacketStream<P> extends InboundPacketStream<P>, OutboundPacketStream<P> {
}
