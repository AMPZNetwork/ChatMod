package com.ampznetwork.chatmod.api.model;

import com.ampznetwork.chatmod.api.ChatModCompatibilityLayerAdapter;
import org.comroid.api.func.util.Debug;
import org.comroid.api.tree.Reloadable;

public interface CompatibilityLayer<P> extends Reloadable {
    ChatModCompatibilityLayerAdapter getMod();

    default boolean isEnabled() {
        return true;
    }

    default boolean isDefault() {
        return false;
    }

    default boolean skip(P packet) {
        return false;
    }

    ChatMessagePacket convertToChatModPacket(P packet);

    P convertToNativePacket(ChatMessagePacket packet);

    default void handle(P packet) {
        if (!isEnabled() || skip(packet)) return;
        var convert = convertToChatModPacket(packet);
        getMod().relayInbound(convert);
    }

    void doSend(P packet);

    default void send(ChatMessagePacket packet) {
        if (!isEnabled()) return;
        var convert = convertToNativePacket(packet);
        if (skip(convert)) return;
        doSend(convert);
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
