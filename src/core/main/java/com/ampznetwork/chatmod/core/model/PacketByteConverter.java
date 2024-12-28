package com.ampznetwork.chatmod.core.model;

import com.ampznetwork.chatmod.api.ChatMod;
import com.google.gson.Gson;
import lombok.AccessLevel;
import lombok.Value;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import org.comroid.api.ByteConverter;

import java.nio.charset.StandardCharsets;

@Value
@NonFinal
@FieldDefaults(level = AccessLevel.PROTECTED, makeFinal = true)
public abstract class PacketByteConverter<P> implements ByteConverter<P> {
    ChatMod  mod;
    Gson     gson;
    Class<P> packetType;

    @Override
    public byte[] toBytes(P it) {
        return gson.toJson(it).getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public P fromBytes(byte[] bytes) {
        var string = new String(bytes, StandardCharsets.UTF_8);
        return gson.fromJson(string, packetType);
    }
}
