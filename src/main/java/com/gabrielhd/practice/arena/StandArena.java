package com.gabrielhd.practice.arena;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;

@Getter @Setter
public class StandArena {

    private Location a;
    private Location b;
    private Location min;
    private Location max;

    public StandArena(Location a, Location b, Location min, Location max) {
        this.a = a;
        this.b = b;

        this.min = min;
        this.max = max;
    }
}
