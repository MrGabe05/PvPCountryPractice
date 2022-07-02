package com.gabrielhd.practice.listeners;

import com.gabrielhd.practice.player.PlayerData;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerListener implements Listener {

    @EventHandler
    public void onJoinPlayer(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        PlayerData.load(player);
    }

    @EventHandler
    public void onQuitPlayer(PlayerQuitEvent event) {

    }

    @EventHandler
    public void onKickPlayer(PlayerKickEvent event) {

    }
}
