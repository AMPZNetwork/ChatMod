package com.ampznetwork.chatmod.core.compatibility.builtin;

import com.ampznetwork.chatmod.api.model.ChatMessagePacket;
import com.ampznetwork.chatmod.api.model.ChatModCompatibilityLayerAdapter;
import com.ampznetwork.chatmod.core.model.PacketByteConverter;
import com.ampznetwork.chatmod.core.serialization.ChatMessagePacketTypeAdapter;
import com.google.gson.GsonBuilder;
import lombok.Value;

@Value
public class ChatMessagePacketByteConverter extends PacketByteConverter<ChatMessagePacket> {
    public ChatMessagePacketByteConverter(ChatModCompatibilityLayerAdapter mod) {
        super(new GsonBuilder()
                .registerTypeAdapter(ChatMessagePacket.class, new ChatMessagePacketTypeAdapter(mod))
                .create(), ChatMessagePacket.class);
    }
}
