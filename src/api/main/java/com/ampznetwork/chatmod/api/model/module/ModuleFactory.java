package com.ampznetwork.chatmod.api.model.module;

public interface ModuleFactory<M extends Module<?>> {
    M create(ModuleContainer mod);
}
