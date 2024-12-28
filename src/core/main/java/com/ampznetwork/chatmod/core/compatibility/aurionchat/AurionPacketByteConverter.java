package com.ampznetwork.chatmod.core.compatibility.aurionchat;

import com.ampznetwork.chatmod.core.model.PacketByteConverter;
import com.google.gson.Gson;
import com.mineaurion.aurionchat.api.AurionPacket;

public class AurionPacketByteConverter extends PacketByteConverter<AurionPacket> {
    public AurionPacketByteConverter() {
        super(new Gson(), AurionPacket.class);
    }
}
