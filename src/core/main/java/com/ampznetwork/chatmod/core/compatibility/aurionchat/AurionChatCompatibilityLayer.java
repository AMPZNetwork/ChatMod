package com.ampznetwork.chatmod.core.compatibility.aurionchat;

import com.ampznetwork.chatmod.api.model.ChatMessage;
import com.ampznetwork.chatmod.api.model.ChatMessagePacket;
import com.ampznetwork.chatmod.api.model.ChatModCompatibilityLayerAdapter;
import com.ampznetwork.chatmod.api.model.PacketType;
import com.ampznetwork.chatmod.core.compatibility.RabbitMqCompatibilityLayer;
import com.ampznetwork.libmod.api.entity.Player;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.google.gson.JsonObject;
import com.mineaurion.aurionchat.api.AurionPacket;
import com.mineaurion.aurionchat.api.AurionPlayer;
import lombok.EqualsAndHashCode;
import lombok.Value;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import org.comroid.api.ByteConverter;
import org.comroid.api.func.util.Streams;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;

@Value
@EqualsAndHashCode(callSuper = true)
public class AurionChatCompatibilityLayer extends RabbitMqCompatibilityLayer<AurionChatCompatibilityLayer.PacketAdapter> {
    {
        AurionPacket.PARSE = this::parsePacket;
    }

    public AurionChatCompatibilityLayer(ChatModCompatibilityLayerAdapter mod) {
        super(mod);
    }

    @Override
    public String getUri() {
        return mod.getAurionChatRabbitUri();
    }

    @Override
    public String getExchange() {
        return "aurion.chat";
    }

    @Override
    protected String getExchangeType() {
        return "fanout";
    }

    @Override
    public boolean isEnabled() {
        return !"none".equalsIgnoreCase(getUri());
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

    @Override
    public String getName() {
        return "AurionChat";
    }

    public PacketAdapter parsePacket(final @NotNull String str) {
        var json = AurionPacket.GSON.fromJson(str, JsonObject.class);
        if (json.has("route")) return AurionPacket.GSON.fromJson(str, PacketAdapter.class);
        return new PacketAdapter(AurionPacket.GSON.fromJson(str, AurionPacket.class), new ArrayList<>());
    }

    @Override
    public boolean skip(PacketAdapter packet) {
        var type = packet.getType();
        return type != AurionPacket.Type.CHAT && type != AurionPacket.Type.AUTO_MESSAGE;
    }

    @Override
    public ChatMessagePacket convertToChatModPacket(PacketAdapter packet) {
        return packet;
    }

    @Override
    public PacketAdapter convertToNativePacket(ChatMessagePacket packet) {
        if (packet instanceof PacketAdapter adp) return adp;
        var sender = packet.getMessage().getSender();
        assert sender != null : "Outbound from Minecraft should always have a Sender";
        var player = new AurionPlayer(sender.getId(), sender.getName(), null, null);
        return new PacketAdapter(AurionPacket.Type.CHAT,
                packet.getSource(),
                player,
                packet.getChannel(),
                mod.getPlayerAdapter().getPlayer(sender.getId()).map(Player::getName).orElseThrow(),
                GsonComponentSerializer.gson().serialize(packet.getMessage().getFullText()),
                packet.getRoute().stream().collect(Streams.append(mod.getSourceName())).toList());
    }

    @Override
    public void handle(PacketAdapter packet) {
        var convert = convertToChatModPacket(packet);

        // relay for other servers
        getMod().relayOutbound(convert);
        getMod().relayInbound(convert);
    }

    @Override
    public void send(ChatMessagePacket packet) {
        if (packet instanceof PacketAdapter) return; // do not loop packet back into aurionchat
        super.send(packet);
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
