package com.ampznetwork.chatmod.api.model.config.channel;

import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.TextColor;
import org.comroid.api.text.minecraft.ComponentSupplier;

import static net.kyori.adventure.text.Component.*;
import static net.kyori.adventure.text.event.HoverEvent.*;
import static net.kyori.adventure.text.format.NamedTextColor.*;

public enum ChannelState implements ComponentSupplier {
    Idle {
        @Override
        public TextColor toColor() {
            return GRAY;
        }

        @Override
        public ComponentLike toComponent() {
            return text("idle", toColor());
        }

        @Override
        public HoverEvent<? extends ComponentLike> toHoverEvent() {
            return showText(toComponent());
        }
    }, Joined {
        @Override
        public TextColor toColor() {
            return GREEN;
        }

        @Override
        public ComponentLike toComponent() {
            return text("joined", toColor());
        }

        @Override
        public HoverEvent<? extends ComponentLike> toHoverEvent() {
            return showText(text("You are in this channel", toColor()));
        }
    }, Spying {
        @Override
        public TextColor toColor() {
            return YELLOW;
        }

        @Override
        public ComponentLike toComponent() {
            return text("spying...", toColor());
        }

        @Override
        public HoverEvent<? extends ComponentLike> toHoverEvent() {
            return showText(text("You are spying on this channel", toColor()));
        }
    };

    public abstract TextColor toColor();

    public abstract HoverEvent<? extends ComponentLike> toHoverEvent();
}
