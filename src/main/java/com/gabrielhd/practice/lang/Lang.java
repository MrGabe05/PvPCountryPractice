package com.gabrielhd.practice.lang;

import com.gabrielhd.practice.Practice;
import com.gabrielhd.practice.config.YamlConfig;
import com.gabrielhd.practice.utils.text.Color;
import com.gabrielhd.practice.utils.text.TextPlaceholders;
import org.bukkit.entity.Player;

import java.io.File;
import java.lang.reflect.Field;
import java.util.*;

public class Lang {

    private final Map<String, Object> langs = new HashMap<>();

    public static Lang
            ARENA_ALREADY_EXISTS,
            ARENA_NOT_EXISTS,
            ARENA_WHITELISTED,
            ARENA_BLACKLISTED,
            ARENA_NOT_WHITELISTED,
            ARENA_NOT_BLACKLISTED,
            JOINED_EVENT,
            SELECTING_EVENT_PLAYERS,
            EVENT_STARTING,
            EVENT_STARTING_HOVER,
            EVENT_ALREADY_STARTED,
            EVENT_STARTED_FIGHT,
            EVENT_STARTING_FIGHT,
            EVENT_STARTING_FIGHT_BROADCAST,
            EVENT_ENDING_FIGHT_BROADCAST,
            EVENT_PLAYER_ELIMINATED,
            EVENT_BROADCAST,
            EVENT_WINNING,
            EVENT_COOLDOWN,
            EVENT_NOT_EXISTS,
            EVENT_AVAIABLES,
            PLAYER_DEATH,
            PLAYER_DEATH_FROM_OTHER,
            PLAYER_NOT_FOUND,
            PLAYER_STATS,
            PLAYER_STATS_RESET,
            PLAYER_DUEL_REQUEST_RECEIVED,
            PLAYER_DUEL_REQUEST_SENT,
            PLAYER_DUEL_HOVER_MESSAGE,
            PLAYER_ALREADY_IN_PARTY,
            PLAYER_IGNORING_REQUESTS,
            PLAYER_IGNORING_SPECS,
            PLAYER_NOT_IN_GAME,
            PLAYER_NOT_IN_EVENT,
            PLAYER_BUSY,
            PLAYER_BUSY_OTHER,
            ALREADY_SPECTATING,
            NOW_SPECTATING,
            GIVED_RANKEDS,
            HOVER_BROADCAST_MESSAGE,
            INSUFFICIENT_PLAYERS,
            SUMO_LIMIT,
            SUMO_SPAWN_NULL,
            SPAWN_ADDED,
            SPAWN_REMOVED,
            SPAWN_MAX,
            SPAWN_MIN,
            STARTING_DUEL,
            SPECTATOR_JOIN,
            SPECTATOR_LEAVE,
            PARTY_INFO,
            PARTY_CREATED,
            PARTY_DISBAND,
            PARTY_LEAVE,
            PARTY_JOIN,
            PARTY_ALREADY,
            PARTY_ALREADY_IN,
            PARTY_BROADCAST,
            PARTY_HOVER_BROADCAST,
            PARTY_PUBLIC_REQUIRED,
            PARTY_LIMIT,
            PARTY_NOT_EXISTS,
            PARTY_NOT_LEADER,
            PARTY_STATUS,
            PARTY_IS_OPEN,
            PARTY_IS_CLOSED,
            PARTY_INVITE_BROADCAST,
            PARTY_INVITE_MESSAGE,
            PARTY_INVITE_HOVER_MESSAGE,
            PARTY_INVITE_ALREADY_SENT,
            PARTY_CANT_INVITE_YOURSELF,
            PARTY_DUEL_REQUEST_RECEIVED,
            PARTY_DUEL_REQUEST_SENT,
            PARTY_HOVER_DUEL_MESSAGE,
            MATCH_START,
            MORE_PLAYERS,
            NOT_IN_PARTY,
            NOT_IN_YOUR_PARTY,
            NOT_DUEL_REQUESTS,
            NOT_PARTY_REQUESTS,
            NOT_FIGHT_YOURSELF,
            NOT_AVAILABLE_ARENAS,
            FINISH_MESSAGE,
            WINNER_MESSAGE,
            WINNER_HOVER_MESSAGE,
            LOSSER_MESSAGE,
            LOSSER_HOVER_MESSAGE,
            ELO_CHANGES,
            KIT_CREATED,
            KIT_DELETED,
            KIT_ENABLED,
            KIT_DISABLED,
            KIT_NOT_EXISTS,
            KIT_ALREADY_EXISTS,
            KIT_RANKED_ENABLED,
            KIT_RANKED_DISABLED,
            KIT_SET_ICON,
            KIT_GET_INVENTORY,
            KIT_SET_INVENTORY,
            NO_PERMISSIONS,
            COMMAND_NOT_AVAILABLE,
            COMMAND_NOT_AVAILABLE_FOR_PLAYER;

