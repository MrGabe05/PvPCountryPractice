package com.gabrielhd.practice.utils.items;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public final class ItemUtil
{
    private ItemUtil() {
        throw new RuntimeException("Cannot instantiate a utility class.");
    }
    
    public static String formatMaterial(final Material material) {
        String name = material.toString();
        name = name.replace('_', ' ');
        StringBuilder result = new StringBuilder(String.valueOf(name.charAt(0)));
        for (int i = 1; i < name.length(); ++i) {
            if (name.charAt(i - 1) == ' ') {
                result.append(name.charAt(i));
            }
            else {
                result.append(Character.toLowerCase(name.charAt(i)));
            }
        }
        return result.toString();
    }

    public static ItemStack createItem(final Material m, final int amount, final short durability, final String name) {
        ItemStack itemStack = new ItemStack(m, amount, durability);
        if (name != null) {
            itemStack = renameItem(itemStack, name);
        }
        return itemStack;
    }
    
    public static ItemStack enchantItem(final ItemStack itemStack, final ItemEnchant... enchantments) {
        Arrays.asList(enchantments).forEach(enchantment -> itemStack.addUnsafeEnchantment(enchantment.enchantment, enchantment.level));
        return itemStack;
    }
    
    public static ItemStack createItem(final Material material, final String name) {
        final ItemStack item = new ItemStack(material);
        final ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        item.setItemMeta(meta);
        return item;
    }
    
    public static ItemStack createItem(final Material material, final String name, final int amount) {
        final ItemStack item = new ItemStack(material, amount);
        final ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        item.setItemMeta(meta);
        return item;
    }
    
    public static ItemStack createItem(final Material material, final String name, final int amount, final short damage) {
        final ItemStack item = new ItemStack(material, amount, damage);
        final ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        item.setItemMeta(meta);
        return item;
    }
    
    public static ItemStack hideEnchants(final ItemStack item) {
        final ItemMeta meta = item.getItemMeta();
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_UNBREAKABLE);
        item.setItemMeta(meta);
        return item;
    }
    
    public static ItemStack setUnbreakable(final ItemStack item) {
        final ItemMeta meta = item.getItemMeta();
        meta.spigot().setUnbreakable(true);
        meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
        item.setItemMeta(meta);
        return item;
    }
    
    public static ItemStack renameItem(final ItemStack item, final String name) {
        final ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        item.setItemMeta(meta);
        return item;
    }
    
    public static ItemStack reloreItem(final ItemStack item, final String... lores) {
        return reloreItem(ReloreType.OVERWRITE, item, lores);
    }
    
    public static ItemStack reloreItem(final ReloreType type, final ItemStack item, final String... lores) {
        final ItemMeta meta = item.getItemMeta();
        List<String> lore = meta.getLore();
        if (lore == null) {
            lore = new LinkedList<>();
        }
        switch (type) {
            case APPEND: {
                lore.addAll(Arrays.asList(lores));
                meta.setLore(lore);
                break;
            }
            case PREPEND: {
                final List<String> nLore = new LinkedList<>(Arrays.asList(lores));
                nLore.addAll(lore);
                meta.setLore(nLore);
                break;
            }
            case OVERWRITE: {
                meta.setLore(Arrays.asList(lores));
                break;
            }
        }
        item.setItemMeta(meta);
        return item;
    }
    
    public static ItemStack addItemFlag(final ItemStack item, final ItemFlag flag) {
        final ItemMeta meta = item.getItemMeta();
        meta.addItemFlags(flag);
        item.setItemMeta(meta);
        return item;
    }

    public static String contentsToString(ItemStack[] contents) {
        StringBuilder builder = new StringBuilder();

        for(ItemStack item : contents) {
            builder.append("/item:");

            if(item == null || item.getType() == Material.AIR || item.getAmount() < 1) {
                builder.append("AIR");
                continue;
            }

            ItemMeta itemMeta = item.getItemMeta();

            builder.append("/:/").append(item.getData().getItemType().name());
            builder.append("/:/").append(item.getAmount());
            builder.append("/:/").append(item.getData().getData());
            builder.append("/:/").append(itemMeta.getDisplayName());

            builder.append("/lores:");
            for(String lore : itemMeta.getLore()) {
                builder.append("/:/").append(lore);
            }
            builder.append(":lores/");

            builder.append("/enchants:");
            for(Map.Entry<Enchantment, Integer> enchants : itemMeta.getEnchants().entrySet()) {
                builder.append(enchants.getKey().getName()).append("/;/").append(enchants.getValue());
            }
            builder.append(":enchants/");

            builder.append(":item/");
        }

        return builder.toString();
    }

    public static ItemStack[] stringToContents(String contents) {
        ItemStack[] itemStacks = new ItemStack[0];
        String[] itemString = contents.split("/item:");

        for(int i = 0; i < itemString.length; i++) {
            String[] itemData = itemString[i].split("/:/");

            if(itemData[0].equalsIgnoreCase("AIR")) {
                itemStacks[i] = new ItemStack(Material.AIR);
                continue;
            }

            ItemStack item = createItem(Material.getMaterial(itemData[0]), Integer.parseInt(itemData[1]), Byte.parseByte(itemData[2]), itemData[3]);

            int l;
            for(l = 4; l < itemData.length; l++) {
                String loreData = itemData[l];

                if(loreData.equalsIgnoreCase(":lores/")) break;
                if(loreData.equalsIgnoreCase("/lores:")) continue;

                item = reloreItem(ReloreType.APPEND, item, loreData);
            }

            for(int e = l; e < itemData.length; e++) {
                String enchant = itemData[e];

                if(enchant.equalsIgnoreCase(":enchants/")) break;
                if(enchant.equalsIgnoreCase("/enchants:")) continue;

                String[] split = enchant.split("/;/");

                item = enchantItem(item, new ItemEnchant(Enchantment.getByName(split[0]), Integer.parseInt(split[1])));
            }
        }

        return itemStacks;
    }
    
    public enum ReloreType {
        OVERWRITE("OVERWRITE", 0), 
        PREPEND("PREPEND", 1), 
        APPEND("APPEND", 2);
        
        ReloreType(final String s, final int n) {
        }
    }
    
    public static class ItemEnchant
    {
        private final Enchantment enchantment;
        private final int level;
        
        public ItemEnchant(final Enchantment enchantment, final int level) {
            this.enchantment = enchantment;
            this.level = level;
        }
    }
}
