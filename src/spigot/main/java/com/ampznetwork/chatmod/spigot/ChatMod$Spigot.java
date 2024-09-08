package com.ampznetwork.chatmod.spigot;

import com.ampznetwork.chatmod.api.ChatMod;
import com.ampznetwork.chatmod.spigot.adp.SpigotEventDispatch;
import com.ampznetwork.libmod.spigot.SubMod$Spigot;

import java.util.Set;

public class ChatMod$Spigot extends SubMod$Spigot implements ChatMod {
    public ChatMod$Spigot() {
        super(Set.of(), Set.of());
    }

    @Override
    public String getServerName() {
        return getConfig().getString("server.name", "&eMC");
    }

    @Override
    public void onEnable() {
        super.onEnable();

        getServer().getPluginManager().registerEvents(new SpigotEventDispatch(this), this);
    }

    @Override
    public Class<?> getModuleType() {
        return ChatMod.class;
    }
}
