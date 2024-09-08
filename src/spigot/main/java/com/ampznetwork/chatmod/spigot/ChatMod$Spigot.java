package com.ampznetwork.chatmod.spigot;

import com.ampznetwork.chatmod.api.ChatMod;
import com.ampznetwork.chatmod.api.model.ChatMessage;
import com.ampznetwork.chatmod.api.model.ChatMessagePacket;
import com.ampznetwork.chatmod.spigot.adp.SpigotEventDispatch;
import com.ampznetwork.libmod.spigot.SubMod$Spigot;
import lombok.Getter;
import lombok.Value;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.comroid.api.net.Rabbit;

import java.util.Set;

@Getter
@Slf4j(topic = ChatMod.Strings.AddonName)
public class ChatMod$Spigot extends SubMod$Spigot implements ChatMod {
    @NonFinal Rabbit.Exchange.Route<ChatMessagePacket> rabbit;

    public ChatMod$Spigot() {
        super(Set.of(), Set.of());
    }

    @Override
    public String getServerName() {
        return getConfig().getString("server.name", "&eMC");
    }

    @Override
    public void onEnable() {
        super.onEnable();

        rabbit = Rabbit.of(getConfig().getString("rabbitmq.url"))
                .map(rabbit -> rabbit.bind("chat","",ChatMessagePacket.CONVERTER))
                .orElseThrow();
        getServer().getPluginManager().registerEvents(new SpigotEventDispatch(this), this);
    }

    @Override
    public Class<?> getModuleType() {
        return ChatMod.class;
    }
}
