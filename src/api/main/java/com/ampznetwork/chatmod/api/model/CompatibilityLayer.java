package com.ampznetwork.chatmod.api.model;

import com.ampznetwork.chatmod.api.ChatMod;
import org.comroid.api.func.util.Debug;
import org.comroid.api.tree.Reloadable;

public interface CompatibilityLayer<P> extends Reloadable {
    ChatMod getMod();

    boolean isEnabled();

    ChatMessagePacket convertToChatModPacket(P packet);

    P convertToNativePacket(ChatMessagePacket packet);

    boolean skip(P packet);

    default void handle(P packet) {
        if (!isEnabled() || skip(packet)) return;
        var convert = convertToChatModPacket(packet);
        getMod().relayInbound(convert);
    }

    void send(P packet);

    default void send(ChatMessagePacket packet) {
        if (!isEnabled()) return;
        var convert = convertToNativePacket(packet);
        if (skip(convert)) return;
        send(convert);
    }

    @Override
    default void reload() {
        try {
            Reloadable.super.reload();
        } catch (Throwable t) {
            // todo: proper LoggerAdapter of some sort
            var string = "Failed to set up " + getClass().getSimpleName() + "; " + t + " - to retry, run command /chatmod:reconnect";
            System.out.println(string);
            Debug.log(string, t);
        }
    }
}
