package com.ampznetwork.chatmod.lite.model;

import com.ampznetwork.libmod.api.entity.Player;
import org.jetbrains.annotations.Nullable;

public interface RecipientSerializer {
    @Nullable Player deserializeRecipient(String recipient);

    default String serializeRecipient(Player player) {
        return player.getId().toString();
    }
}
