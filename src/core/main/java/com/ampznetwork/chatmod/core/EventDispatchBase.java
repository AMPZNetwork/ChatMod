package com.ampznetwork.chatmod.core;

import com.ampznetwork.chatmod.api.ChatMod;
import com.ampznetwork.chatmod.api.formatting.MessageFormatter;
import com.ampznetwork.chatmod.api.model.ChatMessage;
import com.ampznetwork.chatmod.api.model.ChatMessagePacket;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import lombok.Value;
import lombok.experimental.NonFinal;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Value
@NonFinal
public abstract class EventDispatchBase<Mod extends ChatMod> {
    protected Mod mod;
    protected MessageFormatter[] formatterChain;

    @SneakyThrows
    protected void dispatch(ChatMessage message) {
        for (var formatter : formatterChain)
            formatter.accept(message);

        var channel = "global"; // todo

        var packet = new ChatMessagePacket(mod.getServerName(), channel, message);
        var str = new ObjectMapper().writeValueAsString(packet);
        mod.getRabbit().send(packet);
    }
}
