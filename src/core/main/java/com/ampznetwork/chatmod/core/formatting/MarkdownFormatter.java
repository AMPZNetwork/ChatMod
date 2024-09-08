package com.ampznetwork.chatmod.core.formatting;

import com.ampznetwork.chatmod.api.formatting.MessageFormatter;
import com.ampznetwork.chatmod.api.model.ChatMessage;
import lombok.Builder;
import lombok.Singular;
import lombok.Value;

import java.util.List;
import java.util.Map;

@Value
@Builder
public class MarkdownFormatter implements MessageFormatter {
    @lombok.Builder.Default Style         style                = Style.STANDARD;
    @lombok.Builder.Default boolean       verbatimToObfuscated = false;
    @Singular               List<Feature> disableFeatures;

    public static MessageFormatter of(Map<String, ?> config) {
        var builder = builder();
        if (config.get("style") instanceof String s) builder.style(Style.valueOf(s.toUpperCase()));
        if (config.get("verbatim_to_obfuscated") instanceof Boolean b) builder.verbatimToObfuscated(b);
        if (config.get("disable") instanceof List<?> ls)
            for (var s : ls) builder.disableFeature(Feature.valueOf(s.toString().toUpperCase()));
        return builder.build();
    }

    @Override
    public void accept(ChatMessage chatMessage) {

    }

    public enum Style {STANDARD, DISCORD}

    public enum Feature {BOLD, CURSIVE, UNDERLINE, STRIKETHROUGH, HIDDEN_LINKS, VERBATIM}
}
