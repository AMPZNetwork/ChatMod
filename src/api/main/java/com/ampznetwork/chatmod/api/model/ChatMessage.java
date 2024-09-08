package com.ampznetwork.chatmod.api.model;

import com.ampznetwork.libmod.api.entity.Player;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Value;
import net.kyori.adventure.text.TextComponent;

@Value
public class ChatMessage {
    Player sender;
    String inputString;
    String        plaintext;
    @JsonDeserialize() TextComponent text;
}
