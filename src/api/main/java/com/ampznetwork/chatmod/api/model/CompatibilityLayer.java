package com.ampznetwork.chatmod.api.model;

import org.comroid.api.func.ext.Context;
import org.comroid.api.info.Log;
import org.comroid.api.tree.Reloadable;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

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
        getMod().relayInbound(convertToChatModPacket(packet));
    }

    void doSend(P packet);

    default void send(ChatMessagePacket packet) {
        if (!isEnabled()) return;

        // append self to route
        if (!packet.getRoute().contains(getMod().getSourceName()))
            packet.getRoute().add(getMod().getSourceName());

        doSend(convertToNativePacket(packet));
    }

    @Override
    default void reload() {
        try {
            Reloadable.super.reload();
            Log.at(Level.INFO, "Successfully reloaded " + getClass().getSimpleName());
        } catch (Throwable t) {
            var delay  = getMod().getAutoReconnectDelaySeconds();
            var string = "Failed to set up " + getClass().getSimpleName();
            if (delay == -1) {
                string += "; to retry, run command /chatmod:reload";
                Log.at(Level.WARNING, string, t);
            } else {
                string += "; trying again in " + delay + " seconds";
                Context.wrap(ScheduledExecutorService.class).wrap()
                        .orElseGet(() -> {
                            var executor = Executors.newSingleThreadScheduledExecutor();
                            Context.root().plus(executor);
                            return executor;
                        }).schedule(this::reload, delay, TimeUnit.SECONDS);
                Log.at(Level.WARNING, string, t);
            }
        }
    }
}
