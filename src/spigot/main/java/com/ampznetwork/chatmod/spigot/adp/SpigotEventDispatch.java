package com.ampznetwork.chatmod.spigot.adp;

import com.ampznetwork.chatmod.api.model.ChatMessage;
import com.ampznetwork.chatmod.core.EventDispatchBase;
import com.ampznetwork.chatmod.spigot.ChatMod$Spigot;
import lombok.Value;
import net.kyori.adventure.text.Component;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import static net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer.*;

@Value
public class SpigotEventDispatch extends EventDispatchBase<ChatMod$Spigot> implements Listener {
    public SpigotEventDispatch(ChatMod$Spigot mod) {
        super(mod);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void dispatch(PlayerJoinEvent event) {
        playerJoin(event.getPlayer().getUniqueId(), text -> event.setJoinMessage(legacySection().serialize(text)));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void dispatch(PlayerQuitEvent event) {
        playerLeave(event.getPlayer().getUniqueId(), text -> event.setQuitMessage(legacySection().serialize(text)));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void dispatch(PlayerKickEvent event) {
        playerLeave(event.getPlayer().getUniqueId(), text -> event.setLeaveMessage(legacySection().serialize(text)));
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void dispatch(AsyncPlayerChatEvent event) {
        if (event.isCancelled())
            return;

        var player = mod.getLib().getPlayerAdapter()
                .getPlayer(event.getPlayer().getUniqueId())
                .orElseThrow();
        var message = new ChatMessage(player, mod.getPlayerAdapter().getDisplayName(player.getId()), event.getMessage(), Component.text(event.getMessage()));
        mod.getFormatter().accept(mod, message);

        if (mod.isListenerCompatibilityMode()) {
            event.setFormat(legacySection().serialize(message.getPrepend()) + "%2$s" + legacySection().serialize(message.getAppend()));
            event.setMessage(legacySection().serialize(message.getText()));
        } else event.setCancelled(true);
        dispatch(message);
    }
}
