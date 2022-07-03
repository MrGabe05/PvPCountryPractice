package com.gabrielhd.practice.kit;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;

@Getter @Setter
public class Kit {

    private final String name;

    private boolean enabled;
    private boolean ranked;
    private boolean combo;
    private boolean sumo;
    private boolean build;
    private boolean spleef;
    private boolean parkour;

    private ItemStack icon;
    private ItemStack[] armor;
    private ItemStack[] contents;
    private ItemStack[] kitEditContents;

    private List<String> whitelistArenas;
    private List<String> blacklistArenas;

    public Kit(String name) {
        this.name = name;
    }

    public void applyToPlayer(Player player) {
        player.getInventory().setContents(this.contents);
        player.getInventory().setArmorContents(this.armor);

        player.updateInventory();
    }
}
