package com.gabrielhd.practice.events.types;

import com.gabrielhd.practice.events.CustomEvent;
import com.gabrielhd.practice.events.EventType;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.function.Consumer;

public class Sumo extends CustomEvent {

    public Sumo(Player host, Location spawn, EventType eventType) {
        super(host, spawn, eventType);
    }

    @Override
    public List<Location> getSpawnLocations() {
        return null;
    }

    @Override
    public void onStart() {

    }

    @Override
    public Consumer<Player> onDeath() {
        return null;
    }
}
