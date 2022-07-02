package com.gabrielhd.practice.events;

import com.gabrielhd.practice.player.PlayerData;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.function.Consumer;

@Getter @Setter
public abstract class CustomEvent {

    private Player host;

    private int minPlayers;
    private int maxPlayers;

    private Location spawn;

    private EventType eventType;

    private Set<UUID> participants;

    public CustomEvent(Player host, Location spawn, EventType eventType) {
        this.host = host;
        this.spawn = spawn;
        this.eventType = eventType;

        this.participants = new HashSet<>();
    }

    public boolean joinEvent(Player player) {
        if (this.participants.size() >= this.maxPlayers) {
            return false;
        }

        PlayerData playerData = PlayerData.of(player);



        return true;
    }

    public abstract List<Location> getSpawnLocations();

    public abstract void onStart();

    public abstract Consumer<Player> onDeath();
}
