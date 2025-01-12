package com.ampznetwork.chatmod.core.module;

import com.ampznetwork.chatmod.api.model.config.ChatModules;
import com.ampznetwork.chatmod.api.model.module.ModuleContainer;
import com.ampznetwork.chatmod.api.model.protocol.ChatMessagePacket;
import lombok.ToString;
import lombok.Value;
import lombok.experimental.NonFinal;

@Value
@NonFinal
@ToString(callSuper = true)
public abstract class IdentityModule<C extends ChatModules.ProviderConfig> extends AbstractModule<C, ChatMessagePacket> {
    public IdentityModule(ModuleContainer mod, C config, Object... children) {
        super(mod, config, children);
    }

    @Override
    public ChatMessagePacket upgradeToNative(ChatMessagePacket packet) {
        return packet;
    }
}
