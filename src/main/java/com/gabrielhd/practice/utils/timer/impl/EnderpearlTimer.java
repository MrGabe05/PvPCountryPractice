package com.gabrielhd.practice.utils.timer.impl;

import com.gabrielhd.practice.utils.timer.PlayerTimer;
import org.apache.commons.lang.time.DurationFormatUtils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.EnderPearl;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class EnderpearlTimer extends PlayerTimer implements Listener {
    
    public HashMap<Player, Entity> enderpearls = new HashMap<>();
    
    public EnderpearlTimer() {
        super("Enderpearl", TimeUnit.SECONDS.toMillis(15L));
    }
    
    @Override
    protected void handleExpiry(Player player, UUID playerUUID) {
        super.handleExpiry(player, playerUUID);
        this.enderpearls.remove(player);
        if (player == null) {
            return;
        }
        player.sendMessage(ChatColor.GREEN + "Ahora puedes usar enderpearls nuevamente.");
    }
    
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if ((event.getAction() != Action.RIGHT_CLICK_BLOCK && event.getAction() != Action.RIGHT_CLICK_AIR) || !event.hasItem()) {
            return;
        }
        Player player = event.getPlayer();
        if (event.getItem().getType() == Material.ENDER_PEARL) {
            long cooldown = this.getRemaining(player);
            if (cooldown > 0L) {
                event.setCancelled(true);
                player.sendMessage(ChatColor.YELLOW + "Enderpearl cooldown: " + ChatColor.WHITE + DurationFormatUtils.formatDurationWords(cooldown, true, true));
                player.updateInventory();
            }
        }
    }
    
    @EventHandler
    public void onPearlLaunch(ProjectileLaunchEvent event) {
        if (event.getEntity().getShooter() instanceof Player && event.getEntity() instanceof EnderPearl) {
            Player player = (Player)event.getEntity().getShooter();
            this.setCooldown(player, player.getUniqueId());
            this.enderpearls.put(player, event.getEntity());
        }
    }
    
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerTeleportEvent(PlayerTeleportEvent event) {
        if (event.getCause() != PlayerTeleportEvent.TeleportCause.ENDER_PEARL) {
            return;
        }

        Player player = event.getPlayer();
        if (this.getRemaining(player) != 0L && event.isCancelled()) {
            this.enderpearls.get(player).remove();
            this.enderpearls.remove(player);
            this.clearCooldown(player);
        }
    }
}
