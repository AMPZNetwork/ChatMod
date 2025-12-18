package com.ampznetwork.chatmod.api.model.config.channel;

import com.ampznetwork.chatmod.api.ChatMod;
import com.ampznetwork.chatmod.api.model.config.ChatModules;
import com.ampznetwork.chatmod.api.model.config.discord.DiscordChannel;
import com.ampznetwork.chatmod.api.model.protocol.ChatMessage;
import com.ampznetwork.libmod.api.entity.Player;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Builder.Default;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Value;
import lombok.experimental.SuperBuilder;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.comroid.api.attr.Aliased;
import org.comroid.api.text.minecraft.ComponentSupplier;
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
@EqualsAndHashCode(callSuper = true)
public class Channel extends ChatModules.NamedBaseConfig implements Aliased, ComponentSupplier.PlayerFocused {
    @Nullable @Default                             String         alias      = null;
    @Nullable @Default String display = null;
    @Nullable @Default                             String         permission = null;
    @Nullable @Default                             DiscordChannel discord    = null;
    @Default                                       boolean        publish    = true;
    @Getter(onMethod_ = @__(@JsonIgnore)) @Default Set<UUID>      playerIDs  = new HashSet<>();
    @Getter(onMethod_ = @__(@JsonIgnore)) @Default Set<UUID>      spyIDs     = new HashSet<>();

    @JsonIgnore
    @Deprecated(forRemoval = true)
    public Channel(
            boolean enabled, @NotNull String name, @Nullable String alias, @Nullable String permission,
            @Nullable DiscordChannel discord, boolean publish
    ) {
        this(enabled, name, alias, null, permission, discord, publish);
    }

    @ConstructorProperties({ "enabled", "name", "alias", "display", "permission", "discord", "publish" })
    public Channel(
            boolean enabled, @NotNull String name, @Nullable String alias, @Nullable String display,
            @Nullable String permission, @Nullable DiscordChannel discord, boolean publish
    ) {
        super(enabled, name);

        this.alias      = alias;
        this.display = display;
        this.permission = permission;
        this.discord    = discord;
        this.publish    = publish;
        this.playerIDs  = new HashSet<>();
        this.spyIDs     = new HashSet<>();
    }

    @Override
    public String getAlternateName() {
        return Objects.requireNonNullElse(display, getName());
    }

    public ChannelState getState(UUID playerId) {
        if (playerIDs.contains(playerId)) return ChannelState.Joined;
        if (spyIDs.contains(playerId)) return ChannelState.Spying;
        return ChannelState.Idle;
    }

    public Stream<UUID> allPlayerIDs() {
        return Stream.concat(playerIDs.stream(), spyIDs.stream());
    }

    @Override
    public Stream<String> aliases() {
        return Stream.of(name, alias).filter(Objects::nonNull);
    }

    public ChatMessage formatMessage(ChatMod mod, Player sender, String message) {
        var msg = new ChatMessage(sender,
                mod.getPlayerAdapter().getDisplayName(sender.getId()),
                message,
                Component.text(message));
        mod.getFormatter().accept(mod, msg);
        return msg;
    }

    public void send(ChatMod mod, ChatMessage message) {
        allPlayerIDs().forEach(id -> mod.getPlayerAdapter().send(id, message.getFullText()));
    }

    @Override
    public TextComponent toComponent() {
        return Component.text(getName(), NamedTextColor.AQUA);
    }

    @Override
    public ComponentLike specifyComponent(TextComponent component, @Nullable UUID playerId) {
        var state  = getState(playerId);
        var result = component.toBuilder().color(state.toColor());
        if (state != ChannelState.Idle) result.hoverEvent(state.toHoverEvent());
        return result;
    }
}
