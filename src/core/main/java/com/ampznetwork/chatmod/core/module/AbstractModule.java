package com.ampznetwork.chatmod.core.module;

import com.ampznetwork.chatmod.api.model.config.ChatModules;
import com.ampznetwork.chatmod.api.model.module.Module;
import com.ampznetwork.chatmod.api.model.module.ModuleContainer;
import lombok.Value;
import lombok.experimental.NonFinal;
import org.comroid.api.info.Log;
import org.comroid.api.tree.Component;
import org.jetbrains.annotations.MustBeInvokedByOverriders;

import java.util.logging.Level;

@Value
@NonFinal
public abstract class AbstractModule<C extends ChatModules.ProviderConfig, P> extends Component.Base implements Module<P> {
    protected ModuleContainer mod;
    protected C               config;

    public AbstractModule(ModuleContainer mod, C config, Object... children) {
        super(children);
        this.mod    = mod;
        this.config = config;
    }

    @Override
    public String toString() {
        return config.getName();
    }

    @Override
    @MustBeInvokedByOverriders
    public void start() {
        Log.at(Level.INFO, "Module " + this + " started");
    }

    @Override
    @MustBeInvokedByOverriders
    public void closeSelf() {
        Log.at(Level.INFO, "Module " + this + " closed");
    }

    @Override
    public boolean isEnabled() {
        return config.isEnable();
    }
}
