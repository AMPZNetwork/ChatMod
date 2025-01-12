package com.ampznetwork.chatmod.core.module.rabbit;

import com.ampznetwork.chatmod.api.model.config.ChatModules;
import com.ampznetwork.chatmod.api.model.module.ModuleContainer;
import com.ampznetwork.chatmod.core.module.AbstractModule;
import lombok.ToString;
import lombok.Value;
import lombok.experimental.NonFinal;
import org.comroid.api.ByteConverter;
import org.comroid.api.func.util.Streams;
import org.comroid.api.net.Rabbit;

import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

@Value
@NonFinal
@ToString(callSuper = true)
public abstract class AbstractRabbitMqModule<C extends ChatModules.RabbitMqProviderConfig, P> extends AbstractModule<C, P> {
    @NonFinal Rabbit                   rabbit = null;
    @NonFinal Rabbit.Exchange.Route<P> route  = null;

    public AbstractRabbitMqModule(ModuleContainer mod, C config) {
        super(mod, config);
    }

    @Override
    public boolean isAvailable() {
        return route != null && !route.isClosed() && route.touch().isOpen();
    }

    @Override
    public int hashCode() {
        return config.getRabbitUri().hashCode();
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof AbstractRabbitMqModule<?, ?> armqc && Objects.equals(route, armqc.route);
    }

    @Override
    public final void start() {
        if (!isClosed() && route != null && !route.isClosed() && route.touch().isOpen()) return;

        var uri = config.getRabbitUri();
        this.rabbit = "inherit".equalsIgnoreCase(uri)
                      ? mod.children(AbstractRabbitMqModule.class).flatMap(rabbit -> Stream.of(rabbit.config)
                .flatMap(Streams.cast(ChatModules.RabbitMqProviderConfig.class))
                .map(ChatModules.RabbitMqProviderConfig::getRabbitUri)
                .filter(Predicate.not("inherit"::equalsIgnoreCase))
                .map($ -> rabbit.getRabbit())).findAny().orElseThrow(() -> new NoSuchElementException("No RabbitMQ URI found to inherit from"))
                      : Rabbit.of(mod.getServerName() + ".ChatMod", uri).get();
        this.route  = Optional.ofNullable(rabbit)
                .map(rabbit -> rabbit.bind(mod.getServerName() + '.' + getName(), config.getExchange(), config.getExchangeType(), "", createByteConverter()))
                .orElse(null);
        addChildren(rabbit, route);

        if (route != null) addChild(route.filterData(this::acceptOutbound)
                .mapData(this::convertToChatModPacket)
                .filterData(Predicate.not(packet -> packet.getRoute().contains(config.getEndpointName())))
                .subscribeData(this::broadcastInbound));

        super.start();
    }

    @Override
    public void closeSelf() {
        clearChildren();
        super.closeSelf();
    }

    @Override
    public void relayInbound(P packet) {
        route.send(packet);
    }

    protected abstract ByteConverter<P> createByteConverter();
}
