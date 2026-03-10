package com.ampznetwork.chatmod.spigot;

import com.ampznetwork.chatmod.api.model.Player;
import com.ampznetwork.chatmod.model.RecipientSerializer;
import lombok.Value;
import org.comroid.api.data.RegExpUtil;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Objects;
import java.util.UUID;

@Value
public class SpigotRecipientSerializer implements RecipientSerializer {
    ChatModLiteSpigot plugin;

    @Override
    public @Nullable Player deserializeRecipient(String recipient) {
        return recipient.matches(RegExpUtil.UUID4_PATTERN)
               ? plugin.getPlayer(UUID.fromString(recipient))
               : Arrays.stream(plugin.getServer().getOfflinePlayers())
                       .filter(player -> Objects.equals(player.getName(), recipient))
                       .findAny()
                       .map(player -> new Player(player.getUniqueId(), player.getName()))
                       .orElse(null);
    }
}
