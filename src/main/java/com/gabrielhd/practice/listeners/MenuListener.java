package com.gabrielhd.practice.listeners;

import com.gabrielhd.practice.menus.Menu;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class MenuListener implements Listener {

    @EventHandler
    public void onPlayerLeaveInvRemove(PlayerQuitEvent playerQuitEvent) {
        Menu.remove(playerQuitEvent.getPlayer());
    }

    @EventHandler
    public void onPlayerKickInvRemove(PlayerKickEvent playerKickEvent) {
        Menu.remove(playerKickEvent.getPlayer());
    }

    @EventHandler
    public void onInventoryOpen(InventoryOpenEvent inventoryOpenEvent) {
        for (Menu menu : Menu.getPlayerMenus((Player)inventoryOpenEvent.getPlayer()).values()) {
            if (inventoryOpenEvent.getInventory().getTitle().equals(menu.getInv().getTitle())) {
                menu.onOpen(inventoryOpenEvent);
            }
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent inventoryCloseEvent) {
        for (Menu menu : Menu.getPlayerMenus((Player)inventoryCloseEvent.getPlayer()).values()) {
            if (inventoryCloseEvent.getInventory().getTitle().equals(menu.getInv().getTitle())) {
                menu.onClose(inventoryCloseEvent);
            }
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent inventoryClickEvent) {
        for (Menu menu : Menu.getPlayerMenus((Player)inventoryClickEvent.getWhoClicked()).values()) {
            if (inventoryClickEvent.getInventory().getTitle().equals(menu.getInv().getTitle()) && inventoryClickEvent.getCurrentItem() != null) {
                inventoryClickEvent.setCancelled(true);
                menu.onClick(inventoryClickEvent);
            }
        }
    }
}
