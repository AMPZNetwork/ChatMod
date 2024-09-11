package com.ampznetwork.chatmod.core.formatting;

import com.ampznetwork.chatmod.api.ChatMod;
import com.ampznetwork.chatmod.api.formatting.MessageFormatter;
import com.ampznetwork.chatmod.api.model.ChatMessage;
import com.ampznetwork.libmod.api.entity.Player;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Singular;
import lombok.Value;
import lombok.experimental.FieldDefaults;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.comroid.api.Polyfill;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static net.kyori.adventure.text.Component.*;
import static net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer.*;

@Value
@Builder
public class ChatMessageFormatter implements MessageFormatter {
    public static final     boolean               DEFAULT_CASE_INSENSITIVE = true;
    @lombok.Builder.Default String                format                   = "[%server_name%] <%player_name%> %message%";
    @lombok.Builder.Default boolean               verbatimToObfuscated     = false;
    @lombok.Builder.Default boolean               caseInsensitive          = DEFAULT_CASE_INSENSITIVE;
    @lombok.Builder.Default String                replace                  = "***";
    @lombok.Builder.Default boolean               forceHttps               = false;
    @lombok.Builder.Default boolean               showDomainOnly           = false;
    @lombok.Builder.Default TextDecoration        decorate                 = TextDecoration.UNDERLINED;
    @Singular               List<MarkdownFeature> disableMarkdownFeatures;
    @Singular               List<Pattern>         patterns;

    public static ChatMessageFormatter of(Map<String, ?> config) {
        var builder = builder();
        if (config.get("scheme") instanceof String s) builder.format(s);
        if (config.get("verbatim_to_obfuscated") instanceof Boolean b) builder.verbatimToObfuscated(b);
        boolean caseInsensitive = DEFAULT_CASE_INSENSITIVE;
        if (config.get("case_insensitive") instanceof Boolean b) builder.caseInsensitive(caseInsensitive = b);
        if (config.get("replace") instanceof String s) builder.replace(s);
        if (config.get("force_https") instanceof Boolean b) builder.forceHttps(b);
        if (config.get("domain_only") instanceof Boolean b) builder.showDomainOnly(b);
        if (config.get("decorate") instanceof String s) builder.decorate(TextDecoration.valueOf(s.toUpperCase()));
        if (config.get("disable") instanceof List<?> ls)
            for (var s : ls) builder.disableMarkdownFeature(MarkdownFeature.valueOf(s.toString().toUpperCase()));
        if (config.get("patterns") instanceof List<?> ls)
            for (var s : ls) builder.pattern(Pattern.compile(s.toString(), caseInsensitive ? Pattern.CASE_INSENSITIVE : 0));
        return builder.build();
    }

    private static final String message_placeholder = "%message%";

    @Override
    public void accept(ChatMod mod, ChatMessage chatMessage) {
        var format  = mod.applyPlaceholders(chatMessage.getSender().getId(), this.format);
        var indexOf = format.indexOf(message_placeholder);

        var text = legacyAmpersand().deserialize(format.substring(0, indexOf))
                .append(convertMessage(mod, chatMessage.getSender(), chatMessage.getMessageString()))
                .append(legacyAmpersand().deserialize(format.substring(indexOf + message_placeholder.length())));

        chatMessage.setPlaintext(format.replace(message_placeholder, legacyAmpersand().serialize(text)))
                .setText(text);
    }

