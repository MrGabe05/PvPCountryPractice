package com.gabrielhd.practice.tasks;

import com.gabrielhd.practice.Practice;
import com.gabrielhd.practice.arena.Arena;
import com.gabrielhd.practice.arena.StandArena;
import org.bukkit.Location;

import java.util.logging.Level;

public class ArenaCommandRunnable implements Runnable {

    private final Practice plugin;
    private final Arena copiedArena;
    private int times;
    
    @Override
    public void run() {
        this.duplicateArena(this.copiedArena, 10000, 10000);
    }
    
    private void duplicateArena(Arena arena, int offsetX, int offsetZ) {
        new DuplicateArenaRunnable(this.plugin, arena, offsetX, offsetZ, 500, 500) {
            @Override
            public void onComplete() {
                double minX = arena.getMin().getX() + this.getOffsetX();
                double minZ = arena.getMin().getZ() + this.getOffsetZ();
                double maxX = arena.getMax().getX() + this.getOffsetX();
                double maxZ = arena.getMax().getZ() + this.getOffsetZ();
                double aX = arena.getA().getX() + this.getOffsetX();
                double aZ = arena.getA().getZ() + this.getOffsetZ();
                double bX = arena.getB().getX() + this.getOffsetX();
                double bZ = arena.getB().getZ() + this.getOffsetZ();
                Location min = new Location(arena.getMin().getWorld(), minX, arena.getMin().getY(), minZ, arena.getMin().getYaw(), arena.getMin().getPitch());
                Location max = new Location(arena.getMax().getWorld(), maxX, arena.getMax().getY(), maxZ, arena.getMax().getYaw(), arena.getMax().getPitch());
                Location a = new Location(arena.getA().getWorld(), aX, arena.getA().getY(), aZ, arena.getA().getYaw(), arena.getA().getPitch());
                Location b = new Location(arena.getB().getWorld(), bX, arena.getB().getY(), bZ, arena.getA().getYaw(), arena.getA().getPitch());
                StandArena standaloneArena = new StandArena(a, b, min, max);
                arena.addArena(standaloneArena);
                arena.addAvailableArena(standaloneArena);
                int n = ArenaCommandRunnable.this.times - 1;
                ArenaCommandRunnable.set(ArenaCommandRunnable.this, n);
                if (n > 0) {
                    ArenaCommandRunnable.this.plugin.getServer().getLogger().log(Level.INFO, "Placed a standalone arena of {0} at {1}, {2}. {3} arenas remaining.", new Object[]{arena.getName(), (int)minX, (int)minZ, ArenaCommandRunnable.this.times});
                    ArenaCommandRunnable.this.duplicateArena(arena, (int)Math.round(maxX), (int)Math.round(maxZ));
                }
                else {
                    ArenaCommandRunnable.this.plugin.getServer().getLogger().log(Level.INFO, "Finished pasting {0}''s standalone arenas.", ArenaCommandRunnable.this.copiedArena.getName());
                    ArenaCommandRunnable.this.plugin.getArenaManager().setGeneratingArenaRunnables(ArenaCommandRunnable.this.plugin.getArenaManager().getGeneratingArenaRunnables() - 1);

                    ArenaCommandRunnable.this.plugin.getArenaManager().reloadArenas();
                }
            }
        }.run();
    }
    
    public Practice getPlugin() {
        return this.plugin;
    }
    
    public Arena getCopiedArena() {
        return this.copiedArena;
    }
    
    public int getTimes() {
        return this.times;
    }
    
    public ArenaCommandRunnable(Practice plugin, Arena copiedArena, int times) {
        this.plugin = plugin;
        this.copiedArena = copiedArena;
        this.times = times;
    }
    
    static void set(ArenaCommandRunnable arenaCommandRunnable, int times) {
        arenaCommandRunnable.times = times;
    }
}
