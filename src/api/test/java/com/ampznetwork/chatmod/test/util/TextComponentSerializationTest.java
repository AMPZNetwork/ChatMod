package com.ampznetwork.chatmod.test.util;

import com.ampznetwork.chatmod.api.model.ChatMessage;
import com.ampznetwork.chatmod.api.model.ChatMessagePacket;
import com.ampznetwork.chatmod.api.model.ChatMessagePacketImpl;
import com.ampznetwork.chatmod.api.model.PacketType;
import com.ampznetwork.libmod.api.entity.Player;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.junit.jupiter.api.Test;

import java.util.UUID;

public class TextComponentSerializationTest {
    @Test
    void testSerialize() throws JsonProcessingException {
        var text = Component.text("arschlöoch", NamedTextColor.RED, TextDecoration.UNDERLINED)
                .hoverEvent(HoverEvent.showText(Component.text("greeting")))
                .clickEvent(ClickEvent.runCommand("help"));
        var msg = new ChatMessage(
                Player.builder().name("Kaleidox").id(UUID.randomUUID()).build(),
                "Kaleidox",
                "arschlöoch",
                text);
        var pkt = new ChatMessagePacketImpl(
                PacketType.CHAT,
                "MC",
                "global",
                msg);
        var mapper = new ObjectMapper();
        var json   = mapper.writeValueAsString(pkt);
        System.out.println("json = " + json);
        var parse = mapper.readValue(json, ChatMessagePacket.class);
        System.out.println("parse = " + parse);
    }
}
