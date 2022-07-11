package com.gabrielhd.practice.listeners;

import com.gabrielhd.practice.menus.Menu;
import com.gabrielhd.practice.player.PlayerData;
import com.gabrielhd.practice.settings.item.ProfileOptionsItem;
import com.gabrielhd.practice.settings.item.ProfileOptionsItemState;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.Objects;

public class ProfileOptionsListeners implements Listener {

    @EventHandler
    public void onInventoryInteractEvent(InventoryClickEvent event) {
        Player player = (Player)event.getWhoClicked();
        PlayerData profile = PlayerData.of(player);

        Inventory inventory = event.getInventory();
        ItemStack itemStack = event.getCurrentItem();
        if (itemStack != null && itemStack.getType() != Material.AIR) {
            Inventory options = profile.getOptions().getInventory();
            if (inventory.getTitle().equals(options.getTitle()) && Arrays.equals(inventory.getContents(), options.getContents())) {
                event.setCancelled(true);
                ProfileOptionsItem item = ProfileOptionsItem.fromItem(itemStack);
                if (item != null) {
                    if (item == ProfileOptionsItem.DUEL_REQUESTS) {
                        profile.getOptions().setDuelRequests(!profile.getOptions().isDuelRequests());
                        inventory.setItem(event.getRawSlot(), item.getItem(profile.getOptions().isDuelRequests() ? ProfileOptionsItemState.ENABLED : ProfileOptionsItemState.DISABLED));
                    }
                    else if (item == ProfileOptionsItem.PARTY_INVITES) {
                        profile.getOptions().setPartyInvites(!profile.getOptions().isPartyInvites());
                        inventory.setItem(event.getRawSlot(), item.getItem(profile.getOptions().isPartyInvites() ? ProfileOptionsItemState.ENABLED : ProfileOptionsItemState.DISABLED));
                    }
                    else if (item == ProfileOptionsItem.TOGGLE_SCOREBOARD) {
                        if (profile.getOptions().getScoreboard() == ProfileOptionsItemState.ENABLED) {
                            profile.getOptions().setScoreboard(ProfileOptionsItemState.SHOW_PING);
                        }
                        else if (profile.getOptions().getScoreboard() == ProfileOptionsItemState.SHOW_PING) {
                            profile.getOptions().setScoreboard(ProfileOptionsItemState.DISABLED);
                        }
                        else if (profile.getOptions().getScoreboard() == ProfileOptionsItemState.DISABLED) {
                            profile.getOptions().setScoreboard(ProfileOptionsItemState.ENABLED);
                        }
                        inventory.setItem(event.getRawSlot(), item.getItem(profile.getOptions().getScoreboard()));
                    }
                    else if (item == ProfileOptionsItem.ALLOW_SPECTATORS) {
                        profile.getOptions().setSpectators(!profile.getOptions().isSpectators());
                        inventory.setItem(event.getRawSlot(), item.getItem(profile.getOptions().isSpectators() ? ProfileOptionsItemState.ENABLED : ProfileOptionsItemState.DISABLED));
                    }
                    else if (item == ProfileOptionsItem.TOGGLE_TIME) {
                        if (profile.getOptions().getTime() == ProfileOptionsItemState.DAY) {
                            profile.getOptions().setTime(ProfileOptionsItemState.SUNSET);
                            player.performCommand("sunset");
                        }
                        else if (profile.getOptions().getTime() == ProfileOptionsItemState.SUNSET) {
                            profile.getOptions().setTime(ProfileOptionsItemState.NIGHT);
                            player.performCommand("night");
                        }
                        else if (profile.getOptions().getTime() == ProfileOptionsItemState.NIGHT) {
                            profile.getOptions().setTime(ProfileOptionsItemState.DAY);
                            player.performCommand("day");
                        }
                        inventory.setItem(event.getRawSlot(), item.getItem(profile.getOptions().getTime()));
                    } else if (item == ProfileOptionsItem.TRAILS) {
                        player.openInventory(Objects.requireNonNull(Menu.getPlayerMenu(player, "settingsTrails")).getInv());
                    }
                }
            }
        }
    }
}
