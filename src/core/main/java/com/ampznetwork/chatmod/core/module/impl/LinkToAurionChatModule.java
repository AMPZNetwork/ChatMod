package com.ampznetwork.chatmod.core.module.impl;

import com.ampznetwork.chatmod.api.model.config.ChatModules;
import com.ampznetwork.chatmod.api.model.module.ModuleContainer;
import com.ampznetwork.chatmod.api.model.protocol.ChatMessage;
import com.ampznetwork.chatmod.api.model.protocol.ChatMessagePacket;
import com.ampznetwork.chatmod.api.model.protocol.internal.PacketType;
import com.ampznetwork.chatmod.core.module.rabbit.AbstractRabbitMqModule;
import com.ampznetwork.libmod.api.entity.Player;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.gson.annotations.Expose;
import com.mineaurion.aurionchat.api.AurionPacket;
import com.mineaurion.aurionchat.api.AurionPlayer;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.Value;
import lombok.experimental.NonFinal;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import org.comroid.api.ByteConverter;
import org.comroid.api.info.Log;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Level;
import java.util.regex.Pattern;

import static net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer.*;

@Value
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class LinkToAurionChatModule extends AbstractRabbitMqModule<ChatModules.AurionChatProviderConfig, LinkToAurionChatModule.PacketAdapter> {
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
                return AurionPacket.GSON.fromJson(new InputStreamReader(new ByteArrayInputStream(bytes)), PacketAdapter.class).setMod(mod);
            }
        };
    }

    @Override
    public int priority() {
        return Integer.MAX_VALUE;
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
        return new PacketAdapter(packet.getPacketType().getAurionPacketType(), packet.getPacketType(),
                packet.getSource(),
                packet.getRoute(),
                new AurionPlayer(sender.getId(), sender.getName(), null, null),
                packet.getChannel(),
                mod.getPlayerAdapter().getPlayer(sender.getId()).map(Player::getName).orElseGet(packet.getMessage()::getSenderName),
                GsonComponentSerializer.gson().serialize(packet.getMessage().getFullText()), config).setMod(mod);
    }

    @Value
    public static class PacketAdapter extends AurionPacket implements ChatMessagePacket {
        @Expose PacketType packetType;
        ChatModules.AurionChatProviderConfig config;
        @Getter(onMethod_ = @__(@JsonIgnore)) @Setter(onMethod_ = @__(@JsonIgnore)) @NonFinal ModuleContainer mod;

        public PacketAdapter(
                Type type, PacketType packetType, String source, List<String> route, @Nullable AurionPlayer player, @Nullable String channel,
                @Nullable String displayName, @NotNull String tellRawData,
                ChatModules.AurionChatProviderConfig config
        ) {
            super(type == null ? AurionPacket.Type.CHAT : type, source, route == null ? new ArrayList<>() : route, player, channel, displayName, tellRawData);
            this.packetType = packetType;
            this.config = config;
        }

        @Override
        public Type getType() {
            return super.getType();
        }

        public PacketType getPacketType() {
            return packetType == null ? PacketType.CHAT : packetType;
        }

        @Override
        public ChatMessage getMessage() {
            var optional = Optional.ofNullable(getPlayer());
            var sender = optional.map(AurionPlayer::getId)
                    .flatMap(mod.getPlayerAdapter()::getPlayer)
                    .or(() -> optional.map(AurionPlayer::getName).flatMap(mod.getPlayerAdapter()::getPlayer))
                    .orElseThrow(() -> new NoSuchElementException("Player not found"));
            TextComponent component;
            if (config.getContentPattern() == null)
                component = (TextComponent) getComponent();
            else {
                var txt     = legacySection().serialize(getComponent());
                var matcher = Pattern.compile(config.getContentPattern()).matcher(txt);
                component = legacySection().deserialize(matcher.find() ? matcher.group(1) : txt);
            }
            return new ChatMessage(sender, mod.getPlayerAdapter().getDisplayName(sender.getId()), getDisplayString(), component);
        }
    }
}
