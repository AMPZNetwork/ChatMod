package com.ampznetwork.chatmod.core.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@RequiredArgsConstructor
public class PingPongEvent {
    private @NotNull UUID   eventId = UUID.randomUUID();
    private @NotNull Kind   kind;
    private @NotNull String arg;

    public enum Kind {/** question */ PING, /** answers */ PONG}
}
