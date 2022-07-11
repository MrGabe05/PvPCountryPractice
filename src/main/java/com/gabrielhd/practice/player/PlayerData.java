package com.gabrielhd.practice.player;

import com.gabrielhd.practice.Practice;
import com.gabrielhd.practice.database.Database;
import com.gabrielhd.practice.events.EventType;
import com.gabrielhd.practice.settings.ProfileOptions;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;

import java.util.*;

@Getter @Setter
public class PlayerData {

    private static final Map<UUID, PlayerData> players = new HashMap<>();

    private final UUID uuid;
    private UUID currentMatchID;
    private UUID duelSelecting;

    private int teamID;

    private long kills;
    private long deaths;
    private long rankeds;
    private long bestElo;

    private int hits;
    private int combo;
    private int eloRange;
    private int pingRange;
    private int missedPots;
    private int longestCombo;

    private PlayerState playerState;
    private ProfileOptions options;

    private final Map<String, Integer> rankedElo;
    private final Map<String, Integer> rankedWins;
    private final Map<String, Integer> rankedLosses;

    private final Map<String, Integer> unrankedWins;
    private final Map<String, Integer> unrankedLosses;

    private final Map<EventType, Integer> eventsWins;
    private final Map<EventType, Integer> eventsLosses;

    private final Map<String, Map<Integer, PlayerKit>> playerKits;

    public PlayerData(UUID uuid, boolean load) {
        this.uuid = uuid;

        this.options = new ProfileOptions();
        this.playerState = PlayerState.LOADING;

        this.teamID = -1;
        this.eloRange = 250;
        this.pingRange = 50;

        this.rankedElo = new HashMap<>();
        this.rankedWins = new HashMap<>();
        this.rankedLosses = new HashMap<>();

        this.unrankedWins = new HashMap<>();
        this.unrankedLosses = new HashMap<>();

        this.eventsWins = new HashMap<>();
        this.eventsLosses = new HashMap<>();

        this.playerKits = new HashMap<>();

        Database.getStorage().loadPlayer(this).thenAccept(value -> {
            if(load) {
                players.put(uuid, this);

                this.playerState = PlayerState.SPAWN;
            }
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

    public void setElo(String kitName, int elo) {
        this.rankedElo.put(kitName.toLowerCase(Locale.ROOT), elo);
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

    public void setRankedWins(String kitName, int wins) {
        this.rankedWins.put(kitName.toLowerCase(Locale.ROOT), wins);
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

    public void setRankedLosses(String kitName, int losses) {
        this.rankedLosses.put(kitName.toLowerCase(Locale.ROOT), losses);
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

    public void setUnrankedWins(String kitName, int wins) {
        this.unrankedWins.put(kitName.toLowerCase(Locale.ROOT), wins);
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

    public void setUnrankedLosses(String kitName, int losses) {
        this.unrankedLosses.put(kitName.toLowerCase(Locale.ROOT), losses);
    }

    public Map<Integer, PlayerKit> getPlayerKits(String kitName) {
        return this.playerKits.computeIfAbsent(kitName, k -> new HashMap());
    }

    public void addPlayerKit(int index, PlayerKit playerKit) {
        this.getPlayerKits(playerKit.getName()).put(index, playerKit);
    }

    public static void save(Player player, boolean remove) {
        Database.getStorage().uploadPlayer(PlayerData.of(player));

        if(remove) players.remove(player.getUniqueId());
    }

    public static PlayerData of(Player player) {
        return of(player.getUniqueId());
    }

    public static PlayerData of(UUID uuid) {
        return players.get(uuid);
    }

    public static OfflinePlayerData get(UUID uuid) {
        return new OfflinePlayerData(uuid);
    }

    public static void load(Player player) {
        new PlayerData(player.getUniqueId(), true);
    }

    public static Collection<PlayerData> getAllData() {
        return players.values();
    }
}
