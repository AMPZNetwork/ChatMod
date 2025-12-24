package com.ampznetwork.chatmod.api.util;

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
import org.comroid.api.text.Markdown;
import org.jetbrains.annotations.Nullable;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;

import static net.kyori.adventure.text.Component.*;

@Log
@Value
@NoArgsConstructor
public class ChatMessageParser {
    Set<Markdown>         activeMd    = new HashSet<>();
    Set<TextDecoration>   activeDecor = new HashSet<>();
    TextComponent.Builder component   = text();
    @NonFinal @Nullable TextColor     color = null;
    @NonFinal           StringBuilder buf   = new StringBuilder();

    @SuppressWarnings("UnnecessaryContinue")
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

                if (activeMd.contains(Markdown.Strikethrough)) {
                    push(Markdown.Strikethrough);
                    continue;
                } else activeMd.add(Markdown.Strikethrough);
            } else if (c == n) {
                // doubles
                switch (c) {
                    case '*':
                        // bold

                        i++; // advance to second char
                        if (activeMd.contains(Markdown.Bold)) {
                            push(Markdown.Bold);
                            continue;
                        } else activeMd.add(Markdown.Bold);
                        break;
                    case '_':
                        // underline

                        i++; // advance to second char
                        if (activeMd.contains(Markdown.Underline)) {
                            push(Markdown.Underline);
                            continue;
                        } else activeMd.add(Markdown.Underline);
                        break;
                    default:
                        buf.append(c);
                }
            } else if (c == '_' || c == '*') {
                // italic

                if (activeMd.contains(Markdown.Italic)) {
                    push(Markdown.Italic);
                    continue;
                } else activeMd.add(Markdown.Italic);
            } else if ((c == '&' || c == '§') && n != 0) {
                push(null);

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
            } else {
                urls:
                if (c == ' ') {
                    // terminate url if applicable
                    var index = buf.indexOf("https://");
                    if (index != -1) {
                        var url = buf.substring(index);
                        URI wrapper;
                        try {
                            wrapper = new URI(url);
                        } catch (URISyntaxException e) {
                            log.log(Level.FINE, "Invalid URI", e);
                            break urls;
                        }

                        buf.delete(index, buf.length());
                        push(null);

                        var urlComponent = text(wrapper.getHost()).decoration(TextDecoration.UNDERLINED, true)
                                .hoverEvent(HoverEvent.showText(text("Open URL:").appendNewline().append(text(url))))
                                .clickEvent(ClickEvent.openUrl(url));
                        component.append(urlComponent);

                        reset();
                        continue;
                    }
                }

                buf.append(c);
            }
        }
        if (!buf.isEmpty()) push(null);

        return component.build();
    }

    private void push(@Nullable Markdown closeMarkdown) {
        var str = buf.toString();
        if (!str.isBlank()) {
            var text = text().content(str);

            applyMcDecor(text);
            applyMd(text);
            component.append(text);
        }

        reset();
        if (closeMarkdown != null && !activeMd.remove(closeMarkdown)) log.fine("Could not remove closed markdown: " + closeMarkdown);
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
}