    public void addLang(String lang, String... text) {
        for(String s : text) {
            langs.put(lang.toLowerCase(Locale.ROOT), Color.text(s));
        }
    }

    public void send(List<Player> players, TextPlaceholders textPlaceholders) {
        players.forEach(player -> send(player, textPlaceholders));
    }

    public void send(Player player) {
        send(player, new TextPlaceholders());
    }

    public void send(Player player, TextPlaceholders textPlaceholders) {
        String lang = player.spigot().getLocale().split("_")[0];

        Object obj = langs.getOrDefault(lang.toLowerCase(Locale.ROOT), langs.values().stream().findFirst().orElse(this.getClass().getName() + " value not set"));

        if(obj instanceof List) {
            for(String s : (List<String>) obj) {
                s = textPlaceholders.parse(s);

                player.sendMessage(Color.text(s));
            }
            return;
        }

        player.sendMessage(Color.text(textPlaceholders.parse((String) obj)));
    }

    public String get(Player player) {
        return get(player, new TextPlaceholders());
    }

    public String get(Player player, TextPlaceholders textPlaceholders) {
        String lang = player.spigot().getLocale().split("_")[0];

        return (String) langs.getOrDefault(lang.toLowerCase(Locale.ROOT), langs.values().stream().findFirst().orElse(this.getClass().getName() + " value not set"));
    }

    public static void loadLangs() {
        File langFolder = new File(Practice.getInstance().getDataFolder(), "/lang/");
        if(!langFolder.exists()) langFolder.mkdir();

        Arrays.stream(langFolder.listFiles()).filter(File::isFile).filter(file -> file.getPath().endsWith(".yml")).forEach(file -> {
            YamlConfig langConfig = new YamlConfig(Practice.getInstance(), file);

            String lang = file.getName().split("_")[1].replace(".yml", "");

            for(Field field : Lang.class.getFields()) {
                field.setAccessible(true);

                if(field.getType() == Lang.class) {
                    if(!langConfig.isSet(field.getName().toLowerCase(Locale.ROOT))) langConfig.set(field.getName().toLowerCase(Locale.ROOT), field.getName().toLowerCase(Locale.ROOT) + " value not set");

                    try {
                        if(field.get(null) == null) {
                            field.set(field, new Lang());
                        }

                        Lang obj = (Lang) field.get(null);

                        if(langConfig.isList(field.getName().toLowerCase(Locale.ROOT))) {
                            obj.addLang(lang, langConfig.getStringList(field.getName().toLowerCase(Locale.ROOT)).toArray(new String[0]));
                        } else {
                            obj.addLang(lang, langConfig.getString(field.getName().toLowerCase(Locale.ROOT)));
                        }
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                }
            }
            langConfig.save();

            Practice.getInstance().getLogger().info("Lang " + lang + " loaded.");
        });
    }
}
