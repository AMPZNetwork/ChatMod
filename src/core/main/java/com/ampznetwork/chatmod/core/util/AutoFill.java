package com.ampznetwork.chatmod.core.util;

import com.ampznetwork.chatmod.api.ChatMod;
import com.ampznetwork.chatmod.api.model.ChannelConfiguration;
import org.comroid.annotations.Instance;
import org.comroid.api.func.util.Command;
import org.comroid.api.func.util.Streams;

import java.util.stream.Stream;

public interface AutoFill {
    enum ChannelNames implements Command.AutoFillProvider {
        @Instance INSTANCE;

        @Override
        public Stream<String> autoFill(Command.Usage usage, String argName, String currentValue) {
            return usage.getContext().stream()
                    .flatMap(Streams.cast(ChatMod.class))
                    .flatMap(mod -> mod.getChannels().stream())
                    .map(ChannelConfiguration::getName);
        }
    }
}
