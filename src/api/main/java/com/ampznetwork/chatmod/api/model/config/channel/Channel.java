package com.ampznetwork.chatmod.api.model.config.channel;

import com.ampznetwork.chatmod.api.ChatMod;
import com.ampznetwork.chatmod.api.model.config.ChatModules;
import com.ampznetwork.chatmod.api.model.config.discord.DiscordChannel;
import com.ampznetwork.chatmod.api.model.protocol.ChatMessage;
import com.ampznetwork.libmod.api.entity.Player;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Builder.Default;
import lombok.Value;
import lombok.experimental.SuperBuilder;
import net.kyori.adventure.text.Component;
import org.comroid.api.attr.Aliased;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;

@Value
@SuperBuilder
public class Channel extends ChatModules.NamedBaseConfig implements Aliased {
    @Nullable @Default String         alias      = null;
    @Nullable @Default String         permission = null;
    @Nullable @Default DiscordChannel discord    = null;
    @Default           boolean        publish    = true;
    @JsonIgnore        Set<UUID>      playerIDs  = new HashSet<>();
    @JsonIgnore        Set<UUID>      spyIDs     = new HashSet<>();

    public Stream<UUID> allPlayerIDs() {
        return Stream.concat(playerIDs.stream(), spyIDs.stream());
    }

    @Override
    public Stream<String> aliases() {
        return Stream.of(name, alias).filter(Objects::nonNull);
    }

    public ChatMessage formatMessage(ChatMod mod, Player sender, String message) {
        var msg = new ChatMessage(sender, mod.getPlayerAdapter().getDisplayName(sender.getId()), message, Component.text(message));
        mod.getFormatter().accept(mod, msg);
        return msg;
    }

    public void send(ChatMod mod, ChatMessage message) {
        allPlayerIDs().forEach(id -> mod.getPlayerAdapter().send(id, message.getFullText()));
    }
}
