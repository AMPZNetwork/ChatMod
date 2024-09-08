package com.ampznetwork.chatmod.core.formatting;

import com.ampznetwork.chatmod.api.ChatMod;
import com.ampznetwork.chatmod.api.formatting.MessageFormatter;
import com.ampznetwork.chatmod.api.model.ChatMessage;
import lombok.Builder;
import lombok.Singular;
import lombok.Value;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import static net.kyori.adventure.text.Component.*;
import static net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer.*;

@Value
@Builder
public class ChatMessageFormatter implements MessageFormatter {
    public static final     Pattern PLACEHOLDER_PATTERN      = Pattern.compile("%([\\w-_.:]+)%");
    public static final     Pattern URL_PATTERN      = Pattern.compile("");
    public static final     boolean DEFAULT_CASE_INSENSITIVE = true;
    @lombok.Builder.Default String        format                   = "[%server_name%] <%player_name%> %message%";
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
        var format = mod.applyPlaceholders(chatMessage.getSender().getId(), this.format);
        var indexOf = format.indexOf(message_placeholder);

        var text = legacyAmpersand().deserialize(format.substring(0,indexOf))
                .append(convertMessage(mod,chatMessage.getMessageString()))
                .append(legacyAmpersand().deserialize(format.substring(indexOf + message_placeholder.length())));

        chatMessage.setPlaintext(format.replace(message_placeholder, legacyAmpersand().serialize(text)))
                .setText(text);
    }

    private @NotNull Component convertMessage(ChatMod mod, String message) {
        var text = text();



        return text.build();
    }

    public enum MarkdownFeature {BOLD, CURSIVE, UNDERLINE, STRIKETHROUGH, HIDDEN_LINKS, VERBATIM}
}
