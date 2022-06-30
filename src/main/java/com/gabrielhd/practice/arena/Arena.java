package com.gabrielhd.practice.arena;

import com.gabrielhd.practice.utils.Cuboid;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;

import java.util.ArrayList;
import java.util.List;

@Getter @Setter
public class Arena {

    private final String name;

    private boolean enabled;

    private Location a;
    private Location b;
    private Cuboid cuboid;

    private List<StandArena> standArenas;
    private List<StandArena> availableArenas;

    private final List<StandArena> occupiedArenas = new ArrayList<>();
    
    public StandArena getAvailableArena() {
        StandArena arena = this.availableArenas.get(0);

        this.occupiedArenas.add(arena);
        this.availableArenas.remove(arena);

        return arena;
    }
    
    public void addArena(StandArena arena) {
        this.standArenas.add(arena);
    }
    
    public void removeArena(StandArena arena) {
        this.standArenas.remove(arena);
    }
    
    public void addAvailableArena(StandArena arena) {
        this.availableArenas.add(arena);
    }

    public void removeAvailableArena(StandArena arena) {
        this.availableArenas.remove(arena);
    }
    
    public Arena(String name, List<StandArena> standArenas, List<StandArena> availableArenas, Location a, Location b, Location min, Location max, boolean enabled) {
        this.name = name;
        this.standArenas = standArenas;
        this.availableArenas = availableArenas;
        this.a = a;
        this.b = b;

        this.enabled = enabled;
        this.cuboid = new Cuboid(max, min);
    }
    
    public Arena(String name) {
        this.name = name;
    }
}
