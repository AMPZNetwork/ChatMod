package com.ampznetwork.chatmod.core;

import com.ampznetwork.chatmod.api.model.config.ChatModules;
import com.ampznetwork.chatmod.api.model.module.Module;
import com.ampznetwork.chatmod.api.model.module.ModuleContainer;
import com.ampznetwork.chatmod.api.model.module.ModuleProvider;
import com.ampznetwork.chatmod.core.module.impl.LinkToAurionChatModule;
import com.ampznetwork.chatmod.core.module.impl.LinkToDiscordModule;
import com.ampznetwork.chatmod.core.module.impl.LinkToLogModule;
import com.ampznetwork.chatmod.core.module.impl.LinkToMinecraftModule;
import com.ampznetwork.chatmod.core.module.impl.LinkToNativeRabbitModule;
import org.comroid.api.info.Log;

import java.util.logging.Level;
import java.util.stream.Stream;

public interface ModuleContainerCore extends ModuleContainer {
    default Stream<Module<?>> createModules() {
        final var caps = getChatModules();
        return Stream.concat(Stream.of(new ModuleProvider<>(ChatModules::getLog, LinkToLogModule::new),
                        new ModuleProvider<>(ChatModules::getMinecraft, LinkToMinecraftModule::new),
                        new ModuleProvider<>(ChatModules::getRabbitmq, LinkToNativeRabbitModule::new),
                        new ModuleProvider<>(ChatModules::getAurionchat, LinkToAurionChatModule::new),
                        new ModuleProvider<>(ChatModules::getDiscord, LinkToDiscordModule::new)).map(provider -> provider.toFactory(caps)),
                Module.CUSTOM_TYPES.stream()).map(moduleFactory -> {
            Module<?> o = moduleFactory.create(this);
            Log.at(Level.INFO, "Module " + o + " created");
            return o;
        });
    }
}
