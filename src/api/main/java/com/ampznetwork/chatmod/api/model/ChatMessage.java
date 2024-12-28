package com.ampznetwork.chatmod.api.model;

import com.ampznetwork.chatmod.api.util.TextComponentDeserializer;
import com.ampznetwork.chatmod.api.util.TextComponentSerializer;
import com.ampznetwork.libmod.api.entity.Player;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
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
    @JsonSerialize(using = TextComponentSerializer.class)
    @JsonDeserialize(using = TextComponentDeserializer.class)
    @NonFinal TextComponent prefix;
    @NonFinal TextComponent text;
    @NonFinal TextComponent suffix;

    public ChatMessage(@Nullable Player sender, String senderName, String messageString, TextComponent text) {
        this(sender, senderName, messageString, null, text);
    }

    public ChatMessage(@Nullable Player sender, String senderName, String messageString, TextComponent prefix, TextComponent text) {
        this(sender, senderName, messageString, prefix, text, null);
    }

    public ChatMessage(
            @Nullable Player sender, String senderName, String messageString, @Nullable TextComponent prefix, TextComponent text,
            @Nullable TextComponent suffix
    ) {
        this.sender        = sender;
        this.senderName    = senderName;
        this.messageString = messageString;
        this.prefix        = prefix != null ? prefix : Component.text("");
        this.text          = text;
        this.suffix        = suffix != null ? suffix : Component.text("");
    }

    public TextComponent getFullText() {
        return Component.text()
                .append(prefix)
                .append(text)
                .append(suffix)
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
