package com.ampznetwork.chatmod.core.compatibility;

import com.ampznetwork.chatmod.api.ChatMod;
import com.ampznetwork.chatmod.api.model.CompatibilityLayer;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import lombok.experimental.NonFinal;
import org.comroid.api.ByteConverter;
import org.comroid.api.func.util.Streams;
import org.comroid.api.net.Rabbit;
import org.comroid.api.tree.Component;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

@Value
@NonFinal
@RequiredArgsConstructor
public abstract class RabbitMqCompatibilityLayer<P> extends Component.Base implements CompatibilityLayer<P> {
    protected ChatMod                  mod;
    @NonFinal Rabbit                   rabbit = null;
    @NonFinal Rabbit.Exchange.Route<P> route  = null;

    protected abstract String getUri();

    protected abstract String getExchange();

    protected @Nullable String getExchangeType() {
        return null;
    }

    protected abstract ByteConverter<P> createByteConverter();

    @Override
    public final void start() {
        if (!isEnabled()) return;

        this.rabbit = Optional.ofNullable(getUri())
                .flatMap(uri -> "inherit".equalsIgnoreCase(uri)
                                ? mod.getCompatibilityLayers().stream()
                                        .flatMap(Streams.cast(DefaultCompatibilityLayer.class))
                                        .map(DefaultCompatibilityLayer::getRabbit)
                                        .findAny()
                                : Rabbit.of(uri).wrap())
                .orElse(null);
        this.route  = Optional.ofNullable(rabbit)
                .map(rabbit -> rabbit.bind(getExchange(), getExchangeType(), "", createByteConverter()))
                .orElse(null);
        addChild(route);

        if (route != null)
            addChild(route.listen().subscribeData(this::handle));
    }

    @Override
    public final void send(P packet) {
        if (route != null) route.send(packet);
    }
}
