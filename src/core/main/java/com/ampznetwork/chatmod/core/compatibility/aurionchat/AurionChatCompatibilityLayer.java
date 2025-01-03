package com.ampznetwork.chatmod.core.compatibility.aurionchat;

import com.ampznetwork.chatmod.api.ChatModCompatibilityLayerAdapter;
import com.ampznetwork.chatmod.api.model.ChatMessage;
import com.ampznetwork.chatmod.api.model.ChatMessagePacket;
import com.ampznetwork.chatmod.api.model.MessageType;
import com.ampznetwork.chatmod.core.compatibility.RabbitMqCompatibilityLayer;
import com.ampznetwork.libmod.api.entity.Player;
import com.mineaurion.aurionchat.api.AurionPacket;
import com.mineaurion.aurionchat.api.AurionPlayer;
import lombok.Value;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import org.comroid.api.ByteConverter;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@Value
public class AurionChatCompatibilityLayer extends RabbitMqCompatibilityLayer<AurionPacketAdapter> {
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
    public ByteConverter<AurionPacketAdapter> createByteConverter() {
        return new AurionPacketByteConverter();
    }

    @Override
    public boolean skip(AurionPacketAdapter packet) {
        var type = packet.getType();
        return type != AurionPacket.Type.CHAT && type != AurionPacket.Type.AUTO_MESSAGE;
    }

    @Override
    public ChatMessagePacket convertToChatModPacket(AurionPacketAdapter packet) {
        var player = Optional.ofNullable(packet.getPlayer());
        var sender = player.map(AurionPlayer::getId)
                .flatMap(mod.getPlayerAdapter()::getPlayer)
                .or(() -> player.map(AurionPlayer::getName)
                        .flatMap(mod.getPlayerAdapter()::getPlayer))
                .orElseThrow(() -> new NoSuchElementException("Player not found for packet " + packet));
        var message = new ChatMessage(sender,
                mod.getPlayerAdapter().getDisplayName(sender.getId()),
                packet.getDisplayString(),
                (TextComponent) packet.getComponent());
        return new ChatMessagePacket(switch (packet.getType()) {
            case CHAT, AUTO_MESSAGE -> MessageType.CHAT;
            case EVENT_JOIN -> MessageType.JOIN;
            default -> throw new UnsupportedOperationException("Unsupported packet type: " + packet.getType());
        }, packet.getSource(), packet.getChannel(), message, List.of(getMod().getSourceName()));
    }

    @Override
    public AurionPacketAdapter convertToNativePacket(ChatMessagePacket packet) {
        var sender = packet.getMessage().getSender();
        assert sender != null : "Outbound from Minecraft should always have a Sender";
        var player = new AurionPlayer(sender.getId(), sender.getName(), null, null);
        return new AurionPacketAdapter(AurionPacket.Type.CHAT, packet.getSource(), player, packet.getChannel(),
                mod.getPlayerAdapter().getPlayer(sender.getId()).map(Player::getName).orElseThrow(),
                GsonComponentSerializer.gson().serialize(packet.getMessage().getFullText()), packet.getRoute());
    }

    @Override
    public void handle(AurionPacketAdapter packet) {
        if (!isEnabled() || skip(packet)) return;
        var convert = convertToChatModPacket(packet);
        if (getMod().skip(convert)) return;

        // relay for other servers
        getMod().relayOutbound(convert);
        getMod().relayInbound(convert);
    }
}
