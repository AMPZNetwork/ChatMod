package com.ampznetwork.chatmod.api.util;

import lombok.NoArgsConstructor;
import lombok.Value;
import lombok.experimental.NonFinal;
import lombok.extern.java.Log;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.comroid.api.text.Markdown;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Set;

import static net.kyori.adventure.text.Component.*;

@Log
@Value
@NoArgsConstructor
public class ChatMessageParser {
    public static void main(String[] args) {
        var strings = """
                *italic text &aGreen inside*
                **bold ~~strike~~ end**
                __underline &cRed__ text*
                ~~strikethrough &lbold~~ done*
                *mix __underline *nested italic* end__ tail*
                **double *single _mix_* end**
                ~~double ~~nested~~ strike~~
                &aGreen text &bAqua swap &cRed
                &lBold &oItalic &nUnder &mStrike
                ***italic bold combo***
                *__combo of underline and italic__*
                *italic &kobf &lbold decor*
                **bold &eYellow &lBoldDecor**
                __underline &oItalicDecor__
                ~~strike &9Blue &lBold~~ test
                &fWhite *italic &cRed*
                &6Gold **bold &mstrike decor**
                *italic __underline &lbold__ end*
                **bold ~~strike *italic*~~ layer**
                ~~&aGreen *nested &lBold*~~ done
                _italic __underline__ mix_
                **bold __underline *italic* nested__ end**
                ~~strike *italic __underline__* done~~
                __under ~~strike &cRed~~ end__
                *italic &cRed __underline__*
                **bold &aGreen ~~strike~~ ok**
                ~~strike &eYellow *italic &oDecor*~~
                *italic ~~strike __underline__~~ final*
                __underline *italic &kObf* end__
                **bold *italic ~~strike~~* end**
                *italic &bAqua &lBoldDecor end*
                **&cRed bold text**
                *__underlineItalic &dPink__*
                ~~&6GoldStrike &lBoldDecor~~
                _italic &nUnderlineDecor_
                **bold __underline *italic~~~* ok__**
                ~~double~~~~toggle~~done
                *italic __underline ~~strike~~ now__*
                &bAqua *italic &nUnderline* end
                __underline *italic &cRed ~~strike~~* final__
                *italic &aGreen &lBold &oItalic*
                **bold *italic &9Blue* next**
                ~~strike __underline &mStrikeDecor__ final~~
                &dPink __underline *italic*__
                _italic **bold &lBoldDecor** tail_
                **bold ~~strike &kObfuscated~~ more**
                __underline *italic &fWhite* done__
                *italic ~~strike __underline &eYellow__~~*
                **bold *italic &mStrike &kObf* end**
                ~~strike &bAqua *italic __underline__*~~
                __underline ~~strike &oItalic~~ end__
                *italic __underline ~~strike~~ &cRed__*
                **bold &eYellow &nUnderline &lBoldDecor**
                _italic &aGreen &lBoldDecor_
                __underline **bold *italic*** done__
                ~~strike *italic __underline__ end*~~
                *italic **bold &oItalicDecor*** now
                __underline ~~strike &bAqua~~ test__
                **bold *italic &kObfuscated* tail**
                ~~strike &6Gold &lBoldDecor text~~
                *italic &fWhite __underline &mStrikeDecor__*
                __underline *italic &9Blue* more__
                **bold __underline &oItalicDecor__ end**
                ~~&cRed strike *italic* ok~~
                *italic **bold __underline__** test*
                __underline ~~strike *italic*~~ stack__
                *italic ~~strike &dPink &lBoldDecor~~*
                **bold *italic __underline &nUnderlineDecor__* end**
                ~~strike *italic &eYellow* done~~
                __underline *italic &6Gold* tail__
                *italic __underline &bAqua *nested italic* end__*
                **bold ~~strike &cRed~~ after**
                _italic &9Blue &oItalicDecor_
                ~~strike **bold &mStrikeDecor** over~~
                __underline &aGreen _italic_ finish__
                *italic **bold ~~strike~~** wrap*
                **bold &lBoldDecor *italic &kObf*** end
                ~~strike __underline &oItalicDecor__ close~~
                __underline *italic &dPink &lBoldDecor* end__
                **bold ~~strike &eYellow _italic_~~ done**
                ~~strike *italic &bAqua __underline__* end~~
                *italic __underline &cRed ~~strike~~__ ok*
                **bold &fWhite &nUnderline decor**
                __underline _italic **bold**_ done__
                ~~strike *italic **bold** end*~~
                *italic &lBoldDecor __underline__ done*
                **bold ~~strike &6Gold &kObf~~ layer**
                __underline *italic &mStrike &oItalicDecor*__
                ~~strike __underline &9Blue *italic*__ test~~
                *italic &dPink &nUnderlineDecor*
                __underline *italic **bold &cRed*** end__
                **bold *italic ~~strike &bAqua~~* tail**
                ~~strike &aGreen __underline *italic*__~~
                *italic &eYellow __underline ~~strike~~__ final*
                **bold &mStrikeDecor &oItalic text**
                __underline ~~strike &lBoldDecor~~ near__
                ~~strike *italic &fWhite* close~~
                _italic &6Gold &mStrikeDecor tail_
                **bold *italic &aGreen &kObf* mix**
                ~~strike __underline &dPink &oItalicDecor__ end~~
                """;

        for (var line : strings.split("\r?\n")) {
            var result = new ChatMessageParser().parse(line);
            var legacy = LegacyComponentSerializer.legacyAmpersand().serialize(result);
            System.out.println("New attempt:");
            System.out.println(" -> input: " + line);
            System.out.println(" -> parse: " + legacy);
        }
    }
    Set<Markdown>         activeMd    = new HashSet<>();
    Set<TextDecoration>   activeDecor = new HashSet<>();
    TextComponent.Builder component   = text();
    @NonFinal @Nullable TextColor     color = null;
    @NonFinal           StringBuilder buf   = new StringBuilder();

    @SuppressWarnings("UnnecessaryContinue")
    public Component parse(String plaintext) {
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
            } else buf.append(c);
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

        buf = new StringBuilder();
        activeDecor.clear();
        if (closeMarkdown != null && !activeMd.remove(closeMarkdown)) log.fine("Could not remove closed markdown: " + closeMarkdown);
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
