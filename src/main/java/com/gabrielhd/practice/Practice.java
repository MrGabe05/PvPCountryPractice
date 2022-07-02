package com.gabrielhd.practice;

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
}
