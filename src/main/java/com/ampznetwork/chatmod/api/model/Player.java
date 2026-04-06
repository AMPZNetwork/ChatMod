package com.ampznetwork.chatmod.api.model;

import lombok.Value;
import org.comroid.api.attr.Named;
import org.comroid.api.attr.UUIDContainer;
import org.comroid.api.net.REST;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Value
public class Player implements UUIDContainer, Named {
    public static CompletableFuture<UUID> fetchId(String name) {
        var future = REST.get("https://api.mojang.com/users/profiles/minecraft/" + name)
                .thenApply(REST.Response::validate2xxOK)
                .thenApply(rsp -> rsp.getBody().get("id").asString())
                .thenApply(Player::fillDashes)
                .thenApply(UUID::fromString);
        return future;
    }

    public static CompletableFuture<String> fetchUsername(UUID id) {
        var future = REST.request(REST.Method.GET, "https://sessionserver.mojang.com/session/minecraft/profile/" + id)
                .execute()
                .thenApply(REST.Response::validate2xxOK)
                .thenApply(rsp -> rsp.getBody().get("name").asString());
        return future;
    }

    UUID   uuid;
    String name;

    private static String fillDashes(String uuid) {
        if (uuid == null) return null;
        if (uuid.length() > 36) uuid = uuid.replaceAll("-", "");
        return uuid.length() == 36
               ? uuid
               : uuid.substring(0, 8) + '-' + uuid.substring(8, 12) + '-' + uuid.substring(12, 16) + '-' + uuid.substring(16, 20) + '-' + uuid.substring(20);
    }
}
