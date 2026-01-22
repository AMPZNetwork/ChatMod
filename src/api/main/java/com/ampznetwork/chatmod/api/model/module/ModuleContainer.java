package com.ampznetwork.chatmod.api.model.module;

import com.ampznetwork.chatmod.api.model.config.ChatModules;
import com.ampznetwork.chatmod.api.model.config.channel.Channel;
import com.ampznetwork.libmod.api.LibMod;
import com.ampznetwork.libmod.api.SubMod;
import com.ampznetwork.libmod.api.interop.game.PlayerIdentifierAdapter;
import com.ampznetwork.libmod.api.model.info.ServerInfoProvider;
import org.comroid.api.attr.Named;
import org.comroid.api.tree.Container;
import org.comroid.api.tree.Startable;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public interface ModuleContainer extends Container, Named {
    ServerInfoProvider getLib();

    PlayerIdentifierAdapter getPlayerAdapter();

    ChatModules getChatModules();

    List<Channel> getChannels();

    boolean isListenerCompatibilityMode();

    Module<?> getDefaultModule();

    Stream<Module<?>> createModules();

    default void initModules() {
        var ls = createModules().toList();
        ls.forEach(this::addChildren);
        ls.stream().filter(Module::isEnabled).forEach(Startable::start);
    }

    default Optional<LibMod> wrapLib() {
        return this instanceof SubMod sub ? Optional.of(sub.getLib()) : Optional.empty();
    }
}