    private static final Map<@NotNull Character, NamedTextColor> McColorCodes  = Map.ofEntries(
            Map.entry('0', NamedTextColor.BLACK),
            Map.entry('1', NamedTextColor.DARK_BLUE),
            Map.entry('2', NamedTextColor.DARK_GREEN),
            Map.entry('3', NamedTextColor.DARK_AQUA),
            Map.entry('4', NamedTextColor.DARK_RED),
            Map.entry('5', NamedTextColor.DARK_PURPLE),
            Map.entry('6', NamedTextColor.GOLD),
            Map.entry('7', NamedTextColor.GRAY),
            Map.entry('8', NamedTextColor.DARK_GRAY),
            Map.entry('9', NamedTextColor.BLUE),
            Map.entry('a', NamedTextColor.GREEN),
            Map.entry('b', NamedTextColor.AQUA),
            Map.entry('c', NamedTextColor.RED),
            Map.entry('d', NamedTextColor.LIGHT_PURPLE),
            Map.entry('e', NamedTextColor.YELLOW),
            Map.entry('f', NamedTextColor.WHITE));
    private static final Map<@NotNull Character, TextDecoration> McFormatCodes = Map.of(
            'k', TextDecoration.OBFUSCATED,
            'l', TextDecoration.BOLD,
            'm', TextDecoration.STRIKETHROUGH,
            'n', TextDecoration.UNDERLINED,
            'o', TextDecoration.ITALIC);
    private static final Map<@NotNull String, MarkdownFeature>   MdLetterCodes = Map.of(
            "_", MarkdownFeature.ITALIC,
            "*", MarkdownFeature.BOLD,
            "~", MarkdownFeature.STRIKETHROUGH,
            "__", MarkdownFeature.UNDERLINE);

