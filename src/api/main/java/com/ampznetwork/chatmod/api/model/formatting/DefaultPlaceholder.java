package com.ampznetwork.chatmod.api.model.formatting;

import com.ampznetwork.chatmod.api.model.module.ModuleContainer;
import com.ampznetwork.chatmod.api.model.protocol.ChatMessagePacket;
import com.ampznetwork.libmod.api.entity.Player;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Value;
import lombok.experimental.FieldDefaults;
import org.comroid.api.attr.Named;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;

@Getter
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public enum DefaultPlaceholder implements FormatPlaceholder.Pair, Named {
    SERVER_NAME {
        @Override
        public Stream<String> streamValues(ModuleContainer mod, ChatMessagePacket packet) {
            return Stream.ofNullable(mod.getServerName());
        }
    }, CHANNEL_NAME {
        @Override
        public Stream<String> streamValues(ModuleContainer mod, ChatMessagePacket packet) {
            return Stream.ofNullable(packet.getChannel());
        }
    }, PLAYER_ID {
        @Override
        public Stream<String> streamValues(ModuleContainer mod, ChatMessagePacket packet) {
            return Stream.ofNullable(packet.getMessage().getSender()).map(Player::getId).map(UUID::toString);
        }
    }, PLAYER_NAME {
        @Override
        public Stream<String> streamValues(ModuleContainer mod, ChatMessagePacket packet) {
            return Stream.ofNullable(packet.getMessage().getSender()).map(Player::getName);
        }
    }, PLAYER_DISPLAYNAME(PLAYER_NAME) {
        @Override
        public Stream<String> streamValues(ModuleContainer mod, ChatMessagePacket packet) {
            return Stream.ofNullable(packet.getMessage().getSender()).map(Player::getId).map(mod.getPlayerAdapter()::getDisplayName);
        }
    }, MESSAGE {
        @Override
        public Stream<String> streamValues(ModuleContainer mod, ChatMessagePacket packet) {
            return Stream.ofNullable(packet.getMessage().getMessageString());
        }
    };

    @Nullable DefaultPlaceholder fallback;

    DefaultPlaceholder() {
        this(null);
    }

    DefaultPlaceholder(@Nullable DefaultPlaceholder fallback) {
        this.fallback = fallback;
    }

    @Override
    public String getName() {
        return toString();
    }

    @Override
    public String toString() {
        return name().toLowerCase();
    }

    @Value
    private class OverrideImpl implements FormatPlaceholder.Pair {
        String value;

        @Override
        public String getName() {
            return DefaultPlaceholder.this.getName();
        }

        @Override
        public Stream<FormatPlaceholder.Pair> getFallback() {
            return Stream.<FormatPlaceholder.Pair>of(DefaultPlaceholder.this, DefaultPlaceholder.this.fallback).filter(Objects::nonNull);
        }

        @Override
        public Stream<String> streamValues(ModuleContainer mod, ChatMessagePacket packet) {
            return Stream.concat(Stream.of(value), getFallback().flatMap(fallback -> fallback.streamValues(mod, packet)));
        }
    }

    @Value
    static class ValueOverride implements FormatPlaceholder.Context {
        Set<FormatPlaceholder.Pair> overrides = new HashSet<>();

        @Override
        public Stream<FormatPlaceholder.Pair> stream() {
            return Stream.concat(overrides.stream(), Arrays.stream(values()));
        }

        @Override
        public FormatPlaceholder.Context override(DefaultPlaceholder placeholder, String value) {
            overrides.add(placeholder.new OverrideImpl(value));
            return this;
        }
    }
}
