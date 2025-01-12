package com.ampznetwork.chatmod.spigot;

import com.ampznetwork.chatmod.api.ChatMod;
import com.ampznetwork.chatmod.api.model.TextResourceProvider;
import com.ampznetwork.chatmod.api.model.config.ChatModules;
import com.ampznetwork.chatmod.api.model.config.channel.Channel;
import com.ampznetwork.chatmod.core.ChatModCommands;
import com.ampznetwork.chatmod.core.ModuleContainerCore;
import com.ampznetwork.chatmod.core.formatting.ChatMessageFormatter;
import com.ampznetwork.chatmod.core.module.impl.LinkToMinecraftModule;
import com.ampznetwork.chatmod.spigot.adp.SpigotEventDispatch;
import com.ampznetwork.libmod.api.interop.game.IPlayerAdapter;
import com.ampznetwork.libmod.api.util.Util;
import com.ampznetwork.libmod.spigot.SubMod$Spigot;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.configuration.MemorySection;
import org.comroid.api.Polyfill;
import org.comroid.api.func.util.Command;
import org.comroid.api.info.Log;
import org.comroid.api.tree.Container;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;

import static com.ampznetwork.chatmod.api.model.config.ChatModules.*;
import static com.ampznetwork.chatmod.spigot.YmlConfigHelper.*;

@Getter
@Slf4j(topic = ChatMod.Strings.AddonName)
public class ChatMod$Spigot extends SubMod$Spigot implements ChatMod, ModuleContainerCore {
    private final @lombok.experimental.Delegate Container            $delegate            = new Container.Base();
    private final                               TextResourceProvider textResourceProvider = new TextResourceProvider(this);
    private                                     ChatMessageFormatter formatter;
    private                                     boolean              hasPlaceholderApi;

    public ChatMod$Spigot() {
        super(Set.of(), Set.of());
    }

    @Override
    public IPlayerAdapter getPlayerAdapter() {
        return ChatMod.super.getPlayerAdapter();
    }

    @Override
    public ChatModules getChatModules() {
        var config = getConfig();
        var caps   = builder();

        var format = config.getConfigurationSection("format");
        if (format != null) caps.defaultFormat(formats(format));

        var modules = config.getConfigurationSection("modules");
        element(LogProviderConfig::builder, modules, "log", (bld, cfg) -> {}, caps::log);
        element(MinecraftProviderConfig::builder, modules, "minecraft", YmlConfigHelper::minecraft, caps::minecraft);
        element(NativeProviderConfig::builder, modules, "rabbitmq", YmlConfigHelper::rabbitmq, caps::rabbitmq);
        element(AurionChatProviderConfig::builder, modules, "aurionchat", YmlConfigHelper::rabbitmq, caps::aurionchat);
        element(DiscordProviderConfig::builder, modules, "discord", YmlConfigHelper::discord, caps::discord);

        return caps.build();
    }

    @Override
    public List<Channel> getChannels() {
        var config = getConfig().getConfigurationSection("channels");
        if (config == null) {
            Log.at(Level.WARNING, "No channels configured");
            return List.of();
        }

        var channels = new ArrayList<Channel>();
        for (var channelName : config.getKeys(false))
            element(Channel::builder, config, channelName, YmlConfigHelper::channel, it -> channels.add((Channel) it));

        return channels;
    }

    @Override
    public String getServerName() {
        return Util.Kyori.sanitize(getConfig().getString("server.name"));
    }

    @Override
    public LinkToMinecraftModule getDefaultModule() {
        return child(LinkToMinecraftModule.class).assertion();
    }

    @Override
    public boolean isListenerCompatibilityMode() {
        return getConfig().getBoolean("compatibility.listeners", false);
    }

    @Override
    public boolean isJoinLeaveEnabled() {
        return getConfig().getBoolean("events.join_leave.enable", true);
    }

    @Override
    public boolean isReplaceDefaultJoinLeaveMessages() {
        return getConfig().getBoolean("events.join_leave.replace", false);
    }

    @Override
    public TextResourceProvider text() {
        return textResourceProvider;
    }

    @Override
    public String applyPlaceholderApi(UUID playerId, String input) {
        var player = getServer().getOfflinePlayer(playerId);
        return hasPlaceholderApi ? PlaceholderAPI.setPlaceholders(player, input) : ChatMod.super.applyPlaceholderApi(playerId, input);
    }

    @Override
    public Class<?> getModuleType() {
        return ChatMod.class;
    }

    @Override
    public void onLoad() {
        cmdr.register(ChatModCommands.class);
        cmdr.register(this);

        super.onLoad();

        hasPlaceholderApi = getServer().getPluginManager().getPlugin("PlaceholderAPI") != null;
    }

    @Override
    public void onEnable() {
        super.onEnable();
        getServer().getPluginManager().registerEvents(new SpigotEventDispatch(this), this);
        reload();
    }

    @Command
    public @NotNull TextComponent reload() {
        reloadConfig();

        this.formatter = ChatMessageFormatter.of(Polyfill.<MemorySection>uncheckedCast(getConfig().get("formatting")).getValues(true));

        // reinitialize modules
        clearChildren();
        initModules();

        return Component.text("Successfully reloaded ChatMod configurations").color(NamedTextColor.GREEN);
    }
}
