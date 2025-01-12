package com.ampznetwork.chatmod.discord;

import com.ampznetwork.chatmod.api.model.config.ChatModules;
import com.ampznetwork.chatmod.api.model.config.channel.Channel;
import com.ampznetwork.chatmod.api.model.config.discord.StandaloneDiscordBot;
import com.ampznetwork.chatmod.api.model.module.Module;
import com.ampznetwork.chatmod.core.ModuleContainerCore;
import com.ampznetwork.chatmod.core.module.impl.LinkToDiscordModule;
import com.ampznetwork.libmod.api.interop.game.PlayerIdentifierAdapter;
import com.ampznetwork.libmod.core.adapter.HeadlessPlayerAdapter;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.StreamReadFeature;
import com.fasterxml.jackson.core.json.JsonReadFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.EqualsAndHashCode;
import lombok.SneakyThrows;
import lombok.Value;
import lombok.experimental.NonFinal;
import org.comroid.api.func.util.Command;
import org.comroid.api.info.Log;
import org.comroid.api.io.FileHandle;
import org.comroid.api.tree.Container;
import org.comroid.exception.ReloadException;

import java.io.InputStreamReader;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;

@Value
@EqualsAndHashCode(of = "config")
public class DiscordBot extends Container.Base implements ModuleContainerCore {
    public static final String     WEBHOOK_NAME = "Minecraft Chat Link";
    public static final String     SOURCE       = "discord";
    public static       FileHandle DIR;
    public static       FileHandle CONFIG;
    public static       DiscordBot INSTANCE;

    @SneakyThrows
    public static void main(String[] args) {
        DIR    = new FileHandle("/srv/chatmod", true);
        CONFIG = DIR.createSubFile("config.json5");

        if (!CONFIG.exists()) try (
                var res = DiscordBot.class.getClassLoader().getResourceAsStream("config.json5");
                var isr = new InputStreamReader(Objects.requireNonNull(res, "Could not load default config resource")); var out = CONFIG.openWriter()
        ) {
            isr.transferTo(out);
        } catch (Throwable t) {
            Log.at(Level.WARNING, "Could not save default config file: " + t);
        }

        while (true) try {
            INSTANCE = new DiscordBot(CONFIG);
        } catch (ReloadException r) {
            Log.at(Level.INFO, "Restarting Discord Bot");
        } catch (Throwable t) {
            Log.at(Level.SEVERE, "Unable to initialize DiscordBot module", t);
            System.exit(1);
        }
    }

    PlayerIdentifierAdapter playerAdapter = HeadlessPlayerAdapter.INSTANCE;
    FileHandle              configFile;
    @NonFinal StandaloneDiscordBot config;

    private DiscordBot(FileHandle configFile) {
        this.configFile = configFile;

        reload();
    }

    @Override
    public ChatModules getChatModules() {
        return config.getModules();
    }

    @Override
    public List<Channel> getChannels() {
        return config.getChannels();
    }

    @Override
    public String getServerName() {
        return SOURCE.toUpperCase();
    }

    @Override
    public Module<?> getDefaultModule() {
        return child(LinkToDiscordModule.class).assertion();
    }

    @Command
    @Override
    @SneakyThrows
    public void reload() {
        stop();

        config = new ObjectMapper(JsonFactory.builder()
                .enable(StreamReadFeature.INCLUDE_SOURCE_IN_LOCATION)
                .enable(JsonReadFeature.ALLOW_TRAILING_COMMA)
                .build()
                .enable(JsonParser.Feature.ALLOW_COMMENTS)
                .enable(JsonParser.Feature.ALLOW_YAML_COMMENTS)
                .enable(JsonParser.Feature.ALLOW_SINGLE_QUOTES)
                .enable(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES)
                .enable(JsonParser.Feature.STRICT_DUPLICATE_DETECTION)).readValue(CONFIG, StandaloneDiscordBot.class);

        start();
    }

    @Override
    public void start() {
        super.start();
        initModules();
    }

    @Override
    public void closeSelf() {
        clearChildren();
    }
}
