package com.ampznetwork.chatmod.core;

import com.ampznetwork.chatmod.api.ChatMod;
import com.ampznetwork.chatmod.core.util.AutoFill;
import com.ampznetwork.chatmod.generated.PluginYml.Permission.chatmod;
import net.kyori.adventure.text.Component;
import org.comroid.annotations.Alias;
import org.comroid.api.func.util.Command;
import org.comroid.api.text.StringMode;

import java.util.Arrays;
import java.util.Objects;
import java.util.UUID;

import static net.kyori.adventure.text.Component.*;
import static net.kyori.adventure.text.format.NamedTextColor.*;
import static net.kyori.adventure.text.format.TextDecoration.*;

public class ChatModCommands {
    @Command(permission = chatmod.SHOUT)
    public static Component shout(
            ChatMod mod,
            @Command.Arg(autoFillProvider = AutoFill.ChannelNames.class) String channelName,
            @Command.Arg(stringMode = StringMode.GREEDY) String message,
            UUID playerId
    ) {
        var player = mod.getLib().getPlayerAdapter().getPlayer(playerId).orElseThrow();
        return mod.getChannels().stream()
                .filter(channel -> Arrays.asList(channel.getName(), channel.getAlias()).contains(channelName))
                .findAny()
                .map(channel -> {
                    var msg = channel.formatMessage(mod, player, message);
                    mod.send(channel.getName(), msg);
                    return msg.getText();
                }).orElseThrow(() -> new Command.Error("Could not shout to channel " + channelName));
    }

    @Command
    @Alias("ch")
    public static class channel {
        @Command(permission = chatmod.channel.LIST)
        public static Component list(ChatMod mod, UUID playerId) {
            var text = text().append(text("Available Channels:").decorate(BOLD));
            if (mod.getChannels().isEmpty())
                text.append(text("\n- "))
                        .append(text("(none)").color(GRAY));
            else for (var channel : mod.getChannels()) {
                text.append(text("\n- "))
                        .append(text(channel.getName()).color(AQUA));
                var member = channel.getPlayerIDs().contains(playerId);
                var spy    = channel.getSpyIDs().contains(playerId);
                if (member || spy)
                    text.append(text(" - "));
                if (member) text.append(text("joined").color(GREEN));
                if (spy) {
                    if (member)
                        text.append(text(", "));
                    text.append(text("spying").color(YELLOW));
                }
            }
            return text.build();
        }

        @Command(permission = chatmod.channel.INFO)
        public static Component info(ChatMod mod, @Command.Arg(autoFillProvider = AutoFill.ChannelNames.class) String channelName) {
            var channel = mod.getChannels().stream()
                    .filter(it -> Arrays.asList(it.getName(), it.getAlias()).contains(channelName))
                    .findAny().orElseThrow(() -> new Command.Error("No such channel: " + channelName));
            return text()
                    .append(text("Chat Channel ").decorate(UNDERLINED))
                    .append(text(channel.getName()).color(AQUA).decorate(UNDERLINED))
                    .append(text("\n"))
                    .append(text("Alias: "))
                    .append(text(Objects.requireNonNullElse(channel.getAlias(), "none")).color(GREEN))
                    .append(text("\n"))
                    .append(text("Required Permission: "))
                    .append(text(Objects.requireNonNullElse(channel.getPermission(), "none")).color(YELLOW))
                    .append(text("\n"))
                    .append(text("Publish to RabbitMQ: "))
                    .append(text(channel.isPublish()).color(YELLOW))
                    .build();
        }

        @Command(permission = chatmod.channel.JOIN)
        public static Component join(ChatMod mod, @Command.Arg(autoFillProvider = AutoFill.ChannelNames.class) String channelName, UUID playerId) {
            var channels = mod.getChannels();
            var currentChannel = channels.stream()
                    .filter(it -> it.getPlayerIDs().contains(playerId))
                    .findAny().orElse(null);
            var targetChannel = channels.stream()
                    .filter(it -> Arrays.asList(it.getName(), it.getAlias()).contains(channelName))
                    .findAny().orElseThrow(() -> new Command.Error("No such channel: " + channelName));
            if (currentChannel != null)
                currentChannel.getPlayerIDs().remove(playerId);
            targetChannel.getPlayerIDs().add(playerId);
            return text("Successfully joined channel ")
                    .append(text(targetChannel.getName()).color(AQUA));
        }

        @Command(permission = chatmod.channel.SPY)
        public static Component spy(
                ChatMod mod, @Command.Arg(autoFillProvider = AutoFill.ChannelNames.class, autoFill = { "*" }) String channelName, UUID playerId) {
            var channels = mod.getChannels();
            if ("*".equals(channelName)) {
                channels.forEach(config -> config.getSpyIDs().add(playerId));
                return text("Now spying ")
                        .append(text("all channels").color(AQUA));
            } else {
                var targetChannel = channels.stream()
                        .filter(it -> Arrays.asList(it.getName(), it.getAlias()).contains(channelName))
                        .findAny().orElseThrow(() -> new Command.Error("No such channel: " + channelName));
                var ids = targetChannel.getSpyIDs();
                if (ids.contains(playerId)) {
                    ids.remove(playerId);
                    return text("Stopped spying channel ")
                            .append(text(targetChannel.getName()).color(AQUA));
                } else {
                    ids.add(playerId);
                    return text("Now spying channel ")
                            .append(text(targetChannel.getName()).color(AQUA));
                }
            }
        }
    }
}
