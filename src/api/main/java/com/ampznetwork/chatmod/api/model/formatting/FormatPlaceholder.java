package com.ampznetwork.chatmod.api.model.formatting;

import com.ampznetwork.chatmod.api.model.module.ModuleContainer;
import com.ampznetwork.chatmod.api.model.protocol.ChatMessagePacket;
import org.comroid.api.func.util.Streams;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Objects;
import java.util.Spliterators;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public interface FormatPlaceholder {
    Pattern          PLACEHOLDER     = Pattern.compile("%(?<key>[a-zA-Z0-9_]+)%");
    ImmutableContext DEFAULT_CONTEXT = () -> Arrays.stream(DefaultPlaceholder.values());

    static FormatPlaceholder.Context override(DefaultPlaceholder placeholder, String value) {
        return new DefaultPlaceholder.ValueOverride().override(placeholder, value);
    }

    interface Pair {
        String getName();

        Stream<FormatPlaceholder.Pair> getFallback();

        Stream<String> streamValues(ModuleContainer mod, ChatMessagePacket packet);
    }

    interface ImmutableContext {
        Stream<FormatPlaceholder.Pair> stream();

        default String apply(ModuleContainer mod, ChatMessagePacket packet, String format) {
            final var lastEnd = new int[]{ 0 };
            var output = StreamSupport.<MatchResult>stream(Spliterators.spliteratorUnknownSize(new Iterator<>() {
                        final Matcher matcher = PLACEHOLDER.matcher(format);
                        boolean consumed = true;

                        @Override
                        public boolean hasNext() {
                            return !consumed || !(consumed = !matcher.find());
                        }

                        @Override
                        public MatchResult next() {
                            consumed = true;
                            return matcher.toMatchResult();
                        }
                    }, 0), false)
                    .sequential()
                    .flatMap(result -> stream().filter(placeholder -> placeholder.getName().equalsIgnoreCase(result.group("key")))
                            .flatMap(placeholder -> Stream.concat(placeholder.streamValues(mod, packet),
                                            placeholder.getFallback().flatMap(fallback -> fallback.streamValues(mod, packet)))
                                    .filter(Objects::nonNull)
                                    .findAny()
                                    .stream())
                            .sequential()
                            .flatMap(value -> {
                                var substring = format.substring(lastEnd[0], result.start());
                                lastEnd[0] = result.end();
                                return Stream.of(substring, value);
                            }))
                    .collect(Streams.atLeastOneOrElseGet(() -> format))
                    .collect(Collectors.joining());
            if (lastEnd[0] != 0) output += format.substring(lastEnd[0]);
            return output;
        }
    }

    interface Context extends ImmutableContext {

        Context override(DefaultPlaceholder placeholder, String value);
    }
}