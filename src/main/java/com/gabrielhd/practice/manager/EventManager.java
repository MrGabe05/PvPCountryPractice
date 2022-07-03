package com.gabrielhd.practice.manager;

import com.gabrielhd.practice.Practice;
import com.gabrielhd.practice.events.CustomEvent;
import com.gabrielhd.practice.events.EventState;
import com.gabrielhd.practice.events.types.Sumo;
import com.gabrielhd.practice.player.PlayerData;
import com.gabrielhd.practice.player.PlayerState;
import com.google.common.collect.ImmutableList;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

@Getter @Setter
public class EventManager {

    private final Practice plugin;

    private final Map<UUID, CustomEvent> spectators;
    private final Map<Class<? extends CustomEvent>, CustomEvent> events;

    private final World eventWorld;
    private long cooldown;

    public EventManager() {
        this.plugin = Practice.getInstance();

        this.cooldown = 0L;
        this.events = new HashMap<>();
        this.spectators = new HashMap<>();

        ImmutableList.of(Sumo.class).forEach(this::addEvent);

        boolean newWorld;
        if (this.plugin.getServer().getWorld("event") == null) {
            this.eventWorld = this.plugin.getServer().createWorld(new WorldCreator("event"));
            newWorld = true;
        } else {
            this.eventWorld = this.plugin.getServer().getWorld("event");
            newWorld = false;
        }

        if (this.eventWorld != null) {
            if (newWorld) {
                this.plugin.getServer().getWorlds().add(this.eventWorld);
            }
            this.eventWorld.setTime(2000L);
            this.eventWorld.setGameRuleValue("doDaylightCycle", "false");
            this.eventWorld.setGameRuleValue("doMobSpawning", "false");
            this.eventWorld.setStorm(false);
            this.eventWorld.getEntities().stream().filter(entity -> !(entity instanceof Player)).forEach(Entity::remove);
        }
    }

    public CustomEvent getByName(String name) {
        return this.events.values().stream().filter(event -> event.getEventType().name().equalsIgnoreCase(name)).findFirst().orElse(null);
    }

    public void hostEvent(CustomEvent event, Player host) {
        event.setState(EventState.WAITING);
        event.setHost(host);

        event.startCountdown();
    }

    private void addEvent(Class<? extends CustomEvent> clazz) {
        CustomEvent event = null;
        try {
            event = clazz.newInstance();
        }
        catch (InstantiationException | IllegalAccessException ex2) {
            System.out.println(ex2);
        }
        this.events.put(clazz, event);
    }

    public void addSpectatorSumo(Player player, PlayerData playerData, Sumo event) {
        this.addSpectator(player, playerData, event);
        if (event.getSpawnLocations().size() == 1) {
            player.teleport(event.getSpawnLocations().get(0));
        } else {
            List<Location> spawnLocations = new ArrayList<>(event.getSpawnLocations());
            player.teleport(spawnLocations.remove(ThreadLocalRandom.current().nextInt(spawnLocations.size())));
        }

        for (Player eventPlayer : event.getBukkitPlayers()) {
            player.showPlayer(eventPlayer);
        }
        player.setGameMode(GameMode.SPECTATOR);
        player.setAllowFlight(true);
        player.setFlying(true);
    }

    private void addSpectator(Player player, PlayerData playerData, CustomEvent event) {
        playerData.setPlayerState(PlayerState.SPECTATING);

        this.spectators.put(player.getUniqueId(), event);

        player.getInventory().setContents(this.plugin.getItemManager().getSpecItems());
        player.updateInventory();

        this.plugin.getServer().getOnlinePlayers().forEach(online -> {
            online.hidePlayer(player);
            player.hidePlayer(online);
        });
    }

    public void removeSpectator(Player player) {
        this.getSpectators().remove(player.getUniqueId());

        this.plugin.getPlayerManager().sendToSpawnAndReset(player);
    }

    public boolean isPlaying(Player player, CustomEvent event) {
        return event.getParticipants().containsKey(player.getUniqueId());
    }

    public CustomEvent getEventPlaying(Player player) {
        return this.events.values().stream().filter(event -> this.isPlaying(player, event)).findFirst().orElse(null);
    }
}
