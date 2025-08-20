package com.ampznetwork.chatmod.api.model;

import com.ampznetwork.chatmod.api.ChatMod;
import com.ampznetwork.chatmod.api.model.config.channel.Channel;
import com.ampznetwork.chatmod.api.model.module.Module;
import org.comroid.annotations.Instance;
import org.comroid.api.func.util.Streams;
import org.comroid.commands.autofill.impl.NamedAutoFillAdapter;
import org.comroid.commands.impl.CommandUsage;

import java.util.stream.Stream;

public interface AutoFillProvider {
    enum ChannelNames implements NamedAutoFillAdapter<Channel> {
        @Instance INSTANCE;

        @Override
        public Stream<Channel> objects(CommandUsage usage, String currentValue) {
            return usage.getContext()
                    .stream()
                    .flatMap(Streams.cast(ChatMod.class))
                    .flatMap(mod -> mod.getChannels().stream());
        }
    }

    enum ModuleNames implements NamedAutoFillAdapter<Module<?>> {
        @Instance INSTANCE;

        @Override
        public Stream<Module<?>> objects(CommandUsage usage, String currentValue) {
            return usage.getContext()
                    .stream()
                    .flatMap(Streams.cast(ChatMod.class))
                    .flatMap(mod -> mod.children(Module.class));
        }

        @Override
        public String toString(Module<?> object) {
            return NamedAutoFillAdapter.super.toString(object).toLowerCase();
        }
    }
}
