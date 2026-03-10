package com.ampznetwork.chatmod.api.model;

import lombok.Value;
import org.comroid.api.attr.Named;
import org.comroid.api.attr.UUIDContainer;

import java.util.UUID;

@Value
public class Player implements UUIDContainer, Named {
    UUID   uuid;
    String name;
}
