package com.ampznetwork.chatmod.api.model;

import com.ampznetwork.chatmod.api.ChatMod;
import com.ampznetwork.chatmod.api.model.module.Module;
import lombok.Value;
import net.kyori.adventure.text.Component;

import static net.kyori.adventure.text.Component.*;
import static net.kyori.adventure.text.format.NamedTextColor.*;

@Value
public class TextResourceProvider {
    ChatMod mod;

    Component EmptyListEntry = text().append(text(" - ").color(WHITE)).append(text("(no entries)").color(GRAY)).build();

    public Component getModuleInfo(Module<?> module) {
        return text().append(text(module.getName()).color(BLUE))
                .append(text(": "))
                .append(module.isEnabled() ? text("ONLINE").color(GREEN) : text("OFFLINE").color(RED))
                .build();
    }
}
