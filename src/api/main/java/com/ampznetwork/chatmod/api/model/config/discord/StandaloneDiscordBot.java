package com.ampznetwork.chatmod.api.model.config.discord;

import com.ampznetwork.chatmod.api.model.config.ChatModules;
import com.ampznetwork.chatmod.api.model.config.channel.Channel;
import com.ampznetwork.chatmod.api.model.config.format.Formats;
import lombok.Singular;
import lombok.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@Value
public class StandaloneDiscordBot implements DiscordBot {
    @NotNull  ChatModules   modules;
    @Singular List<Channel> channels;
    @Nullable Formats       format;
}
