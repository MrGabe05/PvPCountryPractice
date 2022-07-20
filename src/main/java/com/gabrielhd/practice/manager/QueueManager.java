package com.gabrielhd.practice.manager;

import com.gabrielhd.practice.Practice;
import com.gabrielhd.practice.arena.Arena;
import com.gabrielhd.practice.kit.Kit;
import com.gabrielhd.practice.match.Match;
import com.gabrielhd.practice.match.MatchTeam;
import com.gabrielhd.practice.player.PlayerData;
import com.gabrielhd.practice.player.PlayerState;
import com.gabrielhd.practice.queue.QueueEntry;
import com.gabrielhd.practice.queue.QueueType;
import com.gabrielhd.practice.utils.items.ActionItem;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class QueueManager {

    private final Map<UUID, QueueEntry> queued = new ConcurrentHashMap<>();
    private final Map<UUID, Long> playerQueueTime = new HashMap<>();
    private final Practice plugin = Practice.getInstance();
    
    private boolean rankedEnabled = true;
    
    public QueueManager() {
        this.plugin.getServer().getScheduler().runTaskTimer(this.plugin, () -> this.queued.forEach((key, value) -> this.findMatch(this.plugin.getServer().getPlayer(key), value.getKitName(), value.getElo(), value.getQueueType())), 20L, 20L);
    }
    
    public void addPlayerToQueue(Player player, PlayerData playerData, String kitName, QueueType type) {
        if (type != QueueType.UNRANKED && !this.rankedEnabled) {
            player.closeInventory();
            return;
        }
        playerData.setPlayerState(PlayerState.QUEUE);

        this.playerQueueTime.put(player.getUniqueId(), System.currentTimeMillis());
        int elo = (type == QueueType.RANKED) ? playerData.getElo(kitName) : 0;
        QueueEntry entry = new QueueEntry(type, kitName, elo, false);
        this.queued.put(playerData.getUuid(), entry);
        this.giveQueueItems(player);
        String unrankedMessage = "Has sido agregado a " + ChatColor.GOLD + "Unranked " + kitName;
        String rankedMessage = ChatColor.YELLOW + "Has sido agregado a " + ChatColor.GOLD + "Ranked " + kitName  + ChatColor.GREEN + "[" + elo + "]";
        player.sendMessage((type == QueueType.UNRANKED) ? unrankedMessage : rankedMessage);
    }
    
    private void giveQueueItems(Player player) {
        player.closeInventory();
        player.getInventory().setContents(this.plugin.getItemManager().getQueueItemStack());
        player.updateInventory();
    }
    
    public QueueEntry getQueueEntry(UUID uuid) {
        return this.queued.get(uuid);
    }
    
    public long getPlayerQueueTime(UUID uuid) {
        return this.playerQueueTime.get(uuid);
    }
    
    public int getQueueSize(String ladder, QueueType type) {
        return (int)this.queued.entrySet().stream().filter(entry -> entry.getValue().getQueueType() == type).filter(entry -> entry.getValue().getKitName().equals(ladder)).count();
    }
    
    private boolean findMatch(Player player, String kitName, int elo, QueueType type) {
        long queueTime = System.currentTimeMillis() - this.playerQueueTime.get(player.getUniqueId());
        PlayerData playerData = PlayerData.of(player);
        if (playerData == null) {
            this.plugin.getLogger().log(Level.WARNING, "{0}''s player data is null", String.valueOf(player.getName()));
            return false;
        }
        int eloRange = playerData.getEloRange();
        int pingRange = -1;
        int seconds = Math.round(queueTime / 1000L);
        if (seconds > 5 && type != QueueType.UNRANKED) {
            if (pingRange != -1) {
                pingRange += (seconds - 5) * 25;
            }
            if (eloRange != -1) {
                eloRange += seconds * 50;
                if (eloRange >= 3000) {
                    eloRange = 3000;
                }
            }
        }
        if (eloRange == -1) {
            eloRange = Integer.MAX_VALUE;
        }
        if (pingRange == -1) {
            pingRange = Integer.MAX_VALUE;
        }
        int ping = 0;
        for (UUID opponent : this.queued.keySet()) {
            QueueEntry queueEntry;
            if (opponent == player.getUniqueId() || !(queueEntry = this.queued.get(opponent)).getKitName().equals(kitName) || queueEntry.getQueueType() != type || queueEntry.isParty()) continue;
            Player opponentPlayer = this.plugin.getServer().getPlayer(opponent);
            PlayerData opponentData = PlayerData.of(opponent);
            if (opponentData.getPlayerState() == PlayerState.FIGHTING || playerData.getPlayerState() == PlayerState.FIGHTING) continue;
            int eloDiff = Math.abs(queueEntry.getElo() - elo);
            if (type.isRanked()) {
                if (eloDiff > eloRange) continue;
                long opponentQueueTime = System.currentTimeMillis() - this.playerQueueTime.get(opponentPlayer.getUniqueId());
                int opponentEloRange = -1;
                int opponentPingRange = -1;
                int opponentSeconds = Math.round(opponentQueueTime / 1000L);
                if (opponentSeconds > 5) {
                    if (opponentPingRange != -1) {
                        opponentPingRange += (opponentSeconds - 5) * 25;
                    }
                    if (opponentEloRange != -1 && (opponentEloRange += opponentSeconds * 50) >= 3000) {
                        opponentEloRange = 3000;
                    }
                }
                if (opponentEloRange == -1) {
                    opponentEloRange = Integer.MAX_VALUE;
                }
                if (opponentPingRange == -1) {
                    opponentPingRange = Integer.MAX_VALUE;
                }
                if (eloDiff > opponentEloRange) continue;
                int pingDiff = Math.abs(0);
                if (type == QueueType.RANKED && (pingDiff > opponentPingRange || pingDiff > pingRange)) continue;
            }

            if(playerData.getPlayerState() == PlayerState.STARTING || opponentData.getPlayerState() == PlayerState.STARTING) return true;

            Kit kit = this.plugin.getKitManager().getKit(kitName);
            Arena arena = this.plugin.getArenaManager().getRandomArena(kit);

            playerData.setPlayerState(PlayerState.STARTING);
            opponentData.setPlayerState(PlayerState.STARTING);
            String playerFoundMatchMessage;
            String matchedFoundMatchMessage;
            if (type.isRanked()) {
                playerFoundMatchMessage = ChatColor.GREEN + player.getName() + ChatColor.YELLOW + " con " + ChatColor.GREEN + this.queued.get(player.getUniqueId()).getElo() + " elo";
                matchedFoundMatchMessage = ChatColor.GREEN + opponentPlayer.getName() + ChatColor.YELLOW + " con " + ChatColor.GREEN + this.queued.get(opponentPlayer.getUniqueId()).getElo() + " elo";
            }
            else {
                playerFoundMatchMessage = ChatColor.GREEN + player.getName() + ".";
                matchedFoundMatchMessage = ChatColor.GREEN + opponentPlayer.getName() + ".";
            }
            player.sendMessage(ChatColor.YELLOW + "Comenzando duelo contra " + matchedFoundMatchMessage);
            opponentPlayer.sendMessage(ChatColor.YELLOW + "Comenzando duelo contra " + playerFoundMatchMessage);
            MatchTeam teamA = new MatchTeam(player.getUniqueId(), Collections.singletonList(player.getUniqueId()), 0);
            MatchTeam teamB = new MatchTeam(opponentPlayer.getUniqueId(), Collections.singletonList(opponentPlayer.getUniqueId()), 1);
            Match match = new Match(arena, kit, type, teamA, teamB);

            this.plugin.getMatchManager().createMatch(match);
            this.queued.remove(player.getUniqueId());
            this.queued.remove(opponentPlayer.getUniqueId());
            this.playerQueueTime.remove(player.getUniqueId());
            return true;
        }
        return false;
    }
    
    public void removePlayerFromQueue(Player player) {
        this.queued.remove(player.getUniqueId());
        this.plugin.getPlayerManager().sendToSpawnAndReset(player);
    }
    
    public boolean isRankedEnabled() {
        return this.rankedEnabled;
    }
    
    public void setRankedEnabled(boolean rankedEnabled) {
        this.rankedEnabled = rankedEnabled;
    }
}
