package com.ampznetwork.chatmod.spigot;

import com.ampznetwork.chatmod.api.model.config.channel.Channel;
import com.ampznetwork.chatmod.api.model.config.discord.DiscordChannel;
import com.ampznetwork.chatmod.api.model.config.format.Formats;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static com.ampznetwork.chatmod.api.model.config.ChatModules.*;

public class YmlConfigHelper {
    public static <It, Bld extends NamedBaseConfig.Builder<?, ?>> void element(
            Supplier<Bld> builder, ConfigurationSection parent, String module, BiConsumer<Bld, ConfigurationSection> mod, Consumer<It> set) {
        var it      = builder.get();
        var section = init(it, parent, module);
        if (section != null) mod.accept(it, section);
        set.accept((It) it.build());
    }

    public static <It, T> void cond(
            It it, ConfigurationSection config, String property, BiPredicate<ConfigurationSection, String> isPresent, BiConsumer<It, T> set) {
        if (isPresent.test(config, property)) set.accept(it, (T) config.get(property));
    }

    public static <It> void condBoolean(It it, ConfigurationSection config, String property, BiConsumer<It, @NotNull Boolean> setter) {
        cond(it, config, property, ConfigurationSection::isBoolean, setter);
    }

    public static <It> void condInt(It it, ConfigurationSection config, String property, BiConsumer<It, @NotNull Integer> setter) {
        cond(it, config, property, ConfigurationSection::isInt, setter);
    }

    public static <It> void condLong(It it, ConfigurationSection config, String property, BiConsumer<It, @NotNull Long> setter) {
        cond(it, config, property, ConfigurationSection::isLong, setter);
    }

    public static <It> void condString(It it, ConfigurationSection config, String property, BiConsumer<It, String> setter) {
        cond(it, config, property, ConfigurationSection::isString, setter);
    }

    public static <It> void condStrings(It it, ConfigurationSection config, String property, BiConsumer<It, List<String>> setter) {
        List<String> ls;
        if (config.isList(property)) ls = config.getStringList(property);
        else if (config.isString(property)) ls = List.of(Objects.requireNonNull(config.getString(property)));
        else ls = List.of();
        setter.accept(it, ls);
    }

    public static ConfigurationSection init(@NotNull NamedBaseConfig.Builder<?, ?> it, ConfigurationSection section, @NotNull String module) {
        // set name
        it.name(module);

        // check section key exists
        boolean def = it instanceof MinecraftProviderConfig.Builder;
        if (section == null || !section.isSet(module))
            // only minecraft provider should default to "true" when not configured
            it.enable(def);
        else if (section.isBoolean(module))
            // handle shorthand configuration
            it.enable(section.getBoolean(module, def));
        else end:{
                // otherwise try to read 'enabled' attribute
                var mod = section.getConfigurationSection(module);
                if (mod == null) break end;

                it.enable(mod.getBoolean("enabled", def));
                if (it instanceof ProviderConfig.Builder<?, ?> pcb) {
                    condInt(pcb, mod, "autoReconnectDelay", ProviderConfig.Builder::autoReconnectDelay);
                }
                return mod;
            }
        return null;
    }

    public static void format(@NotNull FormatProviderConfig.Builder<?, ?> it, @NotNull ConfigurationSection section) {
        var formats = section.getConfigurationSection("format");
        if (formats == null) return;
        it.format(formats(formats));
    }

    public static Formats formats(ConfigurationSection formats) {
        var format = Formats.builder();
        condString(format, formats, "defaults", (builder, val) -> builder.defaults(Formats.Defaults.valueOf(val.toUpperCase())));
        condString(format, formats, "joinMessage", Formats.Builder::joinMessage);
        condString(format, formats, "leaveMessage", Formats.Builder::leaveMessage);
        condString(format, formats, "messageAuthor", Formats.Builder::messageAuthor);
        condString(format, formats, "messageContent", Formats.Builder::messageContent);
        condString(format, formats, "messageUserAvatar", Formats.Builder::messageUserAvatar);
        return format.build();
    }

    public static void mcEvent(@NotNull MinecraftProviderConfig.EventConfig.Builder<?, ?> it, @NotNull ConfigurationSection section) {
        condBoolean(it, section, "replace", MinecraftProviderConfig.EventConfig.Builder::replace);
        condStrings(it, section, "channels", MinecraftProviderConfig.EventConfig.Builder::channels);
    }

    public static void mcEvents(@NotNull MinecraftProviderConfig.Builder<?, ?> it, @NotNull ConfigurationSection config) {
        element(MinecraftProviderConfig.EventConfig::builder, config, "joinLeave", YmlConfigHelper::mcEvent, it::joinLeave);
        element(MinecraftProviderConfig.EventConfig::builder, config, "achievements", YmlConfigHelper::mcEvent, it::achievements);
    }

    public static void minecraft(@NotNull MinecraftProviderConfig.Builder<?, ?> it, @NotNull ConfigurationSection section) {
        format(it, section);
        mcEvents(it, section);
    }

    public static void rabbitmq(@NotNull RabbitMqProviderConfig.Builder<?, ?> it, @NotNull ConfigurationSection section) {
        condString(it, section, "rabbitUri", RabbitMqProviderConfig.Builder::rabbitUri);
        condString(it, section, "exchange", RabbitMqProviderConfig.Builder::exchange);
        condString(it, section, "exchangeType", RabbitMqProviderConfig.Builder::exchangeType);
    }

    public static void discord(DiscordProviderConfig.Builder<?, ?> it, ConfigurationSection section) {
        condString(it, section, "token", DiscordProviderConfig.Builder::token);
        format(it, section);
    }

    public static void channel(Channel.Builder<?, ?> it, ConfigurationSection section) {
        condString(it, section, "alias", Channel.Builder::alias);
        condString(it, section, "permission", Channel.Builder::permission);
        element(DiscordChannel::builder, section, "discord", YmlConfigHelper::discordChannel, it::discord);
        condBoolean(it, section, "publish", Channel.Builder::publish);
    }

    public static void discordChannel(DiscordChannel.Builder<?, ?> it, ConfigurationSection section) {
        condLong(it, section, "channelId", DiscordChannel.Builder::channelId);
        condString(it, section, "webhookUrl", DiscordChannel.Builder::webhookUrl);
        condString(it, section, "inviteUrl", DiscordChannel.Builder::inviteUrl);
        format(it, section);
    }
}
