package com.gabrielhd.practice.arena;

import com.gabrielhd.practice.utils.Cuboid;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;

@Getter @Setter
public class StandArena {

    private Location a;
    private Location b;
    private Cuboid cuboid;

    public StandArena(Location a, Location b, Location min, Location max) {
        this.a = a;
        this.b = b;

        this.cuboid = new Cuboid(max, min);
    }
}