    private @NotNull Component convertMessage(ChatMod mod, Player player, String message) {
        var text = text();

        // apply regex
        for (var pattern : patterns)
            message = message.replaceAll(pattern.pattern(), replace);

        // parse markdown, formatting and urls
        final char[] chars = message.toCharArray();
        final var helper = new Object() {
            final     Map<MarkdownFeature, @NotNull Boolean> activeMd    = new HashMap<>();
            final     Map<TextDecoration, @NotNull Boolean>  activeDecor = new HashMap<>();
            @Nullable TextColor                              activeColor = null;
            @Nullable Key                                    activeFont  = null;
            TextComponent.Builder builder = text();
            String                buffer  = "", url = null;

            boolean onceOrTwice(boolean isTwice, MarkdownFeature once, MarkdownFeature twice) {
                toggle(isTwice ? twice : once);
                return isTwice;
            }

            void toggle(TextDecoration td) {
                toggle(activeDecor, td, f -> disableMarkdownFeatures.stream().noneMatch(mdf -> f.equals(mdf.kyori)));
            }

            void toggle(MarkdownFeature mdf) {
                toggle(activeMd, mdf, k -> !disableMarkdownFeatures.contains(k));
            }

            <K extends Enum<K>> void toggle(Map<K, @NotNull Boolean> map, K key, Predicate<K> filter) {
                var prevState = map.getOrDefault(key, false);
                var newState =
                        // base filter
                        filter.test(key)
                        // permission check
                        && mod.getLib().getPlayerAdapter().checkPermission(player.getId(),
                                "chatmod.format." + key.name().toLowerCase()).toBooleanOrElse(false)
                        // toggle
                        && Boolean.FALSE.equals(prevState);
                if (prevState && !newState)
                    flush(false);
                map.put(key, newState);
            }

            void flush(boolean clearFeatures) {
                Stream.concat(
                                // apply MD features
                                Arrays.stream(MarkdownFeature.values())
                                        .filter(mdf -> activeMd.getOrDefault(mdf, false))
                                        .map(mdf -> switch (mdf) {
                                            case BOLD -> TextDecoration.BOLD;
                                            case ITALIC -> TextDecoration.ITALIC;
                                            case UNDERLINE -> TextDecoration.UNDERLINED;
                                            case STRIKETHROUGH -> TextDecoration.STRIKETHROUGH;
                                            case HIDDEN_LINKS -> null;
                                            case VERBATIM -> {
                                                if (verbatimToObfuscated)
                                                    yield TextDecoration.OBFUSCATED;
                                                activeFont = Key.key("minecraft", "uniform");
                                                yield null;
                                            }
                                        }).flatMap(Stream::ofNullable),
                                // apply TD features
                                Arrays.stream(TextDecoration.values())
                                        .filter(td -> activeDecor.getOrDefault(td, false)))
                        .forEach(builder::decorate);

                // apply color
                if (activeColor != null)
                    builder.color(activeColor);

                // apply font
                if (activeFont != null)
                    builder.font(activeFont);

                // apply url
                if (url != null) {
                    if (forceHttps)
                        url = url.replaceAll("http://", "https://");
                    else if (buffer.isBlank())
                        buffer = showDomainOnly ? Polyfill.url(url).getHost() : url;
                    builder.clickEvent(ClickEvent.openUrl(url));
                }

                // text content
                builder.content(buffer);

                text.append(builder.build());

                if (clearFeatures)
                    clearFeatures();
                builder = text();
                buffer  = "";
                url     = null;
            }

            void clearFeatures() {
                activeMd.clear();
                activeDecor.clear();
                activeColor = null;
            }
        };
        for (var i = 0; i < chars.length; i++) {
            boolean last = i + 1 >= chars.length, urlStart = message.chars()
                    .skip(i)
                    .limit(8)
                    .mapToObj(c -> String.valueOf((char) c))
                    .collect(Collectors.joining())
                    .matches("https?://\\w?");
            char c = chars[i], n = last ? 0 : chars[i + 1];
            if (c == '_') {
                if (helper.activeDecor.values().stream().anyMatch(Boolean::booleanValue) || helper.activeColor != null)
                    helper.flush(true);
                if (helper.onceOrTwice('_' == n, MarkdownFeature.ITALIC, MarkdownFeature.UNDERLINE)) i += 1;
            } else if (c == '*') {
                if (helper.activeDecor.values().stream().anyMatch(Boolean::booleanValue) || helper.activeColor != null)
                    helper.flush(true);
                if (helper.onceOrTwice('*' == n, MarkdownFeature.ITALIC, MarkdownFeature.BOLD)) i += 1;
            } else if (c == '~') {
                if (helper.activeDecor.values().stream().anyMatch(Boolean::booleanValue) || helper.activeColor != null)
                    helper.flush(true);
                helper.toggle(MarkdownFeature.STRIKETHROUGH);
            } else if (c == '[') {
                helper.toggle(MarkdownFeature.HIDDEN_LINKS);
            } else if ((urlStart || helper.activeMd.getOrDefault(MarkdownFeature.HIDDEN_LINKS, false)) && helper.url == null) {
                // delimit display string
                if (c == ']' && n == '(') {
                    // format is valid
                    helper.toggle(MarkdownFeature.HIDDEN_LINKS);
                    helper.url = "";
                    i += 1;
                } else if (urlStart) {
                    // starts with "https://"
                    helper.flush(false);
                    helper.url = String.valueOf(c);
                } else
                    // append to display string
                    helper.buffer += c;
            } else if (helper.activeMd.getOrDefault(MarkdownFeature.HIDDEN_LINKS, false) || helper.url != null) {
                // validate format
                if (c == ' ') {
                    if (helper.activeMd.getOrDefault(MarkdownFeature.HIDDEN_LINKS, false)) {
                        // invalid format
                        helper.buffer += helper.url + ' ';
                        helper.url = null;
                    } else {
                        // delimit normal url
                        helper.activeDecor.put(TextDecoration.UNDERLINED, true);
                        helper.buffer += c;
                        helper.flush(false);
                    }
                } else if (c == ')') {
                    // delimit url
                    helper.flush(false);
                } else helper.url += c;
            } else if (c == '&') {
                if (i <= 1 || chars[i - 2] != '&')
                    helper.flush(false);
                if (n == 'r')
                    helper.flush(true);
                else if (McColorCodes.containsKey(n)) {
                    helper.activeColor = McColorCodes.get(n);
                } else if (McFormatCodes.containsKey(n)) {
                    helper.toggle(McFormatCodes.get(n));
                } else {
                    helper.buffer += c;
                    i -= 1;
                }
                i += 1;
            } else helper.buffer += c;
        }
        helper.flush(false);

        return text.build();
    }

    @Getter
    @RequiredArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
    public enum MarkdownFeature {
        BOLD(TextDecoration.BOLD),
        ITALIC(TextDecoration.ITALIC),
        UNDERLINE(TextDecoration.UNDERLINED),
        STRIKETHROUGH(TextDecoration.STRIKETHROUGH),
        HIDDEN_LINKS(null),
        VERBATIM(null);

        @Nullable TextDecoration kyori;
    }
}
