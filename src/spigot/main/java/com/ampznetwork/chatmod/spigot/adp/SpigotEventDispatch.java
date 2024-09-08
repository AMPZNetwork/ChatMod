package com.ampznetwork.chatmod.spigot.adp;

import com.ampznetwork.chatmod.api.formatting.MessageFormatter;
import com.ampznetwork.chatmod.api.model.ChatMessage;
import com.ampznetwork.chatmod.core.EventDispatchBase;
import com.ampznetwork.chatmod.spigot.ChatMod$Spigot;
import lombok.Value;
import net.kyori.adventure.text.Component;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import static net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer.*;

@Value
public class SpigotEventDispatch extends EventDispatchBase<ChatMod$Spigot> implements Listener {
    public SpigotEventDispatch(ChatMod$Spigot mod) {
        super(mod, buildFormatterChain(mod));
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void dispatch(AsyncPlayerChatEvent event) {
        if (event.isCancelled())
            return;

        var player = mod.getLib().getPlayerAdapter()
                .getPlayer(event.getPlayer().getUniqueId())
                .orElseThrow();
        var message = new ChatMessage(player,
                event.getMessage(),
                event.getMessage(),
                Component.text(event.getMessage()));

        dispatch(message);
        event.setCancelled(true);
    }

    private static MessageFormatter[] buildFormatterChain(ChatMod$Spigot mod) {
    }
}
