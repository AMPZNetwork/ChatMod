package com.ampznetwork.chatmod.core.module.impl;

import com.ampznetwork.chatmod.api.model.config.ChatModules;
import com.ampznetwork.chatmod.api.model.module.ModuleContainer;
import com.ampznetwork.chatmod.api.model.protocol.ChatMessagePacket;
import com.ampznetwork.chatmod.core.module.IdentityModule;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.Value;
import org.comroid.api.func.util.Debug;
import org.comroid.api.info.Log;

import java.util.logging.Level;

@Value
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class LinkToLogModule extends IdentityModule<ChatModules.LogProviderConfig> {
    public static final ObjectMapper MAPPER = new ObjectMapper();

    public LinkToLogModule(ModuleContainer mod, ChatModules.LogProviderConfig config) {
        super(mod, config);
    }

    @Override
    public boolean isEnabled() {
        return super.isEnabled() || Debug.isDebug();
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    @Override
    public void relayOutbound(ChatMessagePacket packet) {}

    @Override
    public void relayInbound(ChatMessagePacket packet) {
        Log.get("Chat #" + packet.getChannel()).log(Level.INFO, packet.getMessage().getPlaintext());
    }
}
