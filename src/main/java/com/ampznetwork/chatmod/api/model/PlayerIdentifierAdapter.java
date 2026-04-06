package com.ampznetwork.chatmod.api.model;

import java.util.Optional;
import java.util.UUID;

public interface PlayerIdentifierAdapter {
    Optional<Player> getPlayer(UUID playerId);

    Optional<Player> getPlayer(String name);

    default String getDisplayName(UUID playerId) {
        return getPlayer(playerId).map(Player::getName).orElseGet(() -> Player.fetchUsername(playerId).join());
    }
}
