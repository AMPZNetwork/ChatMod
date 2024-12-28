package com.ampznetwork.chatmod.core.serialization;

import com.ampznetwork.chatmod.api.ChatMod;
import com.ampznetwork.chatmod.api.model.ChatMessagePacket;
import com.ampznetwork.chatmod.core.model.PacketByteConverter;
import com.google.gson.GsonBuilder;
import lombok.Value;

@Value
public class ChatMessagePacketByteConverter extends PacketByteConverter<ChatMessagePacket> {
    public ChatMessagePacketByteConverter(ChatMod mod) {
        super(mod, new GsonBuilder()
                .registerTypeAdapter(ChatMessagePacket.class, new ChatMessagePacketTypeAdapter(mod))
                .create(), ChatMessagePacket.class);
    }
}
