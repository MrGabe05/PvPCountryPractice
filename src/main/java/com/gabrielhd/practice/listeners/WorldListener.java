package com.gabrielhd.practice.listeners;

import com.gabrielhd.practice.Practice;
import com.gabrielhd.practice.arena.StandArena;
import com.gabrielhd.practice.match.Match;
import com.gabrielhd.practice.player.PlayerData;
import com.gabrielhd.practice.player.PlayerState;
import com.gabrielhd.practice.utils.others.PlayerUtil;
import net.minecraft.server.v1_8_R3.EntityHuman;
import org.bukkit.GameMode;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.hanging.HangingBreakEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.weather.WeatherChangeEvent;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;

public class WorldListener implements Listener
{
    private final Practice plugin;
    
    public WorldListener() {
        this.plugin = Practice.getInstance();
    }
    
    @EventHandler
    public void onChangedWorld(PlayerChangedWorldEvent e) {
        Player player = e.getPlayer();
        if(e.getFrom().getName().equalsIgnoreCase("lobby")) {
            PlayerUtil.clearPlayer(player);
            for(Player p2 : e.getFrom().getPlayers()) {
                player.hidePlayer(p2);
            }
            PlayerData playerData = PlayerData.of(player);
            if(playerData != null) {
                if(playerData.getPlayerState() == PlayerState.EVENT) {
                    return;
                }
                if(playerData.getPlayerState() == PlayerState.FIGHTING) {
                    player.setGameMode(GameMode.SURVIVAL);
                    if (this.plugin.getMatchManager().getMatch(playerData) != null) {
                        Match match = this.plugin.getMatchManager().getMatch(playerData);
                        if (match != null) {
                            Set<Player> pelea = new HashSet<>();
                            match.getTeams().forEach(team -> team.alivePlayers().forEach(pelea::add));
                            for (Player peleadores : pelea) {
                                for (Player online : this.plugin.getServer().getOnlinePlayers()) {
                                    online.hidePlayer(peleadores);
                                    peleadores.hidePlayer(online);
                                }
                            }
                            for (Player peleadores : pelea) {
                                for (Player other : pelea) {
                                    peleadores.showPlayer(other);
                                    other.showPlayer(peleadores);
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        PlayerData playerData = PlayerData.of(player);
        if (playerData == null) {
            this.plugin.getLogger().log(Level.WARNING, "{0}''s player data is null", String.valueOf(player.getName()));
            event.setCancelled(true);
            return;
        }
        if (playerData.getPlayerState() == PlayerState.FIGHTING) {
            Match match = this.plugin.getMatchManager().getMatch(player.getUniqueId());
            if (match.getKit().isBuild()) {
                if (!match.getPlacedBlockLocations().contains(event.getBlock().getLocation())) {
                    event.setCancelled(true);
                }
            } else {
                event.setCancelled(true);
            }
        }
        else if (!player.isOp() && player.getGameMode() != GameMode.CREATIVE) {
            event.setCancelled(true);
        }
    }
    
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        PlayerData playerData = PlayerData.of(player);
        if (playerData == null) {
            this.plugin.getLogger().log(Level.WARNING, "{0}''s player data is null", String.valueOf(player.getName()));
            event.setCancelled(true);
            return;
        }
        if (playerData.getPlayerState() != PlayerState.FIGHTING) {
            if (!player.isOp() || player.getGameMode() != GameMode.CREATIVE) {
                event.setCancelled(true);
            }
            return;
        }
        Match match = this.plugin.getMatchManager().getMatch(player.getUniqueId());
        if (match == null) {
            event.setCancelled(true);
            return;
        }
        if (!match.getKit().isBuild()) {
            event.setCancelled(true);
        }
        else {
            if(match.getStandArena() == null) {
                return;
            }
            double minX = match.getStandArena().getMin().getX();
            double minZ = match.getStandArena().getMin().getZ();
            double maxX = match.getStandArena().getMax().getX();
            double maxZ = match.getStandArena().getMax().getZ();
            if (minX > maxX) {
                double lastMinX = minX;
                minX = maxX;
                maxX = lastMinX;
            }
            if (minZ > maxZ) {
                double lastMinZ = minZ;
                minZ = maxZ;
                maxZ = lastMinZ;
            }
            if (player.getLocation().getX() >= minX && player.getLocation().getX() <= maxX && player.getLocation().getZ() >= minZ && player.getLocation().getZ() <= maxZ) {
                if (player.getLocation().getY() - match.getStandArena().getA().getY() < 5.0 && event.getBlockPlaced() != null) {
                    match.addPlacedBlockLocation(event.getBlockPlaced().getLocation());
                    match.getBlockTracker().add(event.getBlockReplacedState());
                    match.getBlockTracker().add(event.getBlock().getRelative(BlockFace.DOWN).getState());
                }
                else {
                    event.setCancelled(true);
                }
            }
            else {
                event.setCancelled(true);
            }
        }
    }
    
    @EventHandler
    public void onBucketEmpty(PlayerBucketEmptyEvent event) {
        Player player = event.getPlayer();
        PlayerData playerData = PlayerData.of(player);
        if (playerData == null) {
            this.plugin.getLogger().log(Level.WARNING, "{0}''s player data is null", String.valueOf(player.getName()));
            event.setCancelled(true);
            return;
        }
        if (playerData.getPlayerState() == PlayerState.FIGHTING) {
            Match match = this.plugin.getMatchManager().getMatch(player.getUniqueId());
            if (!match.getKit().isBuild()) {
                event.setCancelled(true);
            }
            else {
                if(match.getStandArena() == null) {
                    return;
                }
                double minX = match.getStandArena().getMin().getX();
                double minZ = match.getStandArena().getMin().getZ();
                double maxX = match.getStandArena().getMax().getX();
                double maxZ = match.getStandArena().getMax().getZ();
                if (minX > maxX) {
                    double lastMinX = minX;
                    minX = maxX;
                    maxX = lastMinX;
                }
                if (minZ > maxZ) {
                    double lastMinZ = minZ;
                    minZ = maxZ;
                    maxZ = lastMinZ;
                }
                if (player.getLocation().getX() >= minX && player.getLocation().getX() <= maxX && player.getLocation().getZ() >= minZ && player.getLocation().getZ() <= maxZ) {
                    if (player.getLocation().getY() - match.getStandArena().getA().getY() < 5.0) {
                        Block block = event.getBlockClicked().getRelative(event.getBlockFace());
                        match.addPlacedBlockLocation(block.getLocation());
                        match.getBlockTracker().add(event.getBlockClicked().getRelative(event.getBlockFace()).getState());
                    }
                    else {
                        event.setCancelled(true);
                    }
                }
                else {
                    event.setCancelled(true);
                }
            }
            return;
        }
        if (!player.isOp() || player.getGameMode() != GameMode.CREATIVE) {
            event.setCancelled(true);
        }
    }
    
    @EventHandler
    public void onBlockFromTo(BlockFromToEvent event) {
        if (event.getToBlock() == null) {
            return;
        }
        for (StandArena arena : this.plugin.getArenaManager().getArenaMatchUUIDs().keySet()) {
            if(arena == null) {
                return;
            }
            double minX = arena.getMin().getX();
            double minZ = arena.getMin().getZ();
            double maxX = arena.getMax().getX();
            double maxZ = arena.getMax().getZ();
            if (minX > maxX) {
                double lastMinX = minX;
                minX = maxX;
                maxX = lastMinX;
            }
            if (minZ > maxZ) {
                double lastMinZ = minZ;
                minZ = maxZ;
                maxZ = lastMinZ;
            }
            if(event.getBlock().getLocation() != null && event.getBlock() != null && event.getToBlock() != null) {
                if(minX != 0.0 && maxX != 0.0 && minZ != 0.0 && maxZ != 0.0) {
                    if (event.getToBlock().getX() >= minX && event.getToBlock().getZ() >= minZ && event.getToBlock().getX() <= maxX && event.getToBlock().getZ() <= maxZ) {
                        UUID matchUUID = this.plugin.getArenaManager().getArenaMatchUUID(arena);
                        if(matchUUID == null) {
                            event.setCancelled(true);
                            return;
                        }
                        Match match = this.plugin.getMatchManager().getMatchFromUUID(matchUUID);
                        if(match != null) {
                            match.addPlacedBlockLocation(event.getToBlock().getLocation());
                            match.getBlockTracker().add(event.getToBlock().getState());
                            match.getBlockTracker().add(event.getToBlock().getRelative(BlockFace.DOWN).getState());
                            event.setCancelled(false);
                        } else {
                            event.setCancelled(true);
                        }
                        break;
                    }
                }
            }
        }
    }

    @EventHandler
    public void onBlockForm(BlockFormEvent event) {
        if(event.getBlock() == null) {
            return;
        }
        for (StandArena arena : this.plugin.getArenaManager().getArenaMatchUUIDs().keySet()) {
            if (arena == null) {
                return;
            }
            UUID matchUUID = this.plugin.getArenaManager().getArenaMatchUUID(arena);
            if(matchUUID == null) {
                event.setCancelled(true);
                return;
            }
            Match match = this.plugin.getMatchManager().getMatchFromUUID(matchUUID);
            if(match != null) {
                match.getBlockTracker().add(event.getBlock().getState());
            }
        }
    }
    
    @EventHandler
    public void onWeatherChange(WeatherChangeEvent event) {
        if (event.toWeatherState()) {
            event.setCancelled(true);
        }
    }
    
    @EventHandler
    public void onLeavesDecay(LeavesDecayEvent event) {
        event.setCancelled(true);
    }
    
    @EventHandler
    public void onHangingBreak(HangingBreakEvent event) {
        event.setCancelled(true);
    }
    
    @EventHandler
    public void onBlockBurn(BlockBurnEvent event) {
        event.setCancelled(true);
    }
    
    @EventHandler
    public void onBlockSpread(BlockSpreadEvent event) {
        event.setCancelled(true);
    }
}
