package com.ampznetwork.chatmod.spigot.adp;

import com.ampznetwork.chatmod.api.formatting.MessageFormatter;
import com.ampznetwork.chatmod.api.formatting.impl.DecorateFormatter;
import com.ampznetwork.chatmod.api.formatting.impl.MarkdownFormatter;
import com.ampznetwork.chatmod.api.formatting.impl.RegexFormatter;
import com.ampznetwork.chatmod.api.formatting.impl.UrlFormatter;
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
import org.comroid.api.Polyfill;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer.*;

@Value
public class SpigotEventDispatch extends EventDispatchBase<ChatMod$Spigot> implements Listener {
    public SpigotEventDispatch(ChatMod$Spigot mod) {
        super(mod, buildFormatterChain(mod));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void dispatch(PlayerJoinEvent event) {
        playerJoin(event.getPlayer().getUniqueId());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void dispatch(PlayerQuitEvent event) {
        playerLeave(event.getPlayer().getUniqueId());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void dispatch(PlayerKickEvent event) {
        playerLeave(event.getPlayer().getUniqueId());
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
        var cfg = mod.getConfig();
        var ls  = Polyfill.<List<Map<String, ?>>>uncheckedCast(cfg.getList("formatters"));
        var out = new ArrayList<MessageFormatter>();
        for (var $0 : ls) {
            var type   = $0.keySet().stream().findAny().orElseThrow();
            var config = Polyfill.<Map<String, ?>>uncheckedCast($0.get(type));
            switch (type) {
                case "urls":
                    out.add(UrlFormatter.of(config));
                    break;
                case "markdown":
                    out.add(MarkdownFormatter.of(config));
                    break;
                case "regex":
                    out.add(RegexFormatter.of(config));
                    break;
                case "decorate":
                    out.add(DecorateFormatter.of(config));
                    break;
                default:
                    throw new IllegalStateException("Unexpected value: " + type);
            }
        }
        return out.toArray(MessageFormatter[]::new);
    }
}
