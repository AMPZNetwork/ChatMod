package com.ampznetwork.test.chatmod;

import com.ampznetwork.chatmod.api.ChatMod;
import com.ampznetwork.chatmod.api.model.protocol.ChatMessage;
import com.ampznetwork.chatmod.core.formatting.ChatMessageFormatter;
import com.ampznetwork.libmod.api.LibMod;
import com.ampznetwork.libmod.api.entity.Player;
import com.ampznetwork.libmod.api.interop.game.IPlayerAdapter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.kyori.adventure.util.TriState;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;

import static org.easymock.EasyMock.*;

public class FormatterTest {
    private final String FORMAT = "§7[%server_name%§7] §f%player_name%§f: %message%";

    @Test
    void test() {
        var string    = "&cThis is red and &nthis is only underlined. **Google is at https://google.com**";
        //var string    = "_&atest inga [a](b) [a]( ) [link test?](https://google.com)_";
        var sender    = Player.builder().id(UUID.randomUUID()).name("Steve").build();
        var formatter = ChatMessageFormatter.builder().build();

        IPlayerAdapter playerAdapter = mock(IPlayerAdapter.class);
        expect(playerAdapter.getPlayer(sender.getId())).andReturn(Optional.of(sender)).anyTimes();
        Arrays.stream(TextDecoration.values())
                .map(Enum::name)
                .map(String::toLowerCase)
                .forEach(str -> expect(playerAdapter.checkPermission(sender.getId(), "chatmod.format." + str)).andReturn(TriState.TRUE).anyTimes());
        LibMod lib = mock(LibMod.class);
        expect(lib.getPlayerAdapter()).andReturn(playerAdapter).anyTimes();
        ChatMod mod = mock(ChatMod.class);
        expect(mod.getLib()).andReturn(lib).anyTimes();
        expect(mod.applyPlaceholderApi(sender.getId(), FORMAT)).andReturn(FORMAT).anyTimes();

        replay(playerAdapter, lib, mod);

        var msg = new ChatMessage(sender, sender.getName(), string, Component.text(string));
        formatter.accept(mod, msg);
        var json = GsonComponentSerializer.gson().serialize(msg.getText());
        System.out.println("json = " + json);
    }
}
