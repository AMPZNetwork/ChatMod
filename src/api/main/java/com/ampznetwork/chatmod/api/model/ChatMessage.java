package com.ampznetwork.chatmod.api.model;

import com.ampznetwork.libmod.api.entity.Player;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIncludeProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRawValue;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
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
    public static final                                                             ObjectMapper MAPPER = new ObjectMapper();
    @JsonProperty @JsonIncludeProperties({ "id", "name", "displayName" }) @Nullable Player       sender;
    @JsonProperty                                                                   String       senderName;
    @JsonProperty                                                                   String       messageString;
    @JsonRawValue @JsonSerialize(converter = KyoriToRawConverter.class) @JsonDeserialize(converter = RawToKyoriConverter.class) @JsonProperty @NonFinal
    TextComponent text;
    @JsonRawValue @JsonSerialize(converter = KyoriToRawConverter.class) @JsonDeserialize(converter = RawToKyoriConverter.class) @JsonProperty @NonFinal
    @Nullable TextComponent prepend;
    @JsonRawValue @JsonSerialize(converter = KyoriToRawConverter.class) @JsonDeserialize(converter = RawToKyoriConverter.class) @JsonProperty @NonFinal
    @Nullable TextComponent append;

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

    @JsonIgnore
    public TextComponent getFullText() {
        return Component.text().append(prepend).append(text).append(append).build();
    }

    @JsonIgnore
    public String getPlaintext() {
        return plainText().serialize(getFullText());
    }

    @Override
    public String toString() {
        return getPlaintext();
    }

    public void validateMutualExclusivity() {
        if ((sender == null) == (senderName == null)) throw new IllegalArgumentException("Sender and SenderName cannot both be set or null");
    }

    @Value
    public static class KyoriToRawConverter implements Converter<TextComponent, String> {
        @Override
        @SneakyThrows
        public String convert(TextComponent component) {
            return json().serialize(component);
        }

        @Override
        public JavaType getInputType(TypeFactory typeFactory) {
            return typeFactory.constructType(TextComponent.class);
        }

        @Override
        public JavaType getOutputType(TypeFactory typeFactory) {
            return typeFactory.constructType(String.class);
        }
    }

    @Value
    public static class RawToKyoriConverter implements Converter<JsonNode, TextComponent> {
        @Override
        public TextComponent convert(JsonNode json) {
            return (TextComponent) json().deserialize(json.toString());
        }

        @Override
        public JavaType getInputType(TypeFactory typeFactory) {
            return typeFactory.constructType(JsonNode.class);
        }

        @Override
        public JavaType getOutputType(TypeFactory typeFactory) {
            return typeFactory.constructType(TextComponent.class);
        }
    }
}
