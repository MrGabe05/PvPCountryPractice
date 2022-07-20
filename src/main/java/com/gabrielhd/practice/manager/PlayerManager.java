package com.gabrielhd.practice.manager;

import com.gabrielhd.practice.Practice;
import com.gabrielhd.practice.party.Party;
import com.gabrielhd.practice.player.PlayerData;
import com.gabrielhd.practice.player.PlayerState;
import com.gabrielhd.practice.utils.others.PlayerUtil;
import com.gabrielhd.practice.utils.timer.impl.EnderpearlTimer;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class PlayerManager {

    public void giveLobbyItems(Player player) {
        boolean inParty = Party.of(player.getUniqueId()) != null;
        boolean inEvent = Practice.getInstance().getEventManager().getEventPlaying(player) != null;
        ItemStack[] items = Practice.getInstance().getItemManager().getSpawnItemStack();

        if (inEvent) {
            items = Practice.getInstance().getItemManager().getEventItemStack();
        } else if (inParty) {
            items = Practice.getInstance().getItemManager().getPartyItemStack();
        }
        player.getInventory().setContents(items);
        player.updateInventory();
    }

    public void sendToSpawnEventFinish(Player player) {
        PlayerData pdata = PlayerData.of(player);
        pdata.setPlayerState(PlayerState.SPAWN);

        PlayerUtil.clearPlayer(player);
        this.giveLobbyItems(player);

        Practice.getInstance().getEventManager().getEventWorld().getPlayers().remove(player);

        if(!Practice.getInstance().getSpawnsLocation().isEmpty()) {
            if (Practice.getInstance().getSpawnsLocation().size() == 1) {
                player.teleport(Practice.getInstance().getSpawnsLocation().get(0));
            }
            else {
                List<Location> spawnLocations = new ArrayList<>(Practice.getInstance().getSpawnsLocation());
                player.teleport(spawnLocations.remove(ThreadLocalRandom.current().nextInt(spawnLocations.size())));
            }
        }

        for(Player player2 : Bukkit.getOnlinePlayers()) {
            if(player2.getWorld() == player.getWorld()) {
                player2.showPlayer(player);
                player.showPlayer(player2);
            } else {
                player2.hidePlayer(player);
                player.hidePlayer(player2);
            }
        }

        player.setGameMode(GameMode.ADVENTURE);
    }

    public void sendToSpawnAndReset(Player player) {
        PlayerData pdata = PlayerData.of(player);
        pdata.setPlayerState(PlayerState.SPAWN);

        if(Practice.getInstance().getTimerManager().getTimer(EnderpearlTimer.class) != null) {
            Practice.getInstance().getTimerManager().getTimer(EnderpearlTimer.class).clearCooldown(player);
        }

        if(!Practice.getInstance().getSpawnsLocation().isEmpty()) {
            if (Practice.getInstance().getSpawnsLocation().size() == 1) {
                player.teleport(Practice.getInstance().getSpawnsLocation().get(0));
            }
            else {
                List<Location> spawnLocations = new ArrayList<>(Practice.getInstance().getSpawnsLocation());
                player.teleport(spawnLocations.remove(ThreadLocalRandom.current().nextInt(spawnLocations.size())));
            }
        }

        PlayerUtil.clearPlayer(player);
        this.giveLobbyItems(player);

        for(Player player2 : Bukkit.getOnlinePlayers()) {
            if(player2.getWorld() == player.getWorld()) {
                player2.showPlayer(player);
                player.showPlayer(player2);
            } else {
                player2.hidePlayer(player);
                player.hidePlayer(player2);
            }
        }

        player.setGameMode(GameMode.ADVENTURE);
    }
}
