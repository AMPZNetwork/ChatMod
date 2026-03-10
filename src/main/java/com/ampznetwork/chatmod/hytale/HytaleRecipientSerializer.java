package com.ampznetwork.chatmod.hytale;

import com.ampznetwork.chatmod.api.model.Player;
import com.ampznetwork.chatmod.model.RecipientSerializer;
import lombok.Value;
import org.comroid.api.data.RegExpUtil;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

@Value
public class HytaleRecipientSerializer implements RecipientSerializer {
    ChatModLiteHytale plugin;

    @Override
    public @Nullable Player deserializeRecipient(String recipient) {
        return recipient.matches(RegExpUtil.UUID4_PATTERN) ? plugin.getPlayer(UUID.fromString(recipient)) : null;
    }
}
