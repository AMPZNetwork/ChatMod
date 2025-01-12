package com.ampznetwork.chatmod.api.model.config.channel;

import com.ampznetwork.chatmod.api.ChatMod;
import com.ampznetwork.chatmod.api.model.config.ChatModules;
import com.ampznetwork.chatmod.api.model.config.discord.DiscordChannel;
import com.ampznetwork.chatmod.api.model.protocol.ChatMessage;
import com.ampznetwork.libmod.api.entity.Player;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Builder.Default;
import lombok.Getter;
import lombok.Value;
import lombok.experimental.SuperBuilder;
import net.kyori.adventure.text.Component;
import org.comroid.api.attr.Aliased;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.beans.ConstructorProperties;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;

@Value
@SuperBuilder
public class Channel extends ChatModules.NamedBaseConfig implements Aliased {
    @Nullable @Default                             String         alias      = null;
    @Nullable @Default                             String         permission = null;
    @Nullable @Default                             DiscordChannel discord    = null;
    @Default                                       boolean        publish    = true;
    @Getter(onMethod_ = @__(@JsonIgnore)) @Default Set<UUID>      playerIDs  = new HashSet<>();
    @Getter(onMethod_ = @__(@JsonIgnore)) @Default Set<UUID>      spyIDs     = new HashSet<>();

    @ConstructorProperties({ "enabled", "name", "alias", "permission", "discord", "publish" })
    public Channel(
            boolean enabled, @NotNull String name, @Nullable String alias, @Nullable String permission, @Nullable DiscordChannel discord, boolean publish) {
        super(enabled, name);

        this.alias      = alias;
        this.permission = permission;
        this.discord    = discord;
        this.publish    = publish;
        this.playerIDs  = new HashSet<>();
        this.spyIDs     = new HashSet<>();
    }

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
