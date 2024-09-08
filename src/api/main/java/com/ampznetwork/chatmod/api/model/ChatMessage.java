package com.ampznetwork.chatmod.api.model;

import com.ampznetwork.libmod.api.entity.Player;
import lombok.Value;
import net.kyori.adventure.text.TextComponent;

@Value
public class ChatMessage {
    Player sender;
    String inputString;
    String        plaintext;
    TextComponent text;
}
