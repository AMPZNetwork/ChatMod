package com.ampznetwork.chatmod.core.serialization;

import com.ampznetwork.chatmod.api.ChatMod;
import com.ampznetwork.chatmod.api.model.ChatMessage;
import com.ampznetwork.chatmod.api.model.ChatMessagePacket;
import com.ampznetwork.libmod.api.entity.Player;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import lombok.Value;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;

import java.io.IOException;
import java.util.UUID;

@Value
public class ChatMessagePacketTypeAdapter extends TypeAdapter<ChatMessagePacket> {
    private static final GsonComponentSerializer componentSerializer = GsonComponentSerializer.gson();
    ChatMod mod;

    @Override
    public void write(JsonWriter out, ChatMessagePacket packet) throws IOException {
        out.beginObject();

        // Serialize basic fields
        out.name("source").value(packet.getSource());
        out.name("channel").value(packet.getChannel());

        // Serialize the ChatMessage object
        out.name("message");
        writeChatMessage(out, packet.getMessage());

        out.endObject();
    }

    private void writeChatMessage(JsonWriter out, ChatMessage message) throws IOException {
        out.beginObject();

        // Serialize the sender (assuming it's a Player object with toString or a relevant serializer)
        out.name("sender").value(message.getSender().getId().toString());

        // Serialize the messageString
        out.name("messageString").value(message.getMessageString());

        // Serialize the plaintext
        out.name("plaintext").value(message.getPlaintext());

        // Serialize the TextComponent using GsonComponentSerializer
        //todo: should not be json in json, but gson is weird af
        //out.name("text").jsonValue(componentSerializer.serialize(message.getText()));
        out.name("text").value(componentSerializer.serialize(message.getText()));

        out.endObject();
    }

    @Override
    public ChatMessagePacket read(JsonReader in) throws IOException {
        String source = null;
        String channel = null;
        ChatMessage message = null;

        in.beginObject();
        while (in.hasNext()) {
            String name = in.nextName();
            switch (name) {
                case "source":
                    source = in.nextString();
                    break;
                case "channel":
                    channel = in.nextString();
                    break;
                case "message":
                    message = readChatMessage(in);
                    break;
                default:
                    in.skipValue();
                    break;
            }
        }
        in.endObject();

        return new ChatMessagePacket(source, channel, message);
    }

    private ChatMessage readChatMessage(JsonReader in) throws IOException {
        Player sender        = null;
        String messageString = null;
        String        plaintext = null;
        TextComponent text      = null;

        in.beginObject();
        while (in.hasNext()) {
            String name = in.nextName();
            switch (name) {
                case "sender":
                    // Deserialize sender (this is a placeholder, adjust based on your Player deserialization)
                    var id = UUID.fromString(in.nextString());
                    sender = mod.getLib().getPlayerAdapter().getPlayer(id).orElseThrow();
                    break;
                case "messageString":
                    messageString = in.nextString();
                    break;
                case "plaintext":
                    plaintext = in.nextString();
                    break;
                case "text":
                    // Deserialize TextComponent using GsonComponentSerializer
                    // todo: how to properly decompose jsonValue here?
                    text = (TextComponent) componentSerializer.deserialize(in.nextString());
                    break;
                default:
                    in.skipValue();
                    break;
            }
        }
        in.endObject();

        // Create and return ChatMessage object
        return new ChatMessage(sender, messageString, plaintext, text);
    }
}