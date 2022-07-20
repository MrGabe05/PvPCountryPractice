package com.gabrielhd.practice;

import com.bizarrealex.aether.Aether;
import com.gabrielhd.practice.cache.StatusCache;
import com.gabrielhd.practice.commands.PartyCommand;
import com.gabrielhd.practice.commands.RankedsCommand;
import com.gabrielhd.practice.commands.StatsCommand;
import com.gabrielhd.practice.commands.management.ArenaCommand;
import com.gabrielhd.practice.commands.management.KitCommand;
import com.gabrielhd.practice.commands.management.ResetStatsCommand;
import com.gabrielhd.practice.commands.management.SpawnsCommand;
import com.gabrielhd.practice.commands.time.DayCommand;
import com.gabrielhd.practice.commands.time.NightCommand;
import com.gabrielhd.practice.commands.time.SunsetCommand;
import com.gabrielhd.practice.commands.toggle.SettingsCommand;
import com.gabrielhd.practice.config.YamlConfig;
import com.gabrielhd.practice.database.Database;
import com.gabrielhd.practice.lang.Lang;
import com.gabrielhd.practice.listeners.*;
import com.gabrielhd.practice.manager.*;
import com.gabrielhd.practice.scoreboard.PracticeBoard;
import com.gabrielhd.practice.tasks.ExpBarRunnable;
import com.gabrielhd.practice.tasks.SaveDataRunnable;
import com.gabrielhd.practice.utils.inventory.UIListener;
import com.gabrielhd.practice.utils.others.LocUtils;
import com.gabrielhd.practice.utils.timer.TimerManager;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.server.v1_8_R3.MinecraftServer;
import org.bukkit.Bukkit;
import org.bukkit.Difficulty;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Getter
public class Practice extends JavaPlugin {

    @Getter private static Practice instance;

    private List<Location> spawnsLocation;

    @Setter private Location regionSpawnMax;
    @Setter private Location regionSpawnMin;

    private KitManager kitManager;
    private ItemManager itemManager;
    private ChunkManager chunkManager;
    private QueueManager queueManager;
    private MatchManager matchManager;
    private ArenaManager arenaManager;
    private EventManager eventManager;
    private TimerManager timerManager;
    private PlayerManager playerManager;
    private InventoryManager inventoryManager;

    @Override
    public void onEnable() {
        instance = this;

        new Database(this);

        this.loadSpawn();
        this.loadConfig();
        this.registerManagers();
        this.registerCommands();
        this.registerListeners();
        this.loadWorldSettings();
        this.loadTasks();

        if(new YamlConfig(this, "Scoreboard").getBoolean("Scoreboard.Enabled", true)) {
            new Aether(this, new PracticeBoard());
        }

        new StatusCache().start();
    }

    private void loadConfig() {
        File lang = new File(this.getDataFolder(), "lang/");
        if(!lang.exists()) lang.mkdir();

        new YamlConfig(this, "Events");
        new YamlConfig(this, "Items");
        new YamlConfig(this, "Scoreboard");
        new YamlConfig(this, "Settings");

        new YamlConfig(this, "lang/lang_en");

        Lang.loadLangs();
    }

    private void loadWorldSettings() {
        for(World w : Bukkit.getWorlds()) {
            w.setAutoSave(false);
            w.setGameRuleValue("doMobSpawning", "false");
            w.setGameRuleValue("commandBlockOutput", "false");
            w.setTime(0);
            w.setDifficulty(Difficulty.HARD);
        }
    }

    private void loadTasks() {
        this.getServer().getScheduler().runTaskTimerAsynchronously(this, new SaveDataRunnable(), 6000L, 6000L);
        this.getServer().getScheduler().runTaskTimerAsynchronously(this, new ExpBarRunnable(), 2L, 2L);
    }

    private void registerManagers() {
        kitManager = new KitManager();
        itemManager = new ItemManager();
        queueManager = new QueueManager();
        matchManager = new MatchManager();
        arenaManager = new ArenaManager();
        eventManager = new EventManager();
        timerManager = new TimerManager();
        chunkManager = new ChunkManager();
        playerManager = new PlayerManager();
        inventoryManager = new InventoryManager();
    }

    private void registerCommands() {
        Arrays.asList(new SettingsCommand(), new RankedsCommand(), new ResetStatsCommand(), new SunsetCommand(), new ArenaCommand(), new NightCommand(), new PartyCommand(), new DayCommand(), new KitCommand(), new StatsCommand(), new SpawnsCommand()).forEach(command -> this.registerCommand(command, this.getName()));
    }

    private void registerListeners() {
        Arrays.asList(new EntityListener(), new PlayerListener(), new MatchListener(), new WorldListener(), new UIListener(), new ProfileOptionsListeners(), new MenuListener()).forEach(listener -> this.getServer().getPluginManager().registerEvents(listener, this));
    }

    public void loadSpawn() {
        YamlConfig spawnConfig = new YamlConfig(this, "Spawn");

        this.spawnsLocation = new ArrayList<>();

        if(!spawnConfig.getStringList("Spawns").isEmpty()) {
            spawnConfig.getStringList("Spawns").forEach(spawn -> this.spawnsLocation.add(LocUtils.StringToLocation(spawn)));
        }

        if(spawnConfig.isSet("Max") && spawnConfig.isSet("Min")) {
            this.regionSpawnMax = LocUtils.StringToLocation("Max");
            this.regionSpawnMin = LocUtils.StringToLocation("Min");
        }
    }

    public void registerCommand(Command cmd, String fallbackPrefix) {
        MinecraftServer.getServer().server.getCommandMap().register(cmd.getName(), fallbackPrefix, cmd);
    }
}
