package com.ampznetwork.chatmod.core.compatibility.aurionchat;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.annotations.Expose;
import com.mineaurion.aurionchat.api.AurionPacket;
import com.mineaurion.aurionchat.api.AurionPlayer;
import lombok.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@Value
public class AurionPacketAdapter extends AurionPacket {
    static {
        AurionPacket.PARSE = AurionPacketAdapter::fromJson;
    }

    public static AurionPacketAdapter fromJson(final @NotNull String json) {
        return GSON.fromJson(json, GSON.fromJson(json, JsonObject.class).has("route") ? AurionPacketAdapter.class : AurionPacket.class);
    }

    public static Gson getGson() {
        return GSON;
    }

    @Expose @Nullable List<String> route;

    public AurionPacketAdapter(
            Type type, String source, @Nullable AurionPlayer player, @Nullable String channel, @Nullable String displayName, @NotNull String tellRawData,
            List<String> route
    ) {
        super(type, source, player, channel, displayName, tellRawData);

        this.route = route;
    }
}
