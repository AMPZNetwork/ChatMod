package com.ampznetwork.chatmod.core.module;

import com.ampznetwork.chatmod.api.model.config.ChatModules;
import com.ampznetwork.chatmod.api.model.module.Module;
import com.ampznetwork.chatmod.api.model.module.ModuleContainer;
import lombok.Value;
import lombok.experimental.NonFinal;
import org.comroid.api.func.ext.Context;
import org.comroid.api.info.Log;
import org.comroid.api.tree.Component;
import org.jetbrains.annotations.MustBeInvokedByOverriders;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
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
    public boolean isEnabled() {
        return config.isEnable();
    }

    @Override
    @MustBeInvokedByOverriders
    public void closeSelf() {
        var autoReconnectDelay = config.getAutoReconnectDelay();
        var autoReconnect      = autoReconnectDelay != -1;
        Log.at(Level.INFO, "Module " + this + " closed" + (autoReconnect ? "; auto-reloading in " + autoReconnectDelay + " seconds" : ""));
        if (autoReconnect)
            Context.root().getFromContext(ScheduledExecutorService.class, true)
                    .orElseGet(() -> {
                        var exec = Executors.newScheduledThreadPool(4);
                        Context.root().plus(exec);
                        return exec;
                    }).schedule(this::start, autoReconnectDelay, TimeUnit.SECONDS);
    }
}
