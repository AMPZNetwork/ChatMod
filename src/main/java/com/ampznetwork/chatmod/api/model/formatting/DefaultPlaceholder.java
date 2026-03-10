package com.ampznetwork.chatmod.api.model.formatting;

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
import java.util.stream.Stream;

@Getter
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public enum DefaultPlaceholder implements FormatPlaceholder.Pair, Named {
    SERVER_NAME {
    }, CHANNEL_NAME {
    }, PLAYER_ID {
    }, PLAYER_NAME {
    }, PLAYER_DISPLAYNAME(PLAYER_NAME) {
    }, MESSAGE {
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

    public @Nullable Stream<FormatPlaceholder.Pair> getFallback() {
        return Stream.of(fallback);
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
