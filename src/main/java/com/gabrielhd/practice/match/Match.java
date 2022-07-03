package com.gabrielhd.practice.match;

import com.gabrielhd.practice.arena.Arena;
import com.gabrielhd.practice.arena.StandArena;
import com.gabrielhd.practice.kit.Kit;
import com.gabrielhd.practice.lang.Lang;
import com.gabrielhd.practice.queue.QueueType;
import com.gabrielhd.practice.utils.others.BlockTracker;
import com.gabrielhd.practice.utils.text.TextPlaceholders;
import io.netty.util.internal.ConcurrentSet;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.util.*;
import java.util.stream.Stream;

@Getter @Setter
public class Match {

    private final Kit kit;
    private final Arena arena;
    private final UUID matchId;
    private final QueueType type;

    private StandArena standArena;
    private MatchState matchState;
    private int winningTeamId;
    private int countdown;

    private final Set<UUID> spectators;
    private final Set<UUID> haveSpectated;
    private final List<MatchTeam> teams;
    private final Set<Integer> runnables;
    private final Set<Entity> entitiesToRemove;
    private final Set<Location> placedBlockLocations;
    private final Set<BlockState> originalBlockChanges;

    private final Map<UUID, Inventory> snapshots;

    private final BlockTracker blockTracker;
    
    public Match(Arena arena, Kit kit, QueueType type, MatchTeam... teams) {
        this.arena = arena;
        this.kit = kit;
        this.type = type;
        this.countdown = 6;
        this.matchId = UUID.randomUUID();
        this.matchState = MatchState.STARTING;

        this.snapshots = new HashMap<>();
        this.entitiesToRemove = new HashSet<>();
        this.spectators = new ConcurrentSet<>();
        this.originalBlockChanges = new ConcurrentSet<>();
        this.placedBlockLocations = new ConcurrentSet<>();
        this.runnables = new HashSet<>();
        this.haveSpectated = new HashSet<>();
        this.teams = Arrays.asList(teams);

        this.blockTracker = new BlockTracker();
    }
    
    public void addSpectator(UUID uuid) {
        this.spectators.add(uuid);
    }
    
    public void removeSpectator(UUID uuid) {
        this.spectators.remove(uuid);
    }
    
    public void addHaveSpectated(UUID uuid) {
        this.haveSpectated.add(uuid);
    }
    
    public boolean haveSpectated(UUID uuid) {
        return this.haveSpectated.contains(uuid);
    }
    
    public void addSnapshot(Player player) {
        this.snapshots.put(player.getUniqueId(), Bukkit.createInventory(player, 6 * 9));
    }
    
    public boolean hasSnapshot(UUID uuid) {
        return this.snapshots.containsKey(uuid);
    }
    
    public Inventory getSnapshot(UUID uuid) {
        return this.snapshots.get(uuid);
    }
    
    public void addEntityToRemove(Entity entity) {
        this.entitiesToRemove.add(entity);
    }
    
    public void removeEntityToRemove(Entity entity) {
        this.entitiesToRemove.remove(entity);
    }
    
    public void clearEntitiesToRemove() {
        this.entitiesToRemove.clear();
    }
    
    public void addRunnable(int id) {
        this.runnables.add(id);
    }
    
    public void broadcastWithSound(String message, Sound sound) {
        this.teams.forEach(team -> team.alivePlayers().forEach(player -> {
            player.sendMessage(message);
            player.playSound(player.getLocation(), sound, 10.0f, 1.0f);
        }));
        this.spectatorPlayers().forEach(spectator -> {
            spectator.sendMessage(message);
            spectator.playSound(spectator.getLocation(), sound, 10.0f, 1.0f);
        });
    }
    
    public void broadcast(Lang message, TextPlaceholders textPlaceholders) {
        this.teams.forEach(team -> team.alivePlayers().forEach(player -> player.sendMessage(message.get(player, textPlaceholders))));
        this.spectatorPlayers().forEach(spectator -> spectator.sendMessage(message.get(spectator, textPlaceholders)));
    }
    
    public Stream<Player> spectatorPlayers() {
        return this.spectators.stream().map(Bukkit::getPlayer).filter(Objects::nonNull);
    }
    
    public int decrementCountdown() {
        return --this.countdown;
    }
    
    public boolean isParty() {
        return this.isFFA() || (this.teams.get(0).getPlayers().size() != 1 && this.teams.get(1).getPlayers().size() != 1);
    }
    
    public boolean isPartyMatch() {
        return this.isFFA() || this.teams.get(0).getPlayers().size() >= 2 || this.teams.get(1).getPlayers().size() >= 2;
    }
    
    public boolean isFFA() {
        return this.teams.size() == 1;
    }
    
    public void addPlacedBlockLocation(Location location) {
        this.placedBlockLocations.add(location);
    }

    public void removePlacedBlockLocation(Location location) {
	this.placedBlockLocations.remove(location);
    }
}
