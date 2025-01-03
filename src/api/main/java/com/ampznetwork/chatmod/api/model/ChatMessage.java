package com.ampznetwork.chatmod.api.model;

import com.ampznetwork.libmod.api.entity.Player;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.databind.util.Converter;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import lombok.Value;
import lombok.experimental.NonFinal;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import org.jetbrains.annotations.Nullable;

import static net.kyori.adventure.text.serializer.json.JSONComponentSerializer.*;
import static net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer.*;

@Data
@NoArgsConstructor
public class ChatMessage {
    public static final               ObjectMapper  MAPPER = new ObjectMapper();
    @JsonProperty @Nullable           Player        sender;
    @JsonProperty                     String        senderName;
    @JsonProperty                     String        messageString;
    @JsonSerialize(converter = KyoriToJacksonConverter.class) @JsonDeserialize(converter = JacksonToKyoriConverter.class)
    @JsonProperty @NonFinal           TextComponent text;
    @JsonSerialize(converter = KyoriToJacksonConverter.class) @JsonDeserialize(converter = JacksonToKyoriConverter.class)
    @JsonProperty @Nullable @NonFinal TextComponent prepend;
    @JsonSerialize(converter = KyoriToJacksonConverter.class) @JsonDeserialize(converter = JacksonToKyoriConverter.class)
    @JsonProperty @Nullable @NonFinal TextComponent append;

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

    @Value
    public static class KyoriToJacksonConverter implements Converter<TextComponent, ObjectNode> {
        @Override
        @SneakyThrows
        public ObjectNode convert(TextComponent component) {
            return (ObjectNode) MAPPER.readTree(json().serialize(component));
        }

        @Override
        public JavaType getInputType(TypeFactory typeFactory) {
            return typeFactory.constructType(TextComponent.class);
        }

        @Override
        public JavaType getOutputType(TypeFactory typeFactory) {
            return typeFactory.constructType(ObjectNode.class);
        }
    }

    @Value
    public static class JacksonToKyoriConverter implements Converter<ObjectNode, TextComponent> {
        @Override
        public TextComponent convert(ObjectNode value) {
            return (TextComponent) json().deserialize(value.toString());
        }

        @Override
        public JavaType getInputType(TypeFactory typeFactory) {
            return typeFactory.constructType(ObjectNode.class);
        }

        @Override
        public JavaType getOutputType(TypeFactory typeFactory) {
            return typeFactory.constructType(TextComponent.class);
        }
    }
}
