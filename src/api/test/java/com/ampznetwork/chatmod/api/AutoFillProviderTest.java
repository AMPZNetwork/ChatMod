package com.ampznetwork.chatmod.api;

import com.ampznetwork.chatmod.api.model.AutoFillProvider;
import com.ampznetwork.chatmod.api.model.config.channel.Channel;
import com.ampznetwork.chatmod.api.model.module.Module;
import com.ampznetwork.chatmod.api.model.module.ModuleContainer;
import com.ampznetwork.chatmod.api.model.protocol.ChatMessagePacket;
import org.comroid.util.TestUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Set;

import static org.easymock.EasyMock.*;

public class AutoFillProviderTest {
    static final                               Channel[]                 CHANNELS = new Channel[]{
            Channel.builder().name("channel-one").build(), Channel.builder().name("channel-two").build()
    };
    static final                               TestUtil.AutoFillProvider helper;
    static                                     ChatMod                   mod;
    @SuppressWarnings("resource") static final Module<?>[] MODULES = new Module[]{
            dummyModule("module-one"), dummyModule("module-two")
    };

    static {
        mod = mock(ChatMod.class);

        helper = new TestUtil.AutoFillProvider() {{getContext().add(mod);}};
    }

    @AfterEach
    public void validate() {
        verify(mod);
    }

    private void mockChannelNames() {
        reset(mod);

        expect(mod.getChannels()).andReturn(Arrays.asList(CHANNELS)).times(3);

        replay(mod);
    }

    @Test
    public void directChannelNames() {
        mockChannelNames();

        helper.testCaseDirect("on empty filter",
                AutoFillProvider.ChannelNames.INSTANCE,
                "",
                "channel-one",
                "channel-two");
        helper.testCaseDirect("on match many",
                AutoFillProvider.ChannelNames.INSTANCE,
                "channel",
                "channel-one",
                "channel-two");
        helper.testCaseDirect("on match one", AutoFillProvider.ChannelNames.INSTANCE, "channel-one", "channel-one");
    }

    @Test
    public void callChannelNames() {
        mockChannelNames();

        helper.testCaseCall("on empty filter", AutoFillProvider.ChannelNames.class, "", "channel-one", "channel-two");
        helper.testCaseCall("on match many",
                AutoFillProvider.ChannelNames.class,
                "channel",
                "channel-one",
                "channel-two");
        helper.testCaseCall("on match one", AutoFillProvider.ChannelNames.class, "channel-one", "channel-one");
    }

    private void mockModuleNames() {
        reset(mod);

        expect(mod.children(Module.class)).andAnswer(() -> Arrays.stream(MODULES)).times(3);

        replay(mod);
    }

    @Test
    public void directModuleNames() {
        mockModuleNames();

        helper.testCaseDirect("on empty filter", AutoFillProvider.ModuleNames.INSTANCE, "", "module-one", "module-two");
        helper.testCaseDirect("on match many",
                AutoFillProvider.ModuleNames.INSTANCE,
                "module",
                "module-one",
                "module-two");
        helper.testCaseDirect("on match one", AutoFillProvider.ModuleNames.INSTANCE, "module-one", "module-one");
    }

    @Test
    public void callModuleNames() {
        mockModuleNames();

        helper.testCaseCall("on empty filter", AutoFillProvider.ModuleNames.class, "", "module-one", "module-two");
        helper.testCaseCall("on match many", AutoFillProvider.ModuleNames.class, "module", "module-one", "module-two");
        helper.testCaseCall("on match one", AutoFillProvider.ModuleNames.class, "module-one", "module-one");
    }

    private static Module<?> dummyModule(String name) {
        return new Module<ChatMessagePacket>() {
            @Override
            public String getName() {
                return name;
            }

            @Override
            public ModuleContainer getMod() {
                return mod;
            }

            @Override
            public boolean isEnabled() {
                return true;
            }

            @Override
            public boolean isAvailable() {
                return true;
            }

            @Override
            public int priority() {
                return 0;
            }

            @Override
            public ChatMessagePacket upgradeToNative(ChatMessagePacket packet) {
                return packet;
            }

            @Override
            public Set<Object> getChildren() {
                return Set.of();
            }

            @Override
            public Object addChildren(@Nullable Object @NotNull ... children) {
                return this;
            }

            @Override
            public int removeChildren(@Nullable Object @NotNull ... children) {
                return 0;
            }

            @Override
            public void clearChildren() {
            }

            @Override
            public void closeSelf() {
            }

            @Override
            public void start() {
            }

            @Override
            public void close() {
            }
        };
    }
}
