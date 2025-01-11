package com.ampznetwork.chatmod.core.module;

import com.ampznetwork.chatmod.api.model.config.ChatModules;
import com.ampznetwork.chatmod.api.model.module.ModuleContainer;
import com.ampznetwork.chatmod.api.model.protocol.ChatMessagePacket;
import lombok.Value;
import lombok.experimental.NonFinal;

@Value
@NonFinal
public abstract class IdentityModule<C extends ChatModules.ProviderConfig> extends AbstractModule<C, ChatMessagePacket> {
    public IdentityModule(ModuleContainer mod, C config, Object... children) {
        super(mod, config, children);
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
