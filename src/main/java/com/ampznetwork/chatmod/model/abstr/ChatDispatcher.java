package com.ampznetwork.chatmod.model.abstr;

import com.ampznetwork.chatmod.api.model.Player;
import net.kyori.adventure.text.ComponentLike;
import org.jetbrains.annotations.NotNull;

public interface ChatDispatcher {
    void sendToPlayer(ComponentLike component, @NotNull Player player);
}
