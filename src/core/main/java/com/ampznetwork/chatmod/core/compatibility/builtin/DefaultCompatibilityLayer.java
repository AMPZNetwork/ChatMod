package com.ampznetwork.chatmod.core.compatibility.builtin;

import com.ampznetwork.chatmod.api.ChatModCompatibilityLayerAdapter;
import com.ampznetwork.chatmod.api.model.ChatMessagePacket;
import com.ampznetwork.chatmod.core.compatibility.RabbitMqCompatibilityLayer;
import lombok.Value;
import org.comroid.api.ByteConverter;

@Value
public class DefaultCompatibilityLayer extends RabbitMqCompatibilityLayer<ChatMessagePacket> {
    public DefaultCompatibilityLayer(ChatModCompatibilityLayerAdapter mod) {
        super(mod);
    }

    @Override
    public String getUri() {
        return mod.getMainRabbitUri();
    }

    @Override
    public String getExchange() {
        return "minecraft.chat";
    }

    @Override
    public ByteConverter<ChatMessagePacket> createByteConverter() {
        return new ChatMessagePacketByteConverter(mod);
    }

    @Override
    public boolean isDefault() {
        return true;
    }

    @Override
    public ChatMessagePacket convertToChatModPacket(ChatMessagePacket packet) {
        return packet;
    }

    @Override
    public ChatMessagePacket convertToNativePacket(ChatMessagePacket packet) {
        return packet;
    }
}