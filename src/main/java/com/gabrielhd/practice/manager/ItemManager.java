package com.gabrielhd.practice.manager;

import com.gabrielhd.practice.Practice;
import com.gabrielhd.practice.config.YamlConfig;
import com.gabrielhd.practice.utils.items.ActionItem;
import com.gabrielhd.practice.utils.items.BuilderItem;
import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.Set;

@Getter
public class ItemManager {

    private final ActionItem[] specItems;
    private final ActionItem[] spawnItems;
    private final ActionItem[] queueItems;
    private final ActionItem[] partyItems;
    private final ActionItem[] eventItems;
    private final ActionItem[] partySpecItems;
    private final ActionItem[] tournamentItems;
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

    public ActionItem[] getItems(YamlConfig config, String name) {
        if(!config.isSet(name)) return new ActionItem[9];

        Set<String> section = config.getConfigurationSection(name).getKeys(false);
        ActionItem[] items = new ActionItem[9];
        for(String item : section) {
            String path = name + "." + item + ".";

            ItemStack itemStack = new BuilderItem(Material.getMaterial(config.getString(path + "ID"))).setTitle(config.getString(path + "Name")).setLore(config.getStringList(path + "Lore")).setGlow(config.getBoolean(path + "Glow")).build();

            items[(Integer.parseInt(item) - 1)] = new ActionItem(itemStack, config.getString(path + "Action", "command"));;
        }

        return items;
    }

    private ItemStack[] getArray(ActionItem[] items) {
        return Arrays.stream(items).map(ActionItem::getItem).toArray(ItemStack[]::new);
    }

    public ItemStack[] getSpecItemStack() {
        return this.getArray(this.specItems);
    }

    public ItemStack[] getSpawnItemStack() {
        return this.getArray(this.spawnItems);
    }

    public ItemStack[] getQueueItemStack() {
        return this.getArray(this.queueItems);
    }

    public ItemStack[] getPartyItemStack() {
        return this.getArray(this.partyItems);
    }

    public ItemStack[] getEventItemStack() {
        return this.getArray(this.eventItems);
    }

    public ItemStack[] getPartySpecItemStack() {
        return this.getArray(this.partySpecItems);
    }

    public ItemStack[] getTournamentItemStack() {
        return this.getArray(this.tournamentItems);
    }
}
