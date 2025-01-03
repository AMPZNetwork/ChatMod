package com.ampznetwork.chatmod.core.compatibility.builtin;

import com.ampznetwork.chatmod.api.model.ChatMessagePacket;
import com.ampznetwork.chatmod.api.model.ChatModCompatibilityLayerAdapter;
import com.ampznetwork.chatmod.api.model.CompatibilityLayer;
import com.ampznetwork.chatmod.core.compatibility.RabbitMqCompatibilityLayer;
import com.ampznetwork.chatmod.core.model.ChatMessagePacketImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.EqualsAndHashCode;
import lombok.SneakyThrows;
import lombok.Value;
import org.comroid.api.ByteConverter;

import java.io.ByteArrayInputStream;
import java.util.function.Predicate;

@Value
@EqualsAndHashCode(callSuper = true)
public class DefaultCompatibilityLayer extends RabbitMqCompatibilityLayer<ChatMessagePacket> {
    public static final ObjectMapper MAPPER = new ObjectMapper();

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
    public boolean isEnabled() {
        return true;
    }

    @Override
    public ByteConverter<ChatMessagePacket> createByteConverter() {
        return new ByteConverter<>() {
            @Override
            @SneakyThrows
            public byte[] toBytes(ChatMessagePacket packet) {
                return MAPPER.writeValueAsBytes(packet);
            }

            @Override
            @SneakyThrows
            public ChatMessagePacket fromBytes(byte[] bytes) {
                return MAPPER.readValue(new ByteArrayInputStream(bytes), ChatMessagePacketImpl.class);
            }
        };
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

    @Override
    public void handle(ChatMessagePacket packet) {
        // loop from here to here
        if (mod.getSourceName().equals(packet.getSource())) mod.relayInbound(packet);

        // relay to registered aurionchat servers
        children(CompatibilityLayer.class).filter(Predicate.not(this::equals)).forEach(layer -> layer.send(packet));
    }
}
