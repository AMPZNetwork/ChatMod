package com.ampznetwork.chatmod.core.module.impl;

import com.ampznetwork.chatmod.api.model.config.ChatModules;
import com.ampznetwork.chatmod.api.model.module.ModuleContainer;
import com.ampznetwork.chatmod.api.model.protocol.ChatMessagePacket;
import com.ampznetwork.chatmod.api.model.protocol.internal.ChatMessagePacketImpl;
import com.ampznetwork.chatmod.core.module.rabbit.IdentityRabbitMqModule;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.EqualsAndHashCode;
import lombok.SneakyThrows;
import lombok.ToString;
import lombok.Value;
import org.comroid.api.ByteConverter;

import java.io.ByteArrayInputStream;

@Value
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class LinkToNativeRabbitModule extends IdentityRabbitMqModule<ChatModules.NativeProviderConfig>
        implements IIdentityModule<ChatModules.NativeProviderConfig> {
    public static final ObjectMapper MAPPER = new ObjectMapper();

    public LinkToNativeRabbitModule(ModuleContainer mod, ChatModules.NativeProviderConfig config) {
        super(mod, config);
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
    public int priority() {
        return 0;
    }
}
