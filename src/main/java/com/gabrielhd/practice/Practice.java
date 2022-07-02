package com.gabrielhd.practice;

import com.gabrielhd.practice.listeners.MenuListener;
import com.gabrielhd.practice.listeners.PlayerListener;
import com.gabrielhd.practice.manager.ArenaManager;
import com.gabrielhd.practice.manager.EventsManager;
import com.gabrielhd.practice.manager.KitManager;
import lombok.Getter;
import org.bukkit.plugin.java.JavaPlugin;

@Getter
public class Practice extends JavaPlugin {

    @Getter private static Practice instance;

    private KitManager kitManager;
    private ArenaManager arenaManager;
    private EventsManager eventsManager;

    @Override
    public void onEnable() {
        instance = this;

        this.registerListeners();

        kitManager = new KitManager();
        arenaManager = new ArenaManager();
        eventsManager = new EventsManager();
    }

    private void registerListeners() {
        this.getServer().getPluginManager().registerEvents(new MenuListener(), this);
        this.getServer().getPluginManager().registerEvents(new PlayerListener(), this);
    }
}
