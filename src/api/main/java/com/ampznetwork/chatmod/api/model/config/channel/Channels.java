package com.ampznetwork.chatmod.api.model.config.channel;

import lombok.Builder;
import lombok.Singular;
import lombok.Value;

import java.util.List;

@Value
@Builder
public class Channels implements IChannels<Channel> {
    @Singular List<Channel> channels;
}
