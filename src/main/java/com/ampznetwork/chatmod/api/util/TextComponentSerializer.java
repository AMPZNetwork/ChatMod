package com.ampznetwork.chatmod.api.util;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.TextDecoration;
import org.comroid.api.func.util.Streams;

import java.io.IOException;
import java.util.stream.Stream;

public class TextComponentSerializer extends JsonSerializer<TextComponent> {
    @Override
    public void serialize(TextComponent component, JsonGenerator gen, SerializerProvider seri) throws IOException {
        gen.writeStartArray();

        for (var textComponent : Stream.concat(Stream.of(component),
                        component.children().stream().flatMap(Streams.cast(TextComponent.class)))
                .toList()) {
            gen.writeStartObject();
            writeComponent(gen, textComponent);
            gen.writeEndObject();
        }

        gen.writeEndArray();
    }

    private void writeComponent(JsonGenerator gen, TextComponent component) throws IOException {
        gen.writeStringField("text", component.content());

        // decoration
        var color = component.color();
        if (color != null)
            gen.writeStringField("color", color.asHexString());
        for (var entry : component.decorations().entrySet())
            gen.writeBooleanField(entry.getKey().toString(), entry.getValue() == TextDecoration.State.TRUE);

        // events
        var clickEvent = component.clickEvent();
        if (clickEvent != null) {
            gen.writeObjectFieldStart("clickEvent");
            gen.writeStringField("action", clickEvent.action().toString());
            gen.writeStringField("value", clickEvent.value());
            gen.writeEndObject();
        }
        var hoverEvent = component.hoverEvent();
        if (hoverEvent != null && (hoverEvent.value() instanceof TextComponent || hoverEvent.value() instanceof String)) {
            gen.writeObjectFieldStart("hoverEvent");
            gen.writeStringField("action", hoverEvent.action().toString());
            var value = hoverEvent.value();
            if (value instanceof TextComponent text) {
                gen.writeObjectFieldStart("value");
                writeComponent(gen, text);
                gen.writeEndObject();
            } else if (value instanceof String string) {
                gen.writeStringField("value", string);
            }
            gen.writeEndObject();
        }
    }
}
