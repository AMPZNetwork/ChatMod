package com.ampznetwork.chatmod.lite.model.abstr;

import com.ampznetwork.libmod.api.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public interface PlayerAdapter {
    @Nullable Player getPlayer(UUID playerId);
}
