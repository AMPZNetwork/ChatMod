package com.ampznetwork.chatmod.api.model.module;

import com.ampznetwork.chatmod.api.model.config.ChatModules;
import lombok.Value;
import org.jetbrains.annotations.Nullable;

import java.util.function.BiFunction;
import java.util.function.Function;

@Value
public class ModuleProvider<Mod extends Module<?>, Cfg extends ChatModules.ProviderConfig> {
    Function<ChatModules, Cfg>            config;
    BiFunction<ModuleContainer, Cfg, Mod> ctor;

    public ModuleFactory<Mod> toFactory(ChatModules caps) {
        return mod -> create(mod, caps);
    }

    public @Nullable Mod create(ModuleContainer mod, ChatModules caps) {
        var cfg = config.apply(caps);
        return cfg == null ? null : ctor.apply(mod, cfg);
    }
}
