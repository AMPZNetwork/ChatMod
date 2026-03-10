package com.ampznetwork.chatmod.model.abstr;

import com.ampznetwork.chatmod.api.model.Player;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public interface PlayerAdapter {
    @Nullable Player getPlayer(UUID playerId);
}
