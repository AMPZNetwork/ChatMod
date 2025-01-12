package com.ampznetwork.chatmod.api.model.module;

import org.jetbrains.annotations.Nullable;

public interface ModuleFactory<M extends Module<?>> {
    @Nullable M create(ModuleContainer mod);
}
