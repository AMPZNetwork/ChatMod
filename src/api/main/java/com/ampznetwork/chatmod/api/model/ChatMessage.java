package com.ampznetwork.chatmod.api.model;

import com.ampznetwork.chatmod.api.util.TextComponentDeserializer;
import com.ampznetwork.chatmod.api.util.TextComponentSerializer;
import com.ampznetwork.libmod.api.entity.Player;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Setter;
import lombok.Value;
import lombok.experimental.NonFinal;
import net.kyori.adventure.text.TextComponent;
import org.jetbrains.annotations.Nullable;

@Value
@Setter
public class ChatMessage {
    @Nullable Player sender;
    String senderName;
    String messageString;
    @NonFinal String plaintext;
    @JsonSerialize(using = TextComponentSerializer.class)
    @JsonDeserialize(using = TextComponentDeserializer.class)
    @NonFinal TextComponent text;

    @Override
    public String toString() {
        return plaintext;
    }

    public void validateMutualExclusivity() {
        if ((sender == null) == (senderName == null))
            throw new IllegalArgumentException("Sender and SenderName cannot both be set or null");
    }
}
