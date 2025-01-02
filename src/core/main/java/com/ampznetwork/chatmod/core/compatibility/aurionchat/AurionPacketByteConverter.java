package com.ampznetwork.chatmod.core.compatibility.aurionchat;

import com.ampznetwork.chatmod.core.model.PacketByteConverter;

public class AurionPacketByteConverter extends PacketByteConverter<AurionPacketAdapter> {
    public AurionPacketByteConverter() {
        super(AurionPacketAdapter.getGson(), AurionPacketAdapter.class);
    }
}
