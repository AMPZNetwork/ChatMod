package com.ampznetwork.chatmod.api.model;

import lombok.Builder;
import lombok.Value;
import org.comroid.api.attr.Named;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Value
@Builder
public class ChannelConfiguration implements Named {
    String name;
    @lombok.Builder.Default @Nullable String    alias      = null;
    @lombok.Builder.Default @Nullable String    permission = null;
    @lombok.Builder.Default           boolean   publish    = true;
    @lombok.Builder.Default           Set<UUID> playerIDs  = new HashSet<>();
    @lombok.Builder.Default           Set<UUID> spyIDs     = new HashSet<>();
}
