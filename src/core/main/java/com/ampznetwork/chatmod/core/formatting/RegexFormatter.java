package com.ampznetwork.chatmod.core.formatting;

import com.ampznetwork.chatmod.api.formatting.MessageFormatter;
import com.ampznetwork.chatmod.api.model.ChatMessage;
import lombok.Builder;
import lombok.Singular;
import lombok.Value;

import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

@Value
@Builder
public class RegexFormatter implements MessageFormatter {
    public static final     boolean       DEFAULT_CASE_INSENSITIVE = true;
    @lombok.Builder.Default boolean       caseInsensitive          = DEFAULT_CASE_INSENSITIVE;
    @lombok.Builder.Default String        replace                  = "***";
    @Singular               List<Pattern> patterns;

    public static MessageFormatter of(Map<String, ?> config) {
        var     builder         = builder();
        boolean caseInsensitive = DEFAULT_CASE_INSENSITIVE;
        if (config.get("case_insensitive") instanceof Boolean b) builder.caseInsensitive(caseInsensitive = b);
        if (config.get("replace") instanceof String s) builder.replace(s);
        if (config.get("patterns") instanceof List<?> ls)
            for (var s : ls) builder.pattern(Pattern.compile(s.toString(), caseInsensitive ? Pattern.CASE_INSENSITIVE : 0));
        return builder.build();
    }

    @Override
    public void accept(ChatMessage chatMessage) {

    }
}
