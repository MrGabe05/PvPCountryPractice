package com.gabrielhd.practice.utils;

import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.potion.Potion;
import org.bukkit.potion.PotionType;

import java.util.*;

@Getter
public class BuilderItem {

    private Material type;
    private static BuilderItem instance;
    private int amount;
    private short data;
    private String title;
    private List<String> lore;
    private final Map<Enchantment, Integer> enchants;
    private Color color;
    private PotionType potion;
    private boolean potionUpgraded;
    private boolean potionExtended;
    private boolean potionSplash;
    private boolean hideFlags;
    private boolean glow;
    private String skull;
    
    public static BuilderItem getInstance() {
        return instance;
    }
    
    public BuilderItem(Material material) {
        this(material, 1);
    }
    
    public BuilderItem(Material material, int n) {
        this(material, n, (short)0);
    }
    
    public BuilderItem(Material material, short n) {
        this(material, 1, n);
    }
    
    public BuilderItem(Material mat, int amount, short data) {
        this.title = null;
        this.lore = new ArrayList<>();
        this.enchants = new HashMap<>();
        this.type = mat;
        if (this.type == null) {
            this.type = Material.BEDROCK;
        }
        this.amount = amount;
        this.data = data;
        this.hideFlags = false;
    }
    
    public BuilderItem(ItemStack itemStack) {
        this.title = null;
        this.lore = new ArrayList<>();
        this.enchants = new HashMap<>();
        this.type = itemStack.getType();
        this.amount = itemStack.getAmount();
        this.data = itemStack.getDurability();
        ItemMeta itemMeta = itemStack.getItemMeta();
        this.title = itemMeta.getDisplayName();
        this.lore = itemMeta.getLore();
        if (itemMeta instanceof LeatherArmorMeta) {
            this.color = ((LeatherArmorMeta)itemMeta).getColor();
        }
        if (itemMeta instanceof PotionMeta) {
                Potion fromItemStack = Potion.fromItemStack(itemStack);
                this.potion = fromItemStack.getType();
                this.potionUpgraded = (fromItemStack.getLevel() > 1);
                this.potionSplash = fromItemStack.isSplash();
                this.potionExtended = fromItemStack.hasExtendedDuration();
        }
        this.enchants.putAll(itemStack.getEnchantments());
    }
    
    public BuilderItem setType(Material mat) {
        this.type = mat;
        return this;
    }
    
    public BuilderItem setData(short data) {
        this.data = data;
        return this;
    }
    
    public BuilderItem setTitle(String s) {
        this.title = ChatColor.translateAlternateColorCodes('&', s);
        return this;
    }
    
    public BuilderItem addLore(String s) {
        this.lore.add(ChatColor.translateAlternateColorCodes('&', s));
        return this;
    }
    
    public BuilderItem addLore(List<String> list) {
        for (String s : list) {
            this.lore.add(ChatColor.translateAlternateColorCodes('&', s));
        }
        return this;
    }
    
    public BuilderItem removeLastLoreLine() {
        this.lore.remove(this.lore.size() - 1);
        return this;
    }
    
    public BuilderItem setLore(List<String> list) {
        this.lore.clear();
        for (String s : list) {
            this.lore.add(ChatColor.translateAlternateColorCodes('&', s));
        }
        return this;
    }
    
    public BuilderItem addEnchantment(Enchantment enchantment, int n) {
        this.enchants.remove(enchantment);
        this.enchants.put(enchantment, n);
        return this;
    }
    
    public BuilderItem setColor(Color color) {
        if (this.type.name().contains("LEATHER_")) {
            this.color = color;
        }
        return this;
    }
    
    public BuilderItem setHideFlags(boolean hideFlags) {
        this.hideFlags = hideFlags;
        return this;
    }
    
    public boolean isHideFlags() {
        return this.hideFlags;
    }
    
    public boolean isGlow() {
        return this.glow;
    }
    
    public void setGlow(boolean glow) {
        this.glow = glow;
    }
    
    public BuilderItem setPotion(String s, Material mat, boolean potionUpgraded, boolean potionExtended) {
        this.type = mat;
        try {
            if (mat == Material.POTION) {
                this.potionSplash = true;
            }
        }
        catch (NoSuchFieldError noSuchFieldError) {
            this.type = Material.POTION;
            this.potionSplash = true;
        }
        this.potion = PotionType.valueOf(s);
        this.potionUpgraded = potionUpgraded;
        this.potionExtended = potionExtended;
        return this;
    }
    
    public BuilderItem setAmount(int amount) {
        this.amount = amount;
        return this;
    }
    
