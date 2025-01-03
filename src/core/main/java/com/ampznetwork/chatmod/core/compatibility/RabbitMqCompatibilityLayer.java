package com.ampznetwork.chatmod.core.compatibility;

import com.ampznetwork.chatmod.api.model.ChatModCompatibilityLayerAdapter;
import com.ampznetwork.chatmod.api.model.CompatibilityLayer;
import com.ampznetwork.chatmod.core.compatibility.builtin.DefaultCompatibilityLayer;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import lombok.experimental.NonFinal;
import org.comroid.api.ByteConverter;
import org.comroid.api.net.Rabbit;
import org.comroid.api.tree.Component;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;

@Value
@NonFinal
@RequiredArgsConstructor
public abstract class RabbitMqCompatibilityLayer<P> extends Component.Base implements CompatibilityLayer<P> {
    protected ChatModCompatibilityLayerAdapter mod;
    @NonFinal Rabbit                   rabbit = null;
    @NonFinal Rabbit.Exchange.Route<P> route  = null;

    protected abstract String getUri();

    protected abstract String getExchange();

    protected @Nullable String getExchangeType() {
        return null;
    }

    @Override
    public int hashCode() {
        return getUri().hashCode();
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof RabbitMqCompatibilityLayer<?> mqcl && Objects.equals(route, mqcl.route);
    }

    @Override
    public final void doSend(P packet) {
        if (route != null) route.send(packet);
    }

    @Override
    public final void start() {
        if (!isEnabled() || (route != null && route.isActive())) return;

        this.rabbit = Optional.ofNullable(getUri())
                .flatMap(uri -> "inherit".equalsIgnoreCase(uri)
                                ? Optional.of(((DefaultCompatibilityLayer) mod.getDefaultCompatibilityLayer()).getRabbit())
                                : Rabbit.of(mod.getServerName(), uri).wrap())
                .orElse(null);
        this.route  = Optional.ofNullable(rabbit)
                .map(rabbit -> rabbit.bind(getClass().getSimpleName() + '@' + mod.getServerName(), getExchange(), getExchangeType(), "", createByteConverter()))
                .orElse(null);
        addChild(route);

        if (route != null) addChild(route.filterData(Predicate.not(packet -> {
            if (skip(packet)) return true;
            var convert = convertToChatModPacket(packet);
            return convert.getRoute().contains(mod.getSourceName()) || mod.skip(convert);
        })).subscribeData(this::handle));
    }

    @Override
    public void stop() {
        if (route != null) route.close();
    }

    @Override
    public abstract boolean isEnabled();

    protected abstract ByteConverter<P> createByteConverter();
}
