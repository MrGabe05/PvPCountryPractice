package com.gabrielhd.practice;

import com.gabrielhd.practice.config.YamlConfig;
import com.gabrielhd.practice.listeners.MenuListener;
import com.gabrielhd.practice.listeners.PlayerListener;
import com.gabrielhd.practice.manager.*;
import com.gabrielhd.practice.utils.others.Cuboid;
import com.gabrielhd.practice.utils.others.LocUtils;
import com.gabrielhd.practice.utils.timer.TimerManager;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;

@Getter
public class Practice extends JavaPlugin {

    @Getter private static Practice instance;

    private Cuboid regionSpawn;
    private List<Location> spawnsLocation;

    private KitManager kitManager;
    private ItemManager itemManager;
    private MatchManager matchManager;
    private ArenaManager arenaManager;
    private EventManager eventManager;
    private PartyManager partyManager;
    private TimerManager timerManager;
    private PlayerManager playerManager;

    @Override
    public void onEnable() {
        instance = this;

        this.loadSpawn();
        this.registerListeners();

        kitManager = new KitManager();
        itemManager = new ItemManager();
        matchManager = new MatchManager();
        arenaManager = new ArenaManager();
        eventManager = new EventManager();
        partyManager = new PartyManager();
        timerManager = new TimerManager();
        playerManager = new PlayerManager();
    }

    private void registerListeners() {
        this.getServer().getPluginManager().registerEvents(new MenuListener(), this);
        this.getServer().getPluginManager().registerEvents(new PlayerListener(), this);
    }

    public void loadSpawn() {
        YamlConfig spawnConfig = new YamlConfig(this, "Spawn");

        this.spawnsLocation = new ArrayList<>();

        spawnConfig.getStringList("Spawns").forEach(spawn -> this.spawnsLocation.add(LocUtils.StringToLocation(spawn)));

        Location max = LocUtils.StringToLocation("Max");
        Location min = LocUtils.StringToLocation("Min");

        this.regionSpawn = new Cuboid(max, min);
    }
}
