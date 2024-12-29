package com.ampznetwork.chatmod.discord.config;

import lombok.Value;

import java.util.Set;

@Value
public class Config {
    String                     discordToken;
    String                     rabbitMqUri;
    Set<DiscordChannelMapping> channels;
}
