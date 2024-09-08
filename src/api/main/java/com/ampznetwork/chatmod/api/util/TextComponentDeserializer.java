package com.ampznetwork.chatmod.api.util;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.TextDecoration;

import java.io.IOException;

import static com.fasterxml.jackson.core.JsonToken.*;
import static net.kyori.adventure.text.Component.*;
import static net.kyori.adventure.text.event.ClickEvent.*;
import static net.kyori.adventure.text.event.HoverEvent.*;
import static net.kyori.adventure.text.format.TextColor.*;

public class TextComponentDeserializer extends JsonDeserializer<TextComponent> {
    @Override
    public TextComponent deserialize(JsonParser p, DeserializationContext ctx) throws IOException {
        var text = text();
        if (p.currentToken() != START_ARRAY)
            throw new JsonParseException(p, "Text Component should be stored in array");
        while (p.nextToken() != END_ARRAY) {
            text.append(readComponent(p));
        }
        return text.build();
    }

    private TextComponent readComponent(JsonParser p) throws IOException {
        var text = text();
        while (p.nextToken() != END_OBJECT)
            switch (p.currentName()) {
                case "text":
                    text.content(p.nextTextValue());
                    break;
                case "color":
                    text.color(fromHexString(p.nextTextValue()));
                    break;
                case "clickEvent":
                    if (p.nextToken() != START_OBJECT)
                        throw new JsonParseException(p, "Expected clickEvent Object");
                    String action0 = null, value0 = null;
                    while (p.nextToken() != END_OBJECT)
                        switch (p.currentName()) {
                            case "action":
                                action0 = p.nextTextValue();
                                break;
                            case "value":
                                value0 = p.nextTextValue();
                                break;
                            default:
                                throw new IllegalStateException("Unexpected value: " + p.currentName());
                        }
                    assert action0 != null && value0 != null;
                    text.clickEvent(switch (ClickEvent.Action.valueOf(action0.toUpperCase())) {
                        case OPEN_URL -> openUrl(value0);
                        case OPEN_FILE -> openFile(value0);
                        case RUN_COMMAND -> runCommand(value0);
                        case SUGGEST_COMMAND -> suggestCommand(value0);
                        case CHANGE_PAGE -> changePage(value0);
                        case COPY_TO_CLIPBOARD -> copyToClipboard(value0);
                    });
                    break;
                case "hoverEvent":
                    if (p.nextToken() != START_OBJECT)
                        throw new JsonParseException(p, "Expected hoverEvent Object");
                    String action1 = null, valueS = null;
                    TextComponent valueC = null;
                    while (p.nextToken() != END_OBJECT)
                        switch (p.currentName()) {
                            case "action":
                                action1 = p.nextTextValue();
                                break;
                            case "value":
                                if (p.nextToken() == VALUE_STRING)
                                    valueS = p.currentValue().toString();
                                else if (p.currentToken() == START_OBJECT)
                                    valueC = readComponent(p);
                                else throw new JsonParseException(p, "Expected hoverEvent.value data");
                                break;
                            default:
                                throw new IllegalStateException("Unexpected value: " + p.currentName());
                        }
                    assert action1 != null;
                    //noinspection SwitchStatementWithTooFewBranches
                    var hoverEvent = switch (action1) {
                        case "show_text" -> {
                            assert valueC != null;
                            yield showText(valueC);
                        }
                        default -> throw new IllegalStateException("Unexpected value: " + action1);
                    };
                    text.hoverEvent(hoverEvent);
                    break;
                default:
                    for (var decor : TextDecoration.values()) {
                        if (decor.toString().equals(p.currentName()))
                            text.decorate(decor);
                    }
            }
        return text.build();
    }
}
