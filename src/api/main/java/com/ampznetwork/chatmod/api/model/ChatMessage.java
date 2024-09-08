package com.ampznetwork.chatmod.api.model;

import com.ampznetwork.chatmod.api.util.TextComponentDeserializer;
import com.ampznetwork.chatmod.api.util.TextComponentSerializer;
import com.ampznetwork.libmod.api.entity.Player;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Value;
import net.kyori.adventure.text.TextComponent;

@Value
public class ChatMessage {
    Player sender;
    String inputString;
    String        plaintext;
    @JsonSerialize(using = TextComponentSerializer.class) @JsonDeserialize(using = TextComponentDeserializer.class) TextComponent text;
}
