package com.ampznetwork.chatmod.api.model.formatting;

import java.util.Arrays;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public interface FormatPlaceholder {
    Pattern          PLACEHOLDER     = Pattern.compile("%(?<key>[a-zA-Z0-9_]+)%");
    ImmutableContext DEFAULT_CONTEXT = () -> Arrays.stream(DefaultPlaceholder.values());

    static FormatPlaceholder.Context override(DefaultPlaceholder placeholder, String value) {
        return new DefaultPlaceholder.ValueOverride().override(placeholder, value);
    }

    interface Pair {
        String getName();

        Stream<FormatPlaceholder.Pair> getFallback();
    }

    interface ImmutableContext {
        Stream<FormatPlaceholder.Pair> stream();
    }

    interface Context extends ImmutableContext {

        Context override(DefaultPlaceholder placeholder, String value);
    }
}