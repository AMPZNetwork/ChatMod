package com.ampznetwork.chatmod.api.model;

import lombok.Value;
import org.comroid.api.data.seri.DataNode;

@Value
public class ChatMessagePacket extends DataNode.Object {
    String source;
    String channel;
    ChatMessage message;
}
