package com.ampznetwork.chatmod.api.model.module;

import com.ampznetwork.chatmod.api.model.protocol.ChatMessagePacket;
import com.ampznetwork.chatmod.api.model.protocol.io.BidirectionalPacketStream;
import org.comroid.api.attr.Named;
import org.comroid.api.func.util.Streams;
import org.comroid.api.info.Log;
import org.comroid.api.tree.Container;
import org.comroid.api.tree.Reloadable;
import org.jetbrains.annotations.MustBeInvokedByOverriders;

import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;
import java.util.function.Predicate;
import java.util.logging.Level;

/**
 * native rabbitmq, aurionchat, discord
 */
public interface Module<P extends ChatMessagePacket> extends Container, BidirectionalPacketStream<P>, Reloadable, Named {
    Comparator<Module<?>> COMPARATOR = Comparator.comparingInt(Module::priority);
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

    default String getEndpointName() {
        return getMod().getLib().getServerName() + '.' + getClass().getSimpleName();
    }

    int priority();

    private void touch(ChatMessagePacket packet) {
        if (!packet.getRoute().contains(getEndpointName())) packet.getRoute().add(getEndpointName());
    }

    /**
     * broadcast inbound packet to all providers except this
     */
    default void broadcastInbound(ChatMessagePacket packet) {
        broadcast(packet, Module::acceptInbound, Module::relayInbound);
    }

    @Override
    @MustBeInvokedByOverriders
    default boolean acceptInbound(P packet) {
        return packet != null && !packet.getRoute().contains(getEndpointName());
    }

    @Override
    @MustBeInvokedByOverriders
    default void relayInbound(P packet) {
        touch(packet);
    }

    @Override
    @MustBeInvokedByOverriders
    default boolean acceptOutbound(P packet) {
        return packet != null && !packet.getRoute().contains(getEndpointName());
    }

    @Override
    default void relayOutbound(P packet) {
        touch(packet);
        broadcastInbound(packet);
    }

    /**
     * broadcast packet to all providers except this using relay
     */
    private <$ extends ChatMessagePacket> void broadcast(
            final ChatMessagePacket packet, final BiPredicate<Module<$>, $> accept, final BiConsumer<Module<$>, $> relay) {
        getMod().<Module<$>>children(Module.class)
                .filter(Predicate.not(this::equals))
                /*.filter(module -> getMod().children(Module.class)
                        .filter(any -> any.getClass().getSimpleName().toLowerCase().contains("aurion"))
                        .anyMatch(Module::isEnabled))*/
                .filter(Module::isEnabled)
                .sorted(COMPARATOR)
                .flatMap(Streams.filter(Module::isAvailable, this::reportCapabilityUnavailable))
                .forEach(cap -> {
                    var convert = cap.upgradeToNative(packet);
                    if (accept.test(cap, convert)) relay.accept(cap, convert);
                });
    }

    private void reportCapabilityUnavailable(Module<?> module) {
        Log.at(Level.WARNING, "CapabilityProvider " + module + " is unavailable");
    }

    P upgradeToNative(ChatMessagePacket packet);
}