    public BuilderItem setSkullOwner(String skull) {
        if (this.type != Material.SKULL_ITEM) {
            this.type = Material.SKULL_ITEM;
            this.data = 3;
        }
        this.skull = skull;
        return this;
    }
    
    public ItemStack build() {
        if (this.type == null) {
            this.type = Material.AIR;
        }
        ItemStack itemStack = new ItemStack(this.type, this.amount, this.data);
        ItemMeta itemMeta = itemStack.getItemMeta();
        if (itemMeta instanceof LeatherArmorMeta && this.color != null) {
            ((LeatherArmorMeta)itemMeta).setColor(this.color);
        }
        if (itemMeta instanceof SkullMeta && this.skull != null) {
            ((SkullMeta)itemMeta).setOwner(this.skull);
        }
        if (itemMeta instanceof PotionMeta && this.potion != null) {
            new Potion(this.potion, this.potionUpgraded ? 2 : 1, this.potionSplash, this.potionExtended).apply(itemStack);
        }
        if (this.title != null) {
            itemMeta.setDisplayName(this.title);
        }
        if (!this.lore.isEmpty()) {
            itemMeta.setLore(this.lore);
        }
        if (this.hideFlags) {
            itemMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_POTION_EFFECTS);
        }
        if (this.glow) {
            itemMeta.addEnchant(new Glow(120), 0, true);
        }
        itemStack.setItemMeta(itemMeta);
        itemStack.addUnsafeEnchantments(this.enchants);
        return itemStack;
    }
    
    @Override
    public BuilderItem clone() {
        BuilderItem itemBuilder = new BuilderItem(this.type, this.amount, this.data);
        itemBuilder.setTitle(this.title);
        itemBuilder.setLore(this.lore);

        for (Map.Entry<Enchantment, Integer> entry : this.enchants.entrySet()) {
            itemBuilder.addEnchantment(entry.getKey(), entry.getValue());
        }

        itemBuilder.setColor(this.color);
        itemBuilder.potion = this.potion;
        itemBuilder.potionExtended = this.potionExtended;
        itemBuilder.potionUpgraded = this.potionUpgraded;
        itemBuilder.potionSplash = this.potionSplash;

        return itemBuilder;
    }
    
    public boolean hasEnchantment(Enchantment enchantment) {
        return this.enchants.containsKey(enchantment);
    }
    
    public int getEnchantmentLevel(Enchantment enchantment) {
        return this.enchants.get(enchantment);
    }
    
    public Map<Enchantment, Integer> getAllEnchantments() {
        return this.enchants;
    }
    
    public boolean isItem(ItemStack itemStack) {
        if (itemStack == null) {
            return false;
        }
        ItemMeta itemMeta = itemStack.getItemMeta();
        if (itemStack.getType() != this.getType()) {
            return false;
        }
        if (!itemMeta.hasDisplayName() && this.getTitle() != null) {
            return false;
        }
        if (!itemMeta.getDisplayName().equals(this.getTitle())) {
            return false;
        }
        if (!itemMeta.hasLore() && !this.getLore().isEmpty()) {
            return false;
        }
        if (itemMeta.hasLore()) {
            for (String s : itemMeta.getLore()) {
                if (!this.getLore().contains(s)) {
                    return false;
                }
            }
        }
        for (Enchantment enchantment : itemStack.getEnchantments().keySet()) {
            if (!this.hasEnchantment(enchantment)) {
                return false;
            }
        }
        return true;
    }
    
    @Override
    public String toString() {
        StringBuilder s = new StringBuilder("" + this.type.toString());
        if (this.data != 0) {
            s.append(":").append(this.data);
        }
        if (this.amount > 1) {
            s.append(",").append(this.amount);
        }
        if (this.title != null) {
            s.append(",name:").append(this.title);
        }
        if (!this.lore.isEmpty()) {
            for (String value : this.lore) {
                s.append(",lore:").append(value);
            }
        }
        for (Map.Entry<Enchantment, Integer> entry : this.getAllEnchantments().entrySet()) {
            s.append(",").append(entry.getKey().getName()).append((entry.getValue() > 1) ? (":" + entry.getValue()) : "");
        }
        if (this.color != null) {
            s.append(",leather_color:").append(this.color.getRed()).append("-").append(this.color.getGreen()).append("-").append(this.color.getBlue());
        }
        if (this.potion != null) {
            s.append(",potion:").append(this.potion).append(":").append(this.potionUpgraded).append(":").append(this.potionExtended);
        }
        if (this.glow) {
            s.append(",glowing");
        }
        return s.toString();
    }
}
