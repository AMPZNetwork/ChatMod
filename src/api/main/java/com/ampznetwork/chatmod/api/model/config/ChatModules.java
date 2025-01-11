package com.ampznetwork.chatmod.api.model.config;

import com.ampznetwork.chatmod.api.model.config.discord.DiscordBot;
import com.ampznetwork.chatmod.api.model.config.discord.IFormatContext;
import com.ampznetwork.chatmod.api.model.config.format.Formats;
import lombok.AccessLevel;
import lombok.Builder.Default;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Singular;
import lombok.experimental.SuperBuilder;
import org.comroid.api.attr.Named;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;

import java.util.Collection;

@Data
@SuperBuilder
@NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
public final class ChatModules {
    @Nullable @Default Formats                  defaultFormat = null;
    @Nullable @Default LogProviderConfig        log           = null;
    @Nullable @Default MinecraftProviderConfig  minecraft     = null;
    @Nullable @Default NativeProviderConfig     builtin       = null;
    @Nullable @Default AurionChatProviderConfig aurionChat    = null;
    @Nullable @Default DiscordProviderConfig    discord       = null;

    @Data
    @SuperBuilder
    @NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
    public static abstract class NamedBaseConfig implements Named {
        protected @Default boolean enabled = true;
        protected @NotNull String  name;
    }

    @Data
    @SuperBuilder
    @NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
    public static abstract class ProviderConfig extends NamedBaseConfig {
        protected @Nullable @Default                                 String endpointName       = null;
        @Range(from = -1, to = Integer.MAX_VALUE) protected @Default int    autoReconnectDelay = 5;

        public abstract String providerType();
    }

    @Data
    @SuperBuilder
    @NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
    public static class LogProviderConfig extends ProviderConfig {
        @Override
        public String providerType() {
            return "log";
        }
    }

    @Data
    @SuperBuilder
    @NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
    public static abstract class FormatProviderConfig extends ProviderConfig {
        protected @Nullable @Default Formats defaultFormat = null;
    }

    @Data
    @SuperBuilder
    @NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
    public static class MinecraftProviderConfig extends FormatProviderConfig implements IFormatContext {
        protected @Nullable @Default EventConfig joinLeave    = null;
        protected @Nullable @Default EventConfig achievements = null;

        @Override
        public String providerType() {
            return "minecraft";
        }

        @Data
        @SuperBuilder
        @NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
        public static class EventConfig extends NamedBaseConfig {
            protected @Default  boolean            replace = true;
            protected @Singular Collection<String> channels;
        }
    }

    @Data
    @SuperBuilder
    @NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
    public static abstract class RabbitMqProviderConfig extends ProviderConfig {
        protected @NotNull  String rabbitUri;
        protected @Nullable String exchange;
        protected @Default  String exchangeType = "fanout";
    }

    @Data
    @SuperBuilder
    @NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
    public static class NativeProviderConfig extends RabbitMqProviderConfig {
        protected @NotNull @Default String exchange = "minecraft.chat";

        @Override
        public final String providerType() {
            return "rabbitmq";
        }
    }

    @Data
    @SuperBuilder
    @NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
    public static class AurionChatProviderConfig extends RabbitMqProviderConfig {
        protected @NotNull @Default String exchange = "aurion.chat";

        @Override
        public final String providerType() {
            return "aurionchat";
        }
    }

    @Data
    @SuperBuilder
    @NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
    public static class DiscordProviderConfig extends FormatProviderConfig implements DiscordBot {
        protected @NotNull String discordToken;

        @Override
        public final String providerType() {
            return "discord";
        }
    }
}
