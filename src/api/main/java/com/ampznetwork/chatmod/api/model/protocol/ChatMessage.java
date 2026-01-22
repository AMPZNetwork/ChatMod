package com.ampznetwork.chatmod.api.model.protocol;

import com.ampznetwork.chatmod.api.parse.ChatMessageParser;
import com.ampznetwork.libmod.api.entity.Player;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
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
import net.kyori.adventure.text.serializer.gson.impl.JSONComponentSerializerProviderImpl;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.jetbrains.annotations.Nullable;

import static net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer.*;

@Data
@NoArgsConstructor
@JsonIgnoreProperties({ "messageString", "contentPlaintext" })
public class ChatMessage {
    public static final ObjectMapper MAPPER = new ObjectMapper();

    @JsonProperty @JsonIncludeProperties({ "id", "name" }) @Nullable                          Player        sender;
    @JsonProperty                                                                             String        senderName;
    @JsonRawValue @JsonSerialize(converter = KyoriToRawConverter.class)
    @JsonDeserialize(converter = RawToKyoriConverter.class) @JsonProperty @NonFinal           TextComponent text;
    @JsonRawValue @JsonSerialize(converter = KyoriToRawConverter.class)
    @JsonDeserialize(converter = RawToKyoriConverter.class) @JsonProperty @NonFinal @Nullable TextComponent prepend;
    @JsonRawValue @JsonSerialize(converter = KyoriToRawConverter.class)
    @JsonDeserialize(converter = RawToKyoriConverter.class) @JsonProperty @NonFinal @Nullable TextComponent append;

    @Deprecated(forRemoval = true)
    public ChatMessage(@Nullable Player sender, String senderName, String contentPlaintext, TextComponent text) {
        this(sender, senderName, new ChatMessageParser.MessageBundle(null, text, null));
    }

    @Deprecated(forRemoval = true)
    public ChatMessage(
            @Nullable Player sender, String senderName, String contentPlaintext, TextComponent prepend,
            TextComponent text
    ) {
        this(sender, senderName, new ChatMessageParser.MessageBundle(prepend, text, null));
    }

    @Deprecated(forRemoval = true)
    public ChatMessage(
            @Nullable Player sender, String senderName, String contentPlaintext, @Nullable TextComponent prepend,
            TextComponent text, @Nullable TextComponent append
    ) {
        this(sender, senderName, new ChatMessageParser.MessageBundle(prepend, text, append));
    }

    public ChatMessage(@Nullable Player sender, String senderName, TextComponent text) {
        this(sender, senderName, new ChatMessageParser.MessageBundle(null, text, null));
    }

    public ChatMessage(@Nullable Player sender, String senderName, ChatMessageParser.MessageBundle bundle) {
        this.sender     = sender;
        this.senderName = senderName;
        this.prepend    = bundle.prefix();
        this.text       = bundle.text();
        this.append     = bundle.suffix();
    }

    @JsonIgnore
    public TextComponent getFullText() {
        var text = Component.text();
        if (prepend != null) text.append(prepend);
        text.append(this.text);
        if (append != null) text.append(append);
        return text.build();
    }

    @JsonIgnore
    @Deprecated(forRemoval = true)
    public String getPlaintext() {
        return plainText().serialize(getFullText());
    }

    @JsonIgnore
    @Deprecated(forRemoval = true)
    public String getMessageString() {
        return PlainTextComponentSerializer.plainText().serialize(text);
    }

    @Value
    public static class KyoriToRawConverter implements Converter<TextComponent, String> {
        JSONComponentSerializerProviderImpl provider = new JSONComponentSerializerProviderImpl();

        @Override
        @SneakyThrows
        public String convert(TextComponent component) {
            return component == null ? "" : provider.instance().serialize(component);
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
        JSONComponentSerializerProviderImpl provider = new JSONComponentSerializerProviderImpl();

        @Override
        public TextComponent convert(JsonNode json) {
            return json == null || json.isNull()
                   ? Component.text("")
                   : (TextComponent) provider.instance().deserialize(json.toString());
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
