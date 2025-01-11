package com.ampznetwork.chatmod.core;

import com.ampznetwork.chatmod.api.ChatMod;
import com.ampznetwork.chatmod.api.model.protocol.ChatMessage;
import com.ampznetwork.chatmod.api.model.protocol.internal.PacketType;
import com.ampznetwork.libmod.api.model.delegate.EventDelegate;
import lombok.SneakyThrows;
import lombok.Value;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.UUID;

import static com.ampznetwork.chatmod.core.formatting.ChatMessageFormatter.*;
import static net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer.*;

@Slf4j
@Value
@NonFinal
public abstract class EventDispatchBase<Mod extends ChatMod> {
    protected Mod mod;

    @SneakyThrows
    protected void dispatch(ChatMessage message) {
        var sender = message.getSender();
        assert sender != null : "Outbound from Minecraft should always have a Sender";

        var playerId = sender.getId();
        var optChannel = mod.getChannels().getChannels().stream()
                .filter(channel -> channel.getPlayerIDs().contains(playerId))
                .findAny();

        if (optChannel.isEmpty()) {
            log.warn("Dropped message because player is not in any channel: {}", message);
            return;
        }

        optChannel.get().send(mod, message);
    }

    protected void playerJoin(UUID playerId, @Nullable EventDelegate<TextComponent> event) {
        mod.getChannels().getChannels().getFirst().getPlayerIDs().add(playerId);
        handleJoinLeave(playerId, PacketType.JOIN, event);
    }

    protected void playerLeave(UUID playerId, @Nullable EventDelegate<TextComponent> event) {
        var channels = mod.getChannels().getChannels();
        channels.stream()
                .filter(channel -> channel.getPlayerIDs().contains(playerId))
                .findAny()
                .ifPresent(channel -> handleJoinLeave(playerId, PacketType.LEAVE, event));
        channels.forEach(channel -> {
            channel.getPlayerIDs().remove(playerId);
            channel.getSpyIDs().remove(playerId);
        });
    }

    private void handleJoinLeave(UUID playerId, PacketType packetType, @Nullable EventDelegate<TextComponent> event) {
        if (!mod.isJoinLeaveEnabled()) return;

        var player = mod.getPlayerAdapter().getPlayer(playerId).orElseThrow();
        var text = Optional.ofNullable(packetType.getCustomFormat(mod))
                .map(format -> format.replace(PLAYER_NAME_PLACEHOLDER, RESERVED_PLACEHOLDER))
                .map(format -> mod.applyPlaceholderApi(playerId, format))
                .map(format -> {
                    var split = format.split(RESERVED_PLACEHOLDER);
                    var txt = Component.text()
                            .append(legacyAmpersand().deserialize(split[0]))
                            .append(Component.text((mod.getPlayerAdapter().getDisplayName(playerId))));
                    if (split.length > 1)
                        txt.append(legacyAmpersand().deserialize(split[1]));
                    return txt.build();
                })
                .orElseGet(() -> packetType.createDefaultText(player, null));

        if ((mod.isListenerCompatibilityMode() || mod.isReplaceDefaultJoinLeaveMessages()) && event != null)
            event.set(text);
        if (!mod.isListenerCompatibilityMode() && event != null)
            event.cancel();
        mod.getJoinLeaveChannels().forEach(channelName -> mod.sendEvent(channelName, player, packetType, text));
    }
}
