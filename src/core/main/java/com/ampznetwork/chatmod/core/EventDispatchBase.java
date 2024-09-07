package com.ampznetwork.chatmod.core;

import com.ampznetwork.chatmod.api.ChatMod;
import lombok.Value;
import lombok.experimental.NonFinal;

@Value
@NonFinal
public abstract class EventDispatchBase<Mod extends ChatMod> {
    Mod mod;
}
