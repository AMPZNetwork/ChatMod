package com.ampznetwork.chatmod.spigot.serialization;

import com.ampznetwork.chatmod.api.ChatMod;
import com.ampznetwork.chatmod.api.model.ChatMessagePacket;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.Value;
import org.comroid.api.ByteConverter;

import java.nio.charset.StandardCharsets;

@Value
public class ChatMessagePacketByteConverter implements ByteConverter<ChatMessagePacket> {
    Gson gson;

    public ChatMessagePacketByteConverter(ChatMod mod) {
        var typeAdapter = new ChatMessagePacketTypeAdapter(mod);
        this.gson = new GsonBuilder()
                .registerTypeAdapter(ChatMessagePacket.class, typeAdapter)
                .create();
    }

    @Override
    public byte[] toBytes(ChatMessagePacket it) {
        return gson.toJson(it).getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public ChatMessagePacket fromBytes(byte[] bytes) {
        return gson.fromJson(new String(bytes, StandardCharsets.UTF_8), ChatMessagePacket.class);
    }
}
