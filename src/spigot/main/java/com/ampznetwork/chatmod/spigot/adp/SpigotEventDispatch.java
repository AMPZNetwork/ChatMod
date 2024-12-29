package com.ampznetwork.chatmod.spigot.adp;

import com.ampznetwork.chatmod.api.model.ChatMessage;
import com.ampznetwork.chatmod.core.EventDispatchBase;
import com.ampznetwork.chatmod.spigot.ChatMod$Spigot;
import com.ampznetwork.libmod.api.model.delegate.EventDelegate;
import com.ampznetwork.libmod.core.adapter.internal.DelegatingCancellable;
import com.ampznetwork.libmod.core.adapter.internal.EventDelegateImpl;
import lombok.Value;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.comroid.api.func.ext.Accessor;

import static net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer.*;

@Value
public class SpigotEventDispatch extends EventDispatchBase<ChatMod$Spigot> implements Listener {
    public static final Accessor<PlayerJoinEvent, String> DELEGATE_PROPERTY_JOIN;
    public static final Accessor<PlayerQuitEvent, String> DELEGATE_PROPERTY_QUIT;
    public static final Accessor<PlayerKickEvent, String> DELEGATE_PROPERTY_KICK;

    static {
        try {
            DELEGATE_PROPERTY_JOIN = new Accessor.GetSetMethods<>(
                    PlayerJoinEvent.class.getMethod("getJoinMessage"),
                    PlayerJoinEvent.class.getMethod("setJoinMessage", String.class));
            DELEGATE_PROPERTY_QUIT = new Accessor.GetSetMethods<>(
                    PlayerQuitEvent.class.getMethod("getQuitMessage"),
                    PlayerQuitEvent.class.getMethod("setQuitMessage", String.class));
            DELEGATE_PROPERTY_KICK = new Accessor.GetSetMethods<>(
                    PlayerKickEvent.class.getMethod("getLeaveMessage"),
                    PlayerKickEvent.class.getMethod("setLeaveMessage", String.class));
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    public SpigotEventDispatch(ChatMod$Spigot mod) {
        super(mod);
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

    @EventHandler(priority = EventPriority.MONITOR)
    public void dispatch(PlayerJoinEvent event) {
        playerJoin(event.getPlayer().getUniqueId(), createEventDelegate(event, DELEGATE_PROPERTY_JOIN));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void dispatch(PlayerQuitEvent event) {
        playerLeave(event.getPlayer().getUniqueId(), createEventDelegate(event, DELEGATE_PROPERTY_QUIT));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void dispatch(PlayerKickEvent event) {
        playerLeave(event.getPlayer().getUniqueId(), createEventDelegate(event, DELEGATE_PROPERTY_KICK));
    }

    private <E> EventDelegate<TextComponent> createEventDelegate(E event, Accessor<E, String> property) {
        return new EventDelegateImpl<>(event,
                property.proxy(legacySection()::deserialize, legacySection()::serialize),
                new DelegatingCancellable<>(event, property));
    }
}
