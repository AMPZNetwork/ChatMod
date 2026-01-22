package com.ampznetwork.chatmod.lite.model.abstr;

import com.ampznetwork.libmod.api.entity.Player;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.comroid.api.java.SoftDepend;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public enum PlaceholderAdapter {
    Native {
        @Override
        public String applyPlaceholders(
                String serverName, String channelName, @Nullable String senderName,
                Player player, String text
        ) {
            //noinspection Convert2MethodRef
            return text.replaceAll("%server_name%", serverName)
                    .replaceAll("%channel_name%", channelName)
                    .replaceAll("%player_name%", Objects.requireNonNullElseGet(senderName, () -> player.getName()));
        }
    }, Hook {
        @Override
        public String applyPlaceholders(
                String serverName, String channelName, String senderName, Player player,
                String text
        ) {
            var offlinePlayer = Bukkit.getServer().getOfflinePlayer(player.getId());
            return PlaceholderAPI.setPlaceholders(offlinePlayer,
                    Native.applyPlaceholders(serverName, channelName, senderName, player, text));
        }
    };

    public static PlaceholderAdapter detect() {
        return SoftDepend.type("me.clip.placeholderapi.PlaceholderAPI").map($ -> Hook).orElse(Native);
    }

    public abstract String applyPlaceholders(
            String serverName, String channelName, String senderName, Player player, String text);
}
