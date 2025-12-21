package com.ampznetwork.chatmod.core.module.impl;

import com.ampznetwork.chatmod.api.model.config.ChatModules;
import com.ampznetwork.chatmod.api.model.module.ModuleContainer;
import com.ampznetwork.chatmod.api.model.protocol.ChatMessage;
import com.ampznetwork.chatmod.api.model.protocol.ChatMessagePacket;
import com.ampznetwork.chatmod.api.model.protocol.internal.ChatMessagePacketImpl;
import com.ampznetwork.chatmod.api.model.protocol.internal.PacketType;
import com.ampznetwork.chatmod.core.module.rabbit.IdentityRabbitMqModule;
import com.ampznetwork.libmod.api.entity.Player;
import com.google.gson.JsonElement;
import com.mineaurion.aurionchat.api.AurionPacket;
import com.mineaurion.aurionchat.api.AurionPlayer;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.Value;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.serializer.json.JSONComponentSerializer;
import org.comroid.api.ByteConverter;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Pattern;

@Value
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class LinkToAurionChatModule extends IdentityRabbitMqModule<ChatModules.AurionChatProviderConfig> {
    public LinkToAurionChatModule(ModuleContainer mod, ChatModules.AurionChatProviderConfig config) {
        super(mod, config);
    }

    @Override
    public ByteConverter<ChatMessagePacket> createByteConverter() {
        return new ByteConverter<>() {
            @Override
            public byte[] toBytes(ChatMessagePacket packet) {
                var sender = Optional.ofNullable(packet.getMessage().getSender())
                        .orElseGet(() -> Player.builder()
                                .id(new UUID(0, 0))
                                .name(packet.getMessage().getSenderName())
                                .build());
                var aurion = new AurionPacket(packet.getPacketType().getAurionPacketType(),
                        packet.getSource(),
                        packet.getRoute(),
                        new AurionPlayer(sender.getId(), sender.getName(), null, null),
                        packet.getChannel(),
                        sender.getName(),
                        JSONComponentSerializer.json().serialize(packet.getMessage().getFullText()));

                return AurionPacket.GSON.toJson(aurion).getBytes(StandardCharsets.UTF_8);
            }

            @Override
            public ChatMessagePacket fromBytes(byte[] bytes) {
                var str    = new String(bytes);
                var tree   = AurionPacket.GSON.fromJson(str, JsonElement.class);
                var aurion = AurionPacket.GSON.fromJson(tree, AurionPacket.class);

                String content;
                var    contentPattern = config.getContentPattern();
                if (contentPattern == null)
                    return null;
                var    matcher        = Pattern.compile(contentPattern).matcher(aurion.getDisplayString());
                if (matcher.matches()) content = matcher.group(1);
                else content = aurion.getDisplayString();

                var sender = mod.getPlayerAdapter()
                        .getPlayer(Objects.requireNonNull(aurion.getPlayer(), "No player in message: " + str).getId())
                        .orElseThrow(() -> new NoSuchElementException("Player not found in message: " + str));

                return new ChatMessagePacketImpl(Arrays.stream(PacketType.values())
                        .filter(pt -> pt.getAurionPacketType() == aurion.getType())
                        .findAny()
                        .orElse(PacketType.OTHER),
                        aurion.getSource(),
                        aurion.getChannel(),
                        new ChatMessage(sender,
                                sender.getName(),
                                content,
                                (TextComponent) JSONComponentSerializer.json().deserialize(aurion.getTellRawData())),
                        aurion.getRoute());
            }
        };
    }

    @Override
    public int priority() {
        return Integer.MAX_VALUE;
    }
}
