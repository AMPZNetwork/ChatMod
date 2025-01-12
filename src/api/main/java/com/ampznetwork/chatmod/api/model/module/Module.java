package com.ampznetwork.chatmod.api.model.module;

import com.ampznetwork.chatmod.api.model.protocol.ChatMessagePacket;
import com.ampznetwork.chatmod.api.model.protocol.io.BidirectionalPacketStream;
import org.comroid.api.Polyfill;
import org.comroid.api.attr.Named;
import org.comroid.api.func.util.Streams;
import org.comroid.api.info.Log;
import org.comroid.api.tree.Container;
import org.comroid.api.tree.Reloadable;
import org.jetbrains.annotations.MustBeInvokedByOverriders;

import java.util.HashSet;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;
import java.util.function.Predicate;
import java.util.logging.Level;

/**
 * native rabbitmq, aurionchat, discord
 */
public interface Module<P> extends Container, BidirectionalPacketStream<P>, Reloadable, Named {
    Set<ModuleFactory<?>> CUSTOM_TYPES = new HashSet<>();

    ModuleContainer getMod();

    /**
     * @return whether the provider is enabled by config
     */
    boolean isEnabled();

    /**
     * @return whether the provider is ready to send and receive data
     */
    boolean isAvailable();

    @Override
    @MustBeInvokedByOverriders
    default boolean acceptInbound(P packet) {
        var convert = convertToChatModPacket(packet);
        return convert != null && !convert.getRoute().contains(getMod().getServerName());
    }

    @Override
    @MustBeInvokedByOverriders
    default boolean acceptOutbound(P packet) {
        var convert = convertToChatModPacket(packet);
        return convert != null && !convert.getRoute().contains(getMod().getServerName());
    }

    /**
     * broadcast inbound packet to all providers except this
     */
    default void broadcastInbound(ChatMessagePacket packet) {
        broadcast(packet, Module::acceptInbound, Module::relayInbound);
    }

    /**
     * broadcast outbound packet to all providers except this
     */
    default void broadcastOutbound(ChatMessagePacket packet) {
        broadcast(packet, Module::acceptOutbound, Module::relayOutbound);
    }

    /**
     * broadcast packet to all providers except this using relay
     */
    private <$> void broadcast(final ChatMessagePacket packet, final BiPredicate<Module<$>, $> accept, final BiConsumer<Module<$>, $> relay) {
        getMod().children(Module.class)
                .filter(Predicate.not(this::equals))
                .filter(module -> getMod().children(Module.class)
                        .filter(any -> any.getName().toLowerCase().contains("aurion"))
                        .anyMatch(Module::isEnabled))
                .filter(Module::isEnabled)
                .flatMap(Streams.filter(Module::isAvailable, this::reportCapabilityUnavailable))
                .map(Polyfill::<Module<$>>uncheckedCast)
                .forEach(cap -> {
                    if (accept.test(cap, cap.convertToNativePacket(packet))) relay.accept(cap, cap.convertToNativePacket(packet));
                });
    }

    private void reportCapabilityUnavailable(Module<?> module) {
        Log.at(Level.WARNING, "CapabilityProvider " + module + " is unavailable");
    }

    ChatMessagePacket convertToChatModPacket(P packet);

    P convertToNativePacket(ChatMessagePacket packet);
}
