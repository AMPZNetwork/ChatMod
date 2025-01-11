package com.ampznetwork.chatmod.core.module.rabbit;

import com.ampznetwork.chatmod.api.model.config.ChatModules;
import com.ampznetwork.chatmod.api.model.module.ModuleContainer;
import com.ampznetwork.chatmod.api.model.protocol.ChatMessagePacket;
import lombok.Value;
import lombok.experimental.NonFinal;

@Value
@NonFinal
public abstract class IdentityRabbitMqModule<C extends ChatModules.RabbitMqProviderConfig> extends AbstractRabbitMqModule<C, ChatMessagePacket> {
    public IdentityRabbitMqModule(ModuleContainer mod, C config) {
        super(mod, config);
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
