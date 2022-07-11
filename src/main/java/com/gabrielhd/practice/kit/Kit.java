package com.gabrielhd.practice.kit;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

    private Set<String> whitelistArenas;
    private Set<String> blacklistArenas;

    public Kit(String name) {
        this.name = name;

        this.armor = new ItemStack[4];
        this.contents = new ItemStack[9];
        this.icon = new ItemStack(Material.BEDROCK);

        this.whitelistArenas = new HashSet<>();
        this.blacklistArenas = new HashSet<>();
    }

    public void applyToPlayer(Player player) {
        player.getInventory().setContents(this.contents);
        player.getInventory().setArmorContents(this.armor);

        player.updateInventory();
    }
}
