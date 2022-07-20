package com.gabrielhd.practice.manager;

import com.gabrielhd.practice.Practice;
import com.gabrielhd.practice.arena.Arena;
import com.gabrielhd.practice.event.match.MatchEndEvent;
import com.gabrielhd.practice.event.match.MatchStartEvent;
import com.gabrielhd.practice.kit.Kit;
import com.gabrielhd.practice.lang.Lang;
import com.gabrielhd.practice.match.Match;
import com.gabrielhd.practice.match.MatchRequest;
import com.gabrielhd.practice.match.MatchState;
import com.gabrielhd.practice.match.MatchTeam;
import com.gabrielhd.practice.player.PlayerData;
import com.gabrielhd.practice.player.PlayerKit;
import com.gabrielhd.practice.player.PlayerState;
import com.gabrielhd.practice.queue.QueueType;
import com.gabrielhd.practice.utils.items.BuilderItem;
import com.gabrielhd.practice.utils.maps.TtlHashMap;
import com.gabrielhd.practice.utils.others.PlayerUtil;
import com.gabrielhd.practice.utils.text.TextPlaceholders;
import net.minecraft.server.v1_8_R3.EntityPlayer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class MatchManager {

    private final Map<UUID, Match> matches;
    private final Map<UUID, UUID> spectators;
    private final Map<UUID, UUID> rematchUUIDs;
    private final Map<UUID, UUID> rematchInventories;
    private final Map<UUID, Set<MatchRequest>> matchRequests;
    
    public MatchManager() {
        this.matches = new ConcurrentHashMap<>();
        this.spectators = new ConcurrentHashMap<>();
        this.rematchUUIDs = new TtlHashMap<>(TimeUnit.SECONDS, 30L);
        this.matchRequests = new TtlHashMap<>(TimeUnit.SECONDS, 30L);
        this.rematchInventories = new TtlHashMap<>(TimeUnit.SECONDS, 30L);
    }
    
    public int getFighters() {
        int i = 0;
        for (Match match : this.matches.values()) {
            for (MatchTeam matchTeam : match.getTeams()) {
                i += matchTeam.getAlivePlayers().size();
            }
        }
        return i;
    }
    
    public int getFighters(String ladder, QueueType type) {
        return (int)this.matches.entrySet().stream().filter(match -> match.getValue().getType() == type).filter(match -> match.getValue().getKit().getName().equals(ladder)).count();
    }
    
    public void createMatchRequest(Player requester, Player requested, Arena arena, String kitName, boolean party) {
        MatchRequest request = new MatchRequest(requester.getUniqueId(), requested.getUniqueId(), arena, kitName, party);
        this.matchRequests.computeIfAbsent(requested.getUniqueId(), k -> new HashSet()).add(request);
    }
    
    public MatchRequest getMatchRequest(UUID requester, UUID requested) {
        Set<MatchRequest> requests = this.matchRequests.get(requested);
        if (requests == null) return null;

        return requests.stream().filter(req -> req.getRequester().equals(requester)).findAny().orElse(null);
    }
    
    public MatchRequest getMatchRequest(UUID requester, UUID requested, String kitName) {
        Set<MatchRequest> requests = this.matchRequests.get(requested);
        if (requests == null) {
            return null;
        }
        return requests.stream().filter(req -> req.getRequester().equals(requester) && req.getKitName().equals(kitName)).findAny().orElse(null);
    }
    
    public Match getMatch(PlayerData playerData) {
        return this.matches.get(playerData.getCurrentMatchID());
    }
    
    public Match getMatch(UUID uuid) {
        PlayerData playerData = PlayerData.of(uuid);
        return this.getMatch(playerData);
    }
    
    public Match getMatchFromUUID(UUID uuid) {
        return this.matches.get(uuid);
    }
    
    public Match getSpectatingMatch(UUID uuid) {
        return this.matches.get(this.spectators.get(uuid));
    }
    
    public void removeMatchRequests(UUID uuid) {
        this.matchRequests.remove(uuid);
    }
    
    public void createMatch(Match match) {
        this.matches.put(match.getMatchId(), match);

        Bukkit.getServer().getPluginManager().callEvent(new MatchStartEvent(match));
    }
    
    public void removeFighter(Player player, PlayerData playerData, boolean spectateDeath) {
        Match match = this.matches.get(playerData.getCurrentMatchID());
        Player killer = player.getKiller();
        MatchTeam entityTeam = match.getTeams().get(playerData.getTeamID());
        MatchTeam winningTeam = match.isFFA() ? entityTeam : match.getTeams().get((entityTeam.getTeamID() == 0) ? 1 : 0);
        if (match.getMatchState() == MatchState.ENDING) {
            return;
        }

        if(killer != null) {
            match.broadcast(Lang.PLAYER_DEATH_FROM_OTHER, new TextPlaceholders().set("%player%", player.getName()).set("%killer%", killer.getName()));
        } else {
            match.broadcast(Lang.PLAYER_DEATH, new TextPlaceholders().set("%player%", player.getName()));
        }

        match.addSnapshot(player);

        entityTeam.killPlayer(player.getUniqueId());
        int remaining = entityTeam.getAlivePlayers().size();
        if (remaining != 0) {
            Set<Item> items = new HashSet<>();
            ItemStack[] contents;
            for (int length = (contents = player.getInventory().getContents()).length, i = 0; i < length; ++i) {
                ItemStack item = contents[i];
                if (item != null && item.getType() != Material.AIR) {
                    items.add(player.getWorld().dropItemNaturally(player.getLocation(), item));
                }
            }
            ItemStack[] armorContents;
            for (int length2 = (armorContents = player.getInventory().getArmorContents()).length, j = 0; j < length2; ++j) {
                ItemStack item = armorContents[j];
                if (item != null && item.getType() != Material.AIR) {
                    items.add(player.getWorld().dropItemNaturally(player.getLocation(), item));
                }
            }
            this.addDroppedItems(match, items);
        }
        if (spectateDeath) {
            this.addDeathSpectator(player, playerData, match);
        }
        if ((match.isFFA() && remaining == 1) || remaining == 0) {
            Bukkit.getPluginManager().callEvent(new MatchEndEvent(match, winningTeam, entityTeam));
        }
    }
    
    public void removeMatch(Match match) {
        this.matches.remove(match.getMatchId());
    }
    
    public void giveKits(Player player, Kit kit) {
        PlayerData playerData = PlayerData.of(player);
        Collection<PlayerKit> playerKits = playerData.getPlayerKits(kit.getName()).values();
        if (playerKits.isEmpty()) {
            kit.applyToPlayer(player);
        } else {
            player.getInventory().setItem(8, Practice.getInstance().getItemManager().getDefaultBook());
            int slot = -1;
            for (PlayerKit playerKit : playerKits) {
                player.getInventory().setItem(++slot, new BuilderItem(Material.ENCHANTED_BOOK).setTitle(String.valueOf(ChatColor.YELLOW.toString()) + ChatColor.BOLD + playerKit.getDisplayName()).build());
            }
            player.updateInventory();
        }
    }
    
    private void addDeathSpectator(Player player, PlayerData playerData, Match match) {
        this.spectators.put(player.getUniqueId(), match.getMatchId());
        playerData.setPlayerState(PlayerState.SPECTATING);
        PlayerUtil.clearPlayer(player);

        CraftPlayer playerCp = (CraftPlayer)player;
        EntityPlayer playerEp = playerCp.getHandle();
        playerEp.getDataWatcher().watch(6, 0.0f);
        //playerEp.setFakingDeath(true);

        match.addSpectator(player.getUniqueId());
        match.addRunnable(Bukkit.getScheduler().scheduleSyncDelayedTask(Practice.getInstance(), () -> {
            match.getTeams().forEach(team -> team.alivePlayers().forEach(member -> member.hidePlayer(player)));
            match.spectatorPlayers().forEach(member -> member.hidePlayer(player));
            player.getActivePotionEffects().stream().map(PotionEffect::getType).forEach(player::removePotionEffect);
        }, 20L));
        player.setWalkSpeed(0.2f);
        player.setAllowFlight(true);

        player.setWalkSpeed(0.0f);
        player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, 10000, -5));
        if (match.isParty() || match.isFFA()) {
            player.getInventory().setContents(Practice.getInstance().getItemManager().getPartySpecItemStack());
        }
        player.updateInventory();
    }
    
    public Map<UUID, UUID> getSpectators() {
        return this.spectators;
    }
    
    public void addSpectator(Player player, PlayerData playerData, Player target, Match targetMatch) {
        this.spectators.put(player.getUniqueId(), targetMatch.getMatchId());
        if (targetMatch.getMatchState() != MatchState.ENDING && !targetMatch.haveSpectated(player.getUniqueId())) {
            targetMatch.broadcast(Lang.SPECTATOR_JOIN, new TextPlaceholders().set("%player%", player.getName()));
        }

        targetMatch.addSpectator(player.getUniqueId());
        playerData.setPlayerState(PlayerState.SPECTATING);
        player.teleport(target);
        player.setAllowFlight(true);
        player.setFlying(true);
        player.getInventory().setContents(Practice.getInstance().getItemManager().getSpecItemStack());
        player.updateInventory();

        Bukkit.getOnlinePlayers().forEach(online -> {
            online.hidePlayer(player);
            player.hidePlayer(online);
        });
        targetMatch.getTeams().forEach(team -> team.alivePlayers().forEach(player::showPlayer));
    }
    
    public void addDroppedItem(Match match, Item item) {
        match.addEntityToRemove(item);
        match.addRunnable(Bukkit.getScheduler().runTaskLater(Practice.getInstance(), () -> {
            match.removeEntityToRemove(item);
            item.remove();
        }, 100L).getTaskId());
    }
    
    public void addDroppedItems(Match match, Set<Item> items) {
        for (Item item : items) {
            match.addEntityToRemove(item);
        }
        match.addRunnable(Bukkit.getScheduler().runTaskLater(Practice.getInstance(), () -> {
            for (Item item2 : items) {
                match.removeEntityToRemove(item2);
                item2.remove();
            }
        }, 100L).getTaskId());
    }
    
    public void removeSpectator(Player player) {
        Match match = this.matches.get(this.spectators.get(player.getUniqueId()));
        match.removeSpectator(player.getUniqueId());
        PlayerData playerData = PlayerData.of(player);
        if (match.getTeams().size() > playerData.getTeamID() && playerData.getTeamID() >= 0) {
            MatchTeam entityTeam = match.getTeams().get(playerData.getTeamID());
            if (entityTeam != null) {
                entityTeam.killPlayer(player.getUniqueId());
            }
        }
        if (match.getMatchState() != MatchState.ENDING && !match.haveSpectated(player.getUniqueId())) {
            match.broadcast(Lang.SPECTATOR_LEAVE, new TextPlaceholders().set("%player%", player.getName()));

            match.addHaveSpectated(player.getUniqueId());
        }
        this.spectators.remove(player.getUniqueId());

        Practice.getInstance().getPlayerManager().sendToSpawnAndReset(player);
    }
    
    public void pickPlayer(Match match) {
        Player playerA = Bukkit.getPlayer(match.getTeams().get(0).getAlivePlayers().get(0));
        PlayerData playerDataA = PlayerData.of(playerA);
        if (playerDataA.getPlayerState() != PlayerState.FIGHTING) {
            playerA.teleport(match.getArena().getA());
            PlayerUtil.clearPlayer(playerA);

            playerDataA.setPlayerState(PlayerState.FIGHTING);

            Practice.getInstance().getMatchManager().giveKits(playerA, match.getKit());
        }

        Player playerB = Bukkit.getPlayer(match.getTeams().get(1).getAlivePlayers().get(0));
        PlayerData playerDataB = PlayerData.of(playerB);
        if (playerDataB.getPlayerState() != PlayerState.FIGHTING) {
            playerB.teleport(match.getArena().getB());
            PlayerUtil.clearPlayer(playerB);

            playerDataB.setPlayerState(PlayerState.FIGHTING);

            Practice.getInstance().getMatchManager().giveKits(playerB, match.getKit());
        }

        for (MatchTeam team : match.getTeams()) {
            for (UUID uuid : team.getAlivePlayers()) {
                Player player = Bukkit.getPlayer(uuid);
                if (player != null && !playerA.equals(player) && !playerB.equals(player)) {
                    playerA.hidePlayer(player);
                    playerB.hidePlayer(player);
                }
            }
        }
        playerA.showPlayer(playerB);
        playerB.showPlayer(playerA);

        match.broadcast(Lang.STARTING_DUEL, new TextPlaceholders().set("%player-a%", playerA.getName()).set("%player-b%", playerB.getName()));
    }
    
    /*public void saveRematches(Match match) {
        if (match.isParty() || match.isFFA()) {
            return;
        }
        UUID playerOne = match.getTeams().get(0).getLeader();
        UUID playerTwo = match.getTeams().get(1).getLeader();
        PlayerData dataOne = this.plugin.getPlayerManager().getPlayerData(playerOne);
        PlayerData dataTwo = this.plugin.getPlayerManager().getPlayerData(playerTwo);
        if (dataOne != null) {
            this.rematchUUIDs.put(playerOne, playerTwo);
            InventorySnapshot snapshot = match.getSnapshot(playerTwo);
            if (snapshot != null) {
                this.rematchInventories.put(playerOne, snapshot.getSnapshotId());
            }
            if (dataOne.getRematchID() > -1) {
                this.plugin.getServer().getScheduler().cancelTask(dataOne.getRematchID());
            }
        }
        if (dataTwo != null) {
            this.rematchUUIDs.put(playerTwo, playerOne);
            InventorySnapshot snapshot = match.getSnapshot(playerOne);
            if (snapshot != null) {
                this.rematchInventories.put(playerTwo, snapshot.getSnapshotId());
            }
            if (dataTwo.getRematchID() > -1) {
                this.plugin.getServer().getScheduler().cancelTask(dataTwo.getRematchID());
            }
        }
    }*/
    
    public void removeRematch(UUID uuid) {
        this.rematchUUIDs.remove(uuid);
        this.rematchInventories.remove(uuid);
    }
    
    public List<UUID> getOpponents(Match match, Player player) {
        for (MatchTeam team : match.getTeams()) {
            if (team.getPlayers().contains(player.getUniqueId())) {
                continue;
            }
            return team.getPlayers();
        }
        return null;
    }
    
    public UUID getRematcher(UUID uuid) {
        return this.rematchUUIDs.get(uuid);
    }
    
    public UUID getRematcherInventory(UUID uuid) {
        return this.rematchInventories.get(uuid);
    }
    
    public boolean isRematching(UUID uuid) {
        return this.rematchUUIDs.containsKey(uuid);
    }
    
    public Map<UUID, Match> getMatches() {
        return this.matches;
    }
}
