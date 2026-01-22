package com.ampznetwork.chatmod.api.parse;

import com.ampznetwork.chatmod.api.model.config.channel.Channel;
import com.ampznetwork.chatmod.lite.model.abstr.ChatModConfig;
import com.ampznetwork.chatmod.lite.model.abstr.PlaceholderAdapter;
import com.ampznetwork.libmod.api.entity.Player;
import com.ampznetwork.libmod.api.util.Util;
import lombok.NoArgsConstructor;
import lombok.Value;
import lombok.experimental.NonFinal;
import lombok.extern.java.Log;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.comroid.api.text.Markdown;
import org.jetbrains.annotations.Nullable;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.regex.Pattern;

import static net.kyori.adventure.text.Component.*;

@Log
@Value
@NoArgsConstructor
public class ChatMessageParser {
    private static final Pattern URLS = Pattern.compile("https?://[^ ]+");

    public static MessageBundle parse(
            String plaintext, ChatModConfig config, Channel channel, @Nullable Player player,
            String senderName
    ) {
        var text = new ChatMessageParser().parse(plaintext);

        var split      = config.getFormattingScheme().split("%message%");
        var serverName = config.getServerName();
        var prefix = PlaceholderAdapter.Native.applyPlaceholders(serverName,
                channel.getDisplay(),
                senderName,
                player,
                split[0]);
        var suffix = split.length < 2
                     ? ""
                     : PlaceholderAdapter.Native.applyPlaceholders(serverName,
                             channel.getDisplay(),
                             senderName,
                             player,
                             split[1]);

        prefix = Util.Kyori.sanitize(prefix, LegacyComponentSerializer.legacyAmpersand());
        suffix = Util.Kyori.sanitize(suffix, LegacyComponentSerializer.legacyAmpersand());

        var prefixComponent = LegacyComponentSerializer.legacyAmpersand().deserialize(prefix);
        var suffixComponent = LegacyComponentSerializer.legacyAmpersand().deserialize(suffix);

        return new MessageBundle(text, prefixComponent, suffixComponent);
    }

    Set<Markdown>         activeMd    = new HashSet<>();
    Set<TextDecoration>   activeDecor = new HashSet<>();
    TextComponent.Builder component   = text();
    @NonFinal @Nullable TextColor     color = null;
    @NonFinal           StringBuilder buf   = new StringBuilder();

    public TextComponent parse(String plaintext) {
        char c, n;

        for (var i = 0; i < plaintext.length(); i++) {
            c = plaintext.charAt(i);
            n = i + 1 < plaintext.length() ? plaintext.charAt(i + 1) : 0;

            if (c == '~') {
                // strikethrough

                // allow both single and double
                if (n == '~')
                    // advance to second symbol
                    i++;

                toggle(Markdown.Strikethrough);
            } else if (c == n) {
                // doubles
                switch (c) {
                    case '*':
                        // bold

                        i++; // advance to second char
                        toggle(Markdown.Bold);
                        break;
                    case '_':
                        // underline

                        i++; // advance to second char
                        toggle(Markdown.Underline);
                        break;
                    default:
                        buf.append(c);
                }
            } else if (c == '_' || c == '*') {
                // italic

                toggle(Markdown.Italic);
            } else if ((c == '&' || c == '§') && n != 0) {
                push((Markdown) null);

                // start new mc format code
                var code = Character.toLowerCase(n);
                if (Character.isDigit(code) || (code >= 'a' && code <= 'f')) {
                    // color
                    color = switch (code) {
                        case '0' -> NamedTextColor.BLACK;
                        case '1' -> NamedTextColor.DARK_BLUE;
                        case '2' -> NamedTextColor.DARK_GREEN;
                        case '3' -> NamedTextColor.DARK_AQUA;
                        case '4' -> NamedTextColor.DARK_RED;
                        case '5' -> NamedTextColor.DARK_PURPLE;
                        case '6' -> NamedTextColor.GOLD;
                        case '7' -> NamedTextColor.GRAY;
                        case '8' -> NamedTextColor.DARK_GRAY;
                        case '9' -> NamedTextColor.BLUE;
                        case 'a' -> NamedTextColor.GREEN;
                        case 'b' -> NamedTextColor.AQUA;
                        case 'c' -> NamedTextColor.RED;
                        case 'd' -> NamedTextColor.LIGHT_PURPLE;
                        case 'e' -> NamedTextColor.YELLOW;
                        case 'f' -> NamedTextColor.WHITE;
                        default -> throw new IllegalStateException("Unexpected value: " + code);
                    };
                } else {
                    // decor
                    activeDecor.add(switch (code) {
                        case 'k' -> TextDecoration.OBFUSCATED;
                        case 'l' -> TextDecoration.BOLD;
                        case 'm' -> TextDecoration.STRIKETHROUGH;
                        case 'n' -> TextDecoration.UNDERLINED;
                        case 'o' -> TextDecoration.ITALIC;
                        default -> throw new IllegalStateException("Unexpected value: " + code);
                    });
                }
            } else buf.append(c);
        }
        if (!buf.isEmpty()) push((Markdown) null);

        return component.build();
    }

    private void toggle(Markdown markdown) {
        if (activeMd.contains(markdown)) {
            push(markdown);
        } else {
            push((Markdown) null);
            activeMd.add(markdown);
        }
    }

    private void push(@Nullable Markdown closeMarkdown) {
        var str = buf.toString();

        // scan for urls
        var matcher = URLS.matcher(str);
        int lastEnd = -1;
        while (matcher.find()) {
            var buf = str.substring(0, matcher.start());
            if (!buf.isBlank()) push(buf);

            URI url;
            var urlText = matcher.group();
            try {
                url = new URI(urlText);
            } catch (URISyntaxException e) {
                log.log(Level.FINE, "Invalid URI", e);
                push(urlText);
                continue;
            }

            var urlComponent = text().content(url.getHost())
                    .decoration(TextDecoration.UNDERLINED, true)
                    .hoverEvent(HoverEvent.showText(text("Open URL:").appendNewline().append(text(urlText))))
                    .clickEvent(ClickEvent.openUrl(urlText));
            component.append(urlComponent.build());
            lastEnd = matcher.end();
        }
        if (lastEnd != -1) {
            str = str.substring(lastEnd);
        }
        // apply text decorations
        if (!str.isBlank()) push(str);

        reset();
        if (closeMarkdown != null && !activeMd.remove(closeMarkdown)) log.fine("Could not remove closed markdown: " + closeMarkdown);
    }

    private void push(String str) {
        var text = text().content(str);

        applyMcDecor(text);
        applyMd(text);
        component.append(text);
    }

    private void reset() {
        buf = new StringBuilder();
        activeDecor.clear();
    }

    private void applyMcDecor(TextComponent.Builder text) {
        for (var decor : activeDecor)
            text.decoration(decor, true);
    }

    private void applyMd(TextComponent.Builder text) {
        for (var markdown : activeMd)
            switch (markdown) {
                case Italic -> text.decoration(TextDecoration.ITALIC, true);
                case Bold -> text.decoration(TextDecoration.BOLD, true);
                case Underline -> text.decoration(TextDecoration.UNDERLINED, true);
                case Strikethrough -> text.decoration(TextDecoration.STRIKETHROUGH, true);
                default -> log.fine("Skipping unsupported markdown formatter: " + markdown);
            }
    }

    public record MessageBundle(TextComponent text, TextComponent prefix, TextComponent suffix) {}
}
