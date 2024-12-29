package com.ampznetwork.chatmod.api.model;

import com.ampznetwork.libmod.api.entity.Player;
import lombok.Setter;
import lombok.Value;
import lombok.experimental.NonFinal;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import org.jetbrains.annotations.Nullable;

import static net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer.*;

@Value
@Setter
public class ChatMessage {
    @Nullable Player sender;
    String senderName;
    String messageString;
    @NonFinal           TextComponent text;
    @Nullable @NonFinal TextComponent prepend;
    @Nullable @NonFinal TextComponent append;

    public ChatMessage(@Nullable Player sender, String senderName, String messageString, TextComponent text) {
        this(sender, senderName, messageString, null, text);
    }

    public ChatMessage(@Nullable Player sender, String senderName, String messageString, TextComponent prepend, TextComponent text) {
        this(sender, senderName, messageString, prepend, text, null);
    }

    public ChatMessage(
            @Nullable Player sender, String senderName, String messageString, @Nullable TextComponent prepend, TextComponent text,
            @Nullable TextComponent append
    ) {
        this.sender        = sender;
        this.senderName    = senderName;
        this.messageString = messageString;
        this.prepend = prepend != null ? prepend : Component.text("");
        this.text          = text;
        this.append  = append != null ? append : Component.text("");
    }

    public TextComponent getFullText() {
        return Component.text()
                .append(prepend)
                .append(text)
                .append(append)
                .build();
    }

    public String getPlaintext() {
        return plainText().serialize(getFullText());
    }

    @Override
    public String toString() {
        return getPlaintext();
    }

    public void validateMutualExclusivity() {
        if ((sender == null) == (senderName == null))
            throw new IllegalArgumentException("Sender and SenderName cannot both be set or null");
    }
}
