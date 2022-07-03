package com.gabrielhd.practice.lang;

import com.gabrielhd.practice.Practice;
import com.gabrielhd.practice.config.YamlConfig;
import com.gabrielhd.practice.utils.text.Color;
import com.gabrielhd.practice.utils.text.TextPlaceholders;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.*;

public class Lang {

    private final Map<String, String> langs = new HashMap<>();

    public static Lang
            JOINED_EVENT,
            SELECTING_EVENT_PLAYERS,
            STARTING_EVENT_FIGHT,
            WINNING_EVENT,
            PLAYER_DEATH,
            PLAYER_DEATH_FROM_OTHER,
            STARTING_DUEL,
            EVENT_PLAYER_ELIMINATED,
            EVENT_BROADCAST,
            HOVER_BROADCAST_MESSAGE,
            INSUFFICIENT_PLAYERS,
            SPECTATOR_JOIN,
            SPECTATOR_LEAVE,
            PARTY_CREATED,
            PARTY_DISBAND,
            PARTY_LEAVE,
            PARTY_JOIN
                    = new Lang();

    public void addLang(String lang, String text) {
        this.langs.put(lang.toLowerCase(Locale.ROOT), Color.text(text));
    }

    public String get(Player player, TextPlaceholders textPlaceholders) {
        String lang = player.spigot().getLocale().split("_")[0];

        return textPlaceholders.parse(langs.getOrDefault(lang.toLowerCase(Locale.ROOT), langs.get("en")));
    }

    public static void loadLangs() {
        File langFolder = new File(Practice.getInstance().getDataFolder(), "/lang/");
        if(!langFolder.exists()) langFolder.mkdir();

        Arrays.stream(langFolder.listFiles()).filter(File::isFile).filter(file -> file.getPath().endsWith(".yml")).forEach(file -> {
            YamlConfig langConfig = new YamlConfig(Practice.getInstance(), file);

            String lang = file.getName().split("_")[1];

            JOINED_EVENT.addLang(lang.toLowerCase(Locale.ROOT), langConfig.getString("Joined_Event"));
        });
    }
}
