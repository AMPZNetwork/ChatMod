package com.ampznetwork.chatmod.core.module.impl;

import com.ampznetwork.chatmod.api.model.config.ChatModules;
import com.ampznetwork.chatmod.api.model.module.ModuleContainer;
import com.ampznetwork.chatmod.api.model.protocol.ChatMessage;
import com.ampznetwork.chatmod.api.model.protocol.ChatMessagePacket;
import com.ampznetwork.chatmod.api.model.protocol.internal.PacketType;
import com.ampznetwork.chatmod.core.module.rabbit.AbstractRabbitMqModule;
import com.ampznetwork.libmod.api.entity.Player;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.google.gson.JsonObject;
import com.mineaurion.aurionchat.api.AurionPacket;
import com.mineaurion.aurionchat.api.AurionPlayer;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.Value;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import org.comroid.api.ByteConverter;
import org.comroid.api.info.Log;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Level;

@Value
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class LinkToAurionChatModule extends AbstractRabbitMqModule<ChatModules.AurionChatProviderConfig, LinkToAurionChatModule.PacketAdapter> {
    {
        AurionPacket.PARSE = this::parsePacket;
    }

    public LinkToAurionChatModule(ModuleContainer mod, ChatModules.AurionChatProviderConfig config) {
        super(mod, config);
    }

    @Override
    public ByteConverter<PacketAdapter> createByteConverter() {
        return new ByteConverter<>() {
            @Override
            public byte[] toBytes(PacketAdapter packetAdapter) {
                return AurionPacket.GSON.toJson(packetAdapter).getBytes(StandardCharsets.UTF_8);
            }

            @Override
            public PacketAdapter fromBytes(byte[] bytes) {
                return parsePacket(new String(bytes, StandardCharsets.UTF_8));
            }
        };
    }

    public PacketAdapter parsePacket(final @NotNull String str) {
        var json = AurionPacket.GSON.fromJson(str, JsonObject.class);
        if (json.has("route")) return AurionPacket.GSON.fromJson(str, PacketAdapter.class);
        return new PacketAdapter(AurionPacket.GSON.fromJson(str, AurionPacket.class), new ArrayList<>());
    }

    @Override
    public PacketAdapter upgradeToNative(ChatMessagePacket packet) {
        if (packet instanceof PacketAdapter adp) return adp;
        var sender = packet.getMessage().getSender();
        if (sender == null) {
            Log.at(Level.WARNING, "Skipping packet " + packet + " because it does not have a sender");
            //todo: lookup linked user
            sender = Player.builder().id(new UUID(0, 0)).name(packet.getMessage().getSenderName()).build();
        }
        var player = new AurionPlayer(sender.getId(), sender.getName(), null, null);
        return new PacketAdapter(AurionPacket.Type.CHAT,
                packet.getSource(),
                player,
                packet.getChannel(),
                mod.getPlayerAdapter().getPlayer(sender.getId()).map(Player::getName).orElseGet(packet.getMessage()::getSenderName),
                GsonComponentSerializer.gson().serialize(packet.getMessage().getFullText()),
                packet.getRoute());
    }

    @Value
    @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.NONE,
                    setterVisibility = JsonAutoDetect.Visibility.NONE,
                    getterVisibility = JsonAutoDetect.Visibility.NONE,
                    isGetterVisibility = JsonAutoDetect.Visibility.NONE)
    public class PacketAdapter extends AurionPacket implements ChatMessagePacket {
        List<String> route;

        public PacketAdapter(AurionPacket packet, List<String> route) {
            this(packet.getType(), packet.getSource(), packet.getPlayer(), packet.getChannel(), packet.getDisplayName(), packet.getTellRawData(), route);
        }

        public PacketAdapter(
                Type type, String source, @Nullable AurionPlayer player, @Nullable String channel, @Nullable String displayName, @NotNull String tellRawData,
                List<String> route
        ) {
            super(type, source, player, channel, displayName, tellRawData);

            this.route = route == null ? new ArrayList<>() : route;
        }

        @Override
        public String getChannel() {
            return Objects.requireNonNullElse(super.getChannel(), "global");
        }

        @Override
        public PacketType getPacketType() {
            return PacketType.CHAT;
        }

        @Override
        public ChatMessage getMessage() {
            var optional = Optional.ofNullable(getPlayer());
            var sender = optional.map(AurionPlayer::getId)
                    .flatMap(mod.getPlayerAdapter()::getPlayer)
                    .or(() -> optional.map(AurionPlayer::getName).flatMap(mod.getPlayerAdapter()::getPlayer))
                    .orElseThrow(() -> new NoSuchElementException("Player not found"));
            return new ChatMessage(sender, mod.getPlayerAdapter().getDisplayName(sender.getId()), getDisplayString(), (TextComponent) getComponent());
        }
    }
}
