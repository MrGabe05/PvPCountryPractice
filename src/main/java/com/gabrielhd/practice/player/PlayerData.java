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

    private Map<String, Integer> rankedEloMap;
    private Map<String, Integer> rankedWinsMap;
    private Map<String, Integer> rankedLossesMap;

    private Map<String, Integer> unrankedWinsMap;
    private Map<String, Integer> unrankedLossesMap;

    private Map<String, Integer> eventsWinsMap;
    private Map<String, Integer> eventsLossesMap;

    private Map<String, Map<Integer, PlayerKit>> playerKits;

    public PlayerData(UUID uuid, boolean load) {
        this.uuid = uuid;

        this.options = new ProfileOptions();
        this.playerState = PlayerState.LOADING;

        this.teamID = -1;
        this.eloRange = 250;
        this.pingRange = 50;

        this.rankedEloMap = new HashMap<>();
        this.rankedWinsMap = new HashMap<>();
        this.rankedLossesMap = new HashMap<>();

        this.unrankedWinsMap = new HashMap<>();
        this.unrankedLossesMap = new HashMap<>();

        this.eventsWinsMap = new HashMap<>();
        this.eventsLossesMap = new HashMap<>();

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
        return this.rankedEloMap.computeIfAbsent(kitName.toLowerCase(Locale.ROOT), k -> 0);
    }

    public void setElo(String kitName, int elo) {
        this.rankedEloMap.put(kitName.toLowerCase(Locale.ROOT), elo);
    }

    public int getRankedWins() {
        int wins = 0;

        for (String kitName : Practice.getInstance().getKitManager().getRankedKits()) {
            wins += getRankedWins(kitName);
        }

        return wins;
    }

    public int getRankedWins(String kitName) {
        return this.rankedWinsMap.computeIfAbsent(kitName.toLowerCase(Locale.ROOT), k -> 0);
    }

    public void setRankedWins(String kitName, int wins) {
        this.rankedWinsMap.put(kitName.toLowerCase(Locale.ROOT), wins);
    }

    public int getRankedLosses() {
        int losses = 0;

        for (String kitName : Practice.getInstance().getKitManager().getRankedKits()) {
            losses += getRankedLosses(kitName);
        }

        return losses;
    }

    public int getRankedLosses(String kitName) {
        return this.rankedLossesMap.computeIfAbsent(kitName.toLowerCase(Locale.ROOT), k -> 0);
    }

    public void setRankedLosses(String kitName, int losses) {
        this.rankedLossesMap.put(kitName.toLowerCase(Locale.ROOT), losses);
    }

    public int getUnrankedWins() {
        int wins = 0;

        for (String kitName : Practice.getInstance().getKitManager().getKitsNames()) {
            wins += getUnrankedWins(kitName);
        }

        return wins;
    }

    public int getUnrankedWins(String kitName) {
        return this.unrankedWinsMap.computeIfAbsent(kitName.toLowerCase(Locale.ROOT), k -> 0);
    }

    public void setUnrankedWins(String kitName, int wins) {
        this.unrankedWinsMap.put(kitName.toLowerCase(Locale.ROOT), wins);
    }

    public int getUnrankedLosses() {
        int losses = 0;

        for (String kitName : Practice.getInstance().getKitManager().getKitsNames()) {
            losses += getUnrankedLosses(kitName);
        }

        return losses;
    }

    public int getUnrankedLosses(String kitName) {
        return this.unrankedLossesMap.computeIfAbsent(kitName.toLowerCase(Locale.ROOT), k -> 0);
    }

    public void setUnrankedLosses(String kitName, int losses) {
        this.unrankedLossesMap.put(kitName.toLowerCase(Locale.ROOT), losses);
    }

    public int getWinsEvent(EventType event) {
        return this.eventsWinsMap.computeIfAbsent(event.name().toLowerCase(Locale.ROOT), e -> 0);
    }

    public void setWinsEvent(EventType event, int wins) {
        this.eventsWinsMap.put(event.name().toLowerCase(Locale.ROOT), wins);
    }

    public int getLossesEvent(EventType event) {
        return this.eventsLossesMap.computeIfAbsent(event.name().toLowerCase(Locale.ROOT), e -> 0);
    }

    public void setLossesEvent(EventType event, int losses) {
        this.eventsLossesMap.put(event.name().toLowerCase(Locale.ROOT), losses);
    }

    public Map<Integer, PlayerKit> getPlayerKits(String kitName) {
        return this.playerKits.computeIfAbsent(kitName.toLowerCase(Locale.ROOT), k -> new HashMap());
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
