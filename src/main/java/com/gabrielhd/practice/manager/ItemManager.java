package com.gabrielhd.practice.manager;

import com.gabrielhd.practice.Practice;
import com.gabrielhd.practice.config.YamlConfig;
import com.gabrielhd.practice.utils.items.BuilderItem;
import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.Set;

@Getter
public class ItemManager {

    private final ItemStack[] specItems;
    private final ItemStack[] spawnItems;
    private final ItemStack[] queueItems;
    private final ItemStack[] partyItems;
    private final ItemStack[] eventItems;
    private final ItemStack[] partySpecItems;
    private final ItemStack[] tournamentItems;
    private final ItemStack defaultBook;
    
    public ItemManager() {
        YamlConfig itemsConfig = new YamlConfig(Practice.getInstance(), "Items");

        this.specItems = getItems(itemsConfig, "Spec");
        this.spawnItems = getItems(itemsConfig, "Spawn");
        this.queueItems = getItems(itemsConfig, "Queue");
        this.partyItems = getItems(itemsConfig, "Party");
        this.eventItems = getItems(itemsConfig, "Events");
        this.partySpecItems = getItems(itemsConfig, "PartySpec");
        this.tournamentItems = getItems(itemsConfig, "Tournaments");

        this.defaultBook = new BuilderItem(Material.BOOK).build();
    }

    public ItemStack[] getItems(YamlConfig config, String name) {
        if(!config.isSet(name)) return new ItemStack[9];

        Set<String> section = config.getConfigurationSection(name).getKeys(false);
        ItemStack[] items = new ItemStack[9];
        for(String item : section) {
            String path = name + "." + item + ".";

            items[(Integer.parseInt(item) - 1)] = new BuilderItem(Material.getMaterial(config.getString(path + "ID"))).setTitle(config.getString(path + "Name")).setLore(config.getStringList(path + "Lore")).setGlow(config.getBoolean(path + "Glow")).build();
        }

        return items;
    }
}
