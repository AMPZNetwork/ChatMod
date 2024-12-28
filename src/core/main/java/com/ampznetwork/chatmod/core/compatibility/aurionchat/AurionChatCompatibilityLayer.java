package com.ampznetwork.chatmod.core.compatibility.aurionchat;

import com.ampznetwork.chatmod.api.ChatModCompatibilityLayerAdapter;
import com.ampznetwork.chatmod.api.model.ChatMessage;
import com.ampznetwork.chatmod.api.model.ChatMessagePacket;
import com.ampznetwork.chatmod.core.compatibility.RabbitMqCompatibilityLayer;
import com.ampznetwork.libmod.api.entity.Player;
import com.mineaurion.aurionchat.api.AurionPacket;
import com.mineaurion.aurionchat.api.AurionPlayer;
import lombok.Value;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import org.comroid.api.ByteConverter;

import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;

@Value
public class AurionChatCompatibilityLayer extends RabbitMqCompatibilityLayer<AurionPacket> {
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
    public ByteConverter<AurionPacket> createByteConverter() {
        return new AurionPacketByteConverter();
    }

    @Override
    public boolean isEnabled() {
        return !"none".equalsIgnoreCase(getUri());
    }

    @Override
    public ChatMessagePacket convertToChatModPacket(AurionPacket packet) {
        var player = Optional.ofNullable(packet.getPlayer());
        var sender = player.map(AurionPlayer::getId)
                .flatMap(mod.getPlayerAdapter()::getPlayer)
                .or(() -> player.map(AurionPlayer::getName)
                        .flatMap(mod.getPlayerAdapter()::getPlayer))
                .orElseThrow(() -> new NoSuchElementException("Player not found for packet " + packet));
        var message = new ChatMessage(sender, sender.getName(), packet.getDisplayString(), (TextComponent) packet.getComponent());
        return new ChatMessagePacket(packet.getSource(), packet.getChannel(), message);
    }

    @Override
    public AurionPacket convertToNativePacket(ChatMessagePacket packet) {
        var sender = packet.getMessage().getSender();
        assert sender != null : "Outbound from Minecraft should always have a Sender";
        var player = new AurionPlayer(sender.getId(), sender.getName(), null, null);
        return new AurionPacket(AurionPacket.Type.CHAT, packet.getSource(), player, packet.getChannel(),
                mod.getPlayerAdapter().getPlayer(sender.getId()).map(Player::getName).orElseThrow(),
                GsonComponentSerializer.gson().serialize(packet.getMessage().getText()));
    }

    @Override
    public boolean skip(AurionPacket packet) {
        return !Set.of(AurionPacket.Type.CHAT, AurionPacket.Type.AUTO_MESSAGE).contains(packet.getType());
    }
}
