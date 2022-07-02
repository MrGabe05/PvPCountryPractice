package com.gabrielhd.practice.player;

import com.gabrielhd.practice.Practice;
import com.gabrielhd.practice.database.Database;
import com.gabrielhd.practice.events.EventType;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;

import java.util.*;

@Getter @Setter
public class PlayerData {

    private static final Map<UUID, PlayerData> players = new HashMap<>();

    private final UUID uuid;

    private long kills;
    private long deaths;
    private long rankeds;
    private long bestElo;

    private PlayerState playerState;

    private final Map<String, Integer> rankedElo;
    private final Map<String, Integer> rankedWins;
    private final Map<String, Integer> rankedLosses;

    private final Map<String, Integer> unrankedWins;
    private final Map<String, Integer> unrankedLosses;

    private final Map<EventType, Integer> eventsWins;
    private final Map<EventType, Integer> eventsLosses;

    private final Map<String, List<PlayerKit>> kits;

    public PlayerData(UUID uuid) {
        this.uuid = uuid;

        this.playerState = PlayerState.LOADING;

        this.rankedElo = new HashMap<>();
        this.rankedWins = new HashMap<>();
        this.rankedLosses = new HashMap<>();

        this.unrankedWins = new HashMap<>();
        this.unrankedLosses = new HashMap<>();

        this.eventsWins = new HashMap<>();
        this.eventsLosses = new HashMap<>();

        this.kits = new HashMap<>();

        Database.getStorage().loadPlayer(this).thenAccept(value -> {
            players.put(uuid, this);

            this.playerState = PlayerState.LOBBY;
        });
    }

    public int getElo() {
        int totalElo = 0;

        for(String kitName : Practice.getInstance().getKitManager().getRankedKits()) {
            totalElo += getElo(kitName);
        }

        return totalElo;
    }

    public int getElo(String kitName) {
        return this.rankedElo.computeIfAbsent(kitName.toLowerCase(Locale.ROOT), k -> 0);
    }

    public int getRankedWins() {
        int wins = 0;

        for (String kitName : Practice.getInstance().getKitManager().getRankedKits()) {
            wins += getRankedWins(kitName);
        }

        return wins;
    }

    public int getRankedWins(String kitName) {
        return this.rankedWins.computeIfAbsent(kitName.toLowerCase(Locale.ROOT), k -> 0);
    }

    public int getRankedLosses() {
        int losses = 0;

        for (String kitName : Practice.getInstance().getKitManager().getRankedKits()) {
            losses += getRankedLosses(kitName);
        }

        return losses;
    }

    public int getRankedLosses(String kitName) {
        return this.rankedLosses.computeIfAbsent(kitName.toLowerCase(Locale.ROOT), k -> 0);
    }

    public int getUnrankedWins() {
        int wins = 0;

        for (String kitName : Practice.getInstance().getKitManager().getKitsNames()) {
            wins += getUnrankedWins(kitName);
        }

        return wins;
    }

    public int getUnrankedWins(String kitName) {
        return this.unrankedWins.computeIfAbsent(kitName.toLowerCase(Locale.ROOT), k -> 0);
    }

    public int getUnrankedLosses() {
        int losses = 0;

        for (String kitName : Practice.getInstance().getKitManager().getKitsNames()) {
            losses += getUnrankedLosses(kitName);
        }

        return losses;
    }

    public int getUnrankedLosses(String kitName) {
        return this.unrankedLosses.computeIfAbsent(kitName.toLowerCase(Locale.ROOT), k -> 0);
    }

    public static PlayerData of(Player player) {
        return players.get(player.getUniqueId());
    }

    public static void load(Player player) {
        new PlayerData(player.getUniqueId());
    }
}
