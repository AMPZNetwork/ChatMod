package com.ampznetwork.chatmod.core.module.impl;

import com.ampznetwork.chatmod.api.model.config.ChatModules;
import com.ampznetwork.chatmod.api.model.module.ModuleContainer;
import com.ampznetwork.chatmod.api.model.protocol.ChatMessagePacket;
import com.ampznetwork.chatmod.core.module.rabbit.IdentityRabbitMqModule;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.Value;
import org.comroid.api.ByteConverter;

@Value
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class LinkToNativeRabbitModule extends IdentityRabbitMqModule<ChatModules.NativeProviderConfig>
        implements IIdentityModule<ChatModules.NativeProviderConfig> {
    public static final ObjectMapper MAPPER = new ObjectMapper() {{
        configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }};

    public LinkToNativeRabbitModule(ModuleContainer mod, ChatModules.NativeProviderConfig config) {
        super(mod, config);
    }

    @Override
    public ByteConverter<ChatMessagePacket> createByteConverter() {
        return new ChatMessagePacket.JacksonByteConverter(MAPPER);
    }

    @Override
    public int priority() {
        return 0;
    }
}
