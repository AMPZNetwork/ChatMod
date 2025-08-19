package com.ampznetwork.chatmod.api.model;

import com.ampznetwork.chatmod.api.ChatMod;
import com.ampznetwork.chatmod.api.model.config.channel.Channel;
import com.ampznetwork.chatmod.api.model.module.Module;
import org.comroid.annotations.Instance;
import org.comroid.api.func.util.Command;
import org.comroid.api.func.util.Streams;

import java.util.stream.Stream;

public interface AutoFillProvider {
    enum ChannelNames implements Command.AutoFillProvider.Named<Channel> {
        @Instance INSTANCE;

        @Override
        public Stream<Channel> objects(Command.Usage usage, String currentValue) {
            return usage.getContext()
                    .stream()
                    .flatMap(Streams.cast(ChatMod.class))
                    .flatMap(mod -> mod.getChannels().stream());
        }
    }

    enum ModuleNames implements Command.AutoFillProvider.Named<Module<?>> {
        @Instance INSTANCE;

        @Override
        public Stream<Module<?>> objects(Command.Usage usage, String currentValue) {
            return usage.getContext()
                    .stream()
                    .flatMap(Streams.cast(ChatMod.class))
                    .flatMap(mod -> mod.children(Module.class));
        }

        @Override
        public String toString(Module<?> object) {
            return Named.super.toString(object).toLowerCase();
        }
    }
}
