package com.gabrielhd.practice.manager;

import com.gabrielhd.practice.Practice;
import com.gabrielhd.practice.arena.Arena;
import com.gabrielhd.practice.arena.StandArena;
import com.gabrielhd.practice.events.CustomEvent;
import org.bukkit.Chunk;
import org.bukkit.Location;

public class ChunkManager {

    private final Practice plugin;
    private boolean chunksLoaded;
    
    public ChunkManager() {
        this.plugin = Practice.getInstance();

        this.plugin.getServer().getScheduler().runTaskLater(this.plugin, this::loadChunks, 1L);
    }
    
    private void loadChunks() {
        this.plugin.getLogger().info("Started loading all the chunks...");

            Location spawnMin = this.plugin.getRegionSpawnMax();
            Location spawnMax = this.plugin.getRegionSpawnMin();

            if (spawnMin != null && spawnMax != null) {
                int spawnMinX = spawnMin.getBlockX() >> 4;
                int spawnMinZ = spawnMin.getBlockZ() >> 4;
                int spawnMaxX = spawnMax.getBlockX() >> 4;
                int spawnMaxZ = spawnMax.getBlockZ() >> 4;
                if (spawnMinX > spawnMaxX) {
                    int lastSpawnMinX = spawnMinX;
                    spawnMinX = spawnMaxX;
                    spawnMaxX = lastSpawnMinX;
                }
                if (spawnMinZ > spawnMaxZ) {
                    int lastSpawnMinZ = spawnMinZ;
                    spawnMinZ = spawnMaxZ;
                    spawnMaxZ = lastSpawnMinZ;
                }
                for (int x = spawnMinX; x <= spawnMaxX; ++x) {
                    for (int z = spawnMinZ; z <= spawnMaxZ; ++z) {
                        Chunk chunk = spawnMin.getWorld().getChunkAt(x, z);
                        if (!chunk.isLoaded()) {
                            chunk.load();
                        }
                    }
                }
            }

        for(CustomEvent event : this.plugin.getEventManager().getEvents().values()) {
            if(event.getMax() == null && event.getMin() == null) continue;

            Location sumoMin = event.getMin();
            Location sumoMax = event.getMax();
            if (sumoMin != null && sumoMax != null) {
                int sumoMinX = sumoMin.getBlockX() >> 4;
                int sumoMinZ = sumoMin.getBlockZ() >> 4;
                int sumoMaxX = sumoMax.getBlockX() >> 4;
                int sumoMaxZ = sumoMax.getBlockZ() >> 4;
                if (sumoMinX > sumoMaxX) {
                    int lastSumoMinX = sumoMinX;
                    sumoMinX = sumoMaxX;
                    sumoMaxX = lastSumoMinX;
                }
                if (sumoMinZ > sumoMaxZ) {
                    int lastSumoMaxZ = sumoMinZ;
                    sumoMinZ = sumoMaxZ;
                    sumoMaxZ = lastSumoMaxZ;
                }
                for (int x3 = sumoMinX; x3 <= sumoMaxX; ++x3) {
                    for (int z3 = sumoMinZ; z3 <= sumoMaxZ; ++z3) {
                        Chunk chunk3 = sumoMin.getWorld().getChunkAt(x3, z3);
                        if (!chunk3.isLoaded()) {
                            chunk3.load();
                        }
                    }
                }
            }
        }

        for (Arena arena : this.plugin.getArenaManager().getArenas().values()) {
            if (!arena.isEnabled() || arena.getMax() == null || arena.getMin() == null) continue;

            int arenaMinX = arena.getMin().getBlockX() >> 4;
            int arenaMinZ = arena.getMin().getBlockZ() >> 4;
            int arenaMaxX = arena.getMax().getBlockX() >> 4;
            int arenaMaxZ = arena.getMax().getBlockZ() >> 4;
            if (arenaMinX > arenaMaxX) {
                int lastArenaMinX = arenaMinX;
                arenaMinX = arenaMaxX;
                arenaMaxX = lastArenaMinX;
            }
            if (arenaMinZ > arenaMaxZ) {
                int lastArenaMinZ = arenaMinZ;
                arenaMinZ = arenaMaxZ;
                arenaMaxZ = lastArenaMinZ;
            }
            for (int x6 = arenaMinX; x6 <= arenaMaxX; ++x6) {
                for (int z6 = arenaMinZ; z6 <= arenaMaxZ; ++z6) {
                    Chunk chunk6 = arena.getMin().getWorld().getChunkAt(x6, z6);
                    if (!chunk6.isLoaded()) {
                        chunk6.load();
                    }
                }
            }

            for (StandArena saArena : arena.getStandArenas()) {
                if(arena.getMax() == null || arena.getMin() == null) continue;

                arenaMinX = saArena.getMin().getBlockX() >> 4;
                arenaMinZ = saArena.getMin().getBlockZ() >> 4;
                arenaMaxX = saArena.getMax().getBlockX() >> 4;
                arenaMaxZ = saArena.getMax().getBlockZ() >> 4;
                if (arenaMinX > arenaMaxX) {
                    int lastArenaMinX2 = arenaMinX;
                    arenaMinX = arenaMaxX;
                    arenaMaxX = lastArenaMinX2;
                }
                if (arenaMinZ > arenaMaxZ) {
                    int lastArenaMinZ2 = arenaMinZ;
                    arenaMinZ = arenaMaxZ;
                    arenaMaxZ = lastArenaMinZ2;
                }
                for (int x7 = arenaMinX; x7 <= arenaMaxX; ++x7) {
                    for (int z7 = arenaMinZ; z7 <= arenaMaxZ; ++z7) {
                        Chunk chunk7 = saArena.getMin().getWorld().getChunkAt(x7, z7);
                        if (!chunk7.isLoaded()) {
                            chunk7.load();
                        }
                    }
                }
            }
        }
        this.plugin.getLogger().info("Finished uploading all the chunks!");
        this.chunksLoaded = true;
    }
    
    public boolean isChunksLoaded() {
        return this.chunksLoaded;
    }
}
