package com.gabrielhd.practice.tasks;

import com.gabrielhd.practice.Practice;
import com.gabrielhd.practice.player.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class SaveDataRunnable implements Runnable {

    private final Practice plugin;
    
    @Override
    public void run() {
        for(Player player : Bukkit.getOnlinePlayers()) {
            PlayerData.save(player, false);
        }
    }
    
    public SaveDataRunnable() {
        this.plugin = Practice.getInstance();
    }
}
