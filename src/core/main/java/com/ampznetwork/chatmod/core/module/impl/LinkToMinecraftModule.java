package com.ampznetwork.chatmod.core.module.impl;

import com.ampznetwork.chatmod.api.model.config.ChatModules;
import com.ampznetwork.chatmod.api.model.config.channel.Channel;
import com.ampznetwork.chatmod.api.model.module.ModuleContainer;
import com.ampznetwork.chatmod.api.model.protocol.ChatMessagePacket;
import com.ampznetwork.chatmod.core.module.IdentityModule;
import com.ampznetwork.libmod.api.SubMod;
import lombok.EqualsAndHashCode;
import lombok.Value;

@Value
@EqualsAndHashCode(callSuper = true)
public class LinkToMinecraftModule extends IdentityModule<ChatModules.MinecraftProviderConfig> implements IIdentityModule<ChatModules.NativeProviderConfig> {
    public LinkToMinecraftModule(ModuleContainer mod, ChatModules.MinecraftProviderConfig config) {
        super(mod, config);
    }

    @Override
    public boolean isAvailable() {
        return mod.wrapLib().isPresent();
    }

    @Override
    public void relayInbound(ChatMessagePacket packet) {
        var text = packet.getMessage().getFullText();

        mod.wrapLib()
                .map(SubMod::getPlayerAdapter)
                .ifPresent(adp -> mod.getChannels()
                        .stream()
                        .filter(channel -> packet.getChannel().equals(channel.getName()))
                        .flatMap(Channel::allPlayerIDs)
                        .forEach(playerId -> adp.send(playerId, text)));
    }

    @Override
    public void relayOutbound(ChatMessagePacket packet) {
        broadcastInbound(packet);
    }
}
