package com.ampznetwork.chatmod.core.formatting;

import com.ampznetwork.chatmod.api.formatting.MessageFormatter;
import com.ampznetwork.chatmod.api.model.ChatMessage;
import lombok.Builder;
import lombok.Value;
import net.kyori.adventure.text.format.TextDecoration;

import java.util.Map;

@Value
@Builder
public class UrlFormatter implements MessageFormatter {
    @lombok.Builder.Default boolean        forceHttps     = false;
    @lombok.Builder.Default boolean        showDomainOnly = false;
    @lombok.Builder.Default TextDecoration decorate       = TextDecoration.UNDERLINED;

    public static MessageFormatter of(Map<String, ?> config) {
        var builder = builder();
        if (config.get("force_https") instanceof Boolean b) builder.forceHttps(b);
        if (config.get("domain_only") instanceof Boolean b) builder.showDomainOnly(b);
        if (config.get("decorate") instanceof String s) builder.decorate(TextDecoration.valueOf(s.toUpperCase()));
        return builder.build();
    }

    @Override
    public void accept(ChatMessage chatMessage) {

    }
}
