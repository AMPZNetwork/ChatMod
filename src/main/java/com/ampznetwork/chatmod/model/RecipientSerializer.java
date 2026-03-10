package com.ampznetwork.chatmod.model;

import com.ampznetwork.chatmod.api.model.Player;
import org.jetbrains.annotations.Nullable;

public interface RecipientSerializer {
    @Nullable Player deserializeRecipient(String recipient);

    default String serializeRecipient(Player player) {
        return player.getUuid().toString();
    }
}
