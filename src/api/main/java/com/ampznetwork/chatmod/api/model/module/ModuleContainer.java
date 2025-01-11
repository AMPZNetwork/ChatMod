package com.ampznetwork.chatmod.api.model.module;

import com.ampznetwork.chatmod.api.model.config.ChatModules;
import com.ampznetwork.chatmod.api.model.config.channel.Channel;
import com.ampznetwork.chatmod.api.model.config.channel.IChannels;
import com.ampznetwork.libmod.api.LibMod;
import com.ampznetwork.libmod.api.SubMod;
import com.ampznetwork.libmod.api.interop.game.PlayerIdentifierAdapter;
import org.comroid.api.attr.Named;
import org.comroid.api.tree.Container;

import java.util.Optional;
import java.util.stream.Stream;

public interface ModuleContainer extends Container, Named {
    PlayerIdentifierAdapter getPlayerAdapter();

    ChatModules getChatModules();

    IChannels<Channel> getChannels();

    String getServerName();

    Stream<Module<?>> createModules();

    default void initModules() {
        createModules().forEach(this::addChildren);
    }

    default Optional<LibMod> wrapLib() {
        return this instanceof SubMod sub ? Optional.of(sub.getLib()) : Optional.empty();
    }
}
