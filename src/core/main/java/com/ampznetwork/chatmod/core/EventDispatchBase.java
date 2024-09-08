package com.ampznetwork.chatmod.core;

import com.ampznetwork.chatmod.api.ChatMod;
import com.ampznetwork.chatmod.api.formatting.MessageFormatter;
import com.ampznetwork.chatmod.api.model.ChannelConfiguration;
import com.ampznetwork.chatmod.api.model.ChatMessage;
import com.ampznetwork.chatmod.api.model.ChatMessagePacket;
import lombok.SneakyThrows;
import lombok.Value;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;

import java.util.UUID;

@Slf4j
@Value
@NonFinal
public abstract class EventDispatchBase<Mod extends ChatMod> {
    protected Mod                mod;
    protected MessageFormatter[] formatterChain;

    @SneakyThrows
    protected void dispatch(ChatMessage message) {
        var playerId = message.getSender().getId();
        var optChannel = mod.getChannels().stream()
                .filter(channel -> channel.getPlayerIDs().contains(playerId))
                .findAny()
                .map(ChannelConfiguration::getName);

        if (optChannel.isEmpty()) {
            log.warn("Dropped message because player is not in any channel: {}", message);
            return;
        }

        for (var formatter : formatterChain)
            formatter.accept(message);

        var packet = new ChatMessagePacket(mod.getServerName(), optChannel.get(), message);
        mod.getRabbit().send(packet);
    }

    protected boolean playerJoin(UUID playerId) {
        return mod.getChannels().getFirst().getPlayerIDs().add(playerId);
    }

    protected void playerLeave(UUID playerId) {
        mod.getChannels().forEach(channel -> {
            channel.getPlayerIDs().remove(playerId);
            channel.getSpyIDs().remove(playerId);
        });
    }
}
