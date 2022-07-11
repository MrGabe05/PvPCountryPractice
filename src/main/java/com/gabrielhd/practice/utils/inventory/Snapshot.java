package com.gabrielhd.practice.utils.inventory;

import com.gabrielhd.practice.Practice;
import com.gabrielhd.practice.match.Match;
import com.gabrielhd.practice.player.PlayerData;
import com.gabrielhd.practice.utils.items.ItemUtil;
import com.gabrielhd.practice.utils.others.MathUtil;
import com.gabrielhd.practice.utils.others.StringUtil;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.json.simple.JSONObject;

import java.util.*;
import java.util.function.Function;

public class Snapshot {

    private final InventoryUI inventoryUI;
    private final ItemStack[] originalInventory;
    private final ItemStack[] originalArmor;
    private final UUID snapshotId;
    
    public Snapshot(Player player, Match match) {
        this.snapshotId = UUID.randomUUID();

        ItemStack[] contents = player.getInventory().getContents();
        ItemStack[] armor = player.getInventory().getArmorContents();

        this.originalInventory = contents;
        this.originalArmor = armor;

        PlayerData playerData = PlayerData.of(player);
        double health = player.getHealth();
        double food = player.getFoodLevel();

        List<String> potionEffectStrings = new ArrayList<>();
        for (PotionEffect potionEffect : player.getActivePotionEffects()) {
            String romanNumeral = MathUtil.convertToRomanNumeral(potionEffect.getAmplifier() + 1);
            String effectName = StringUtil.toNiceString(potionEffect.getType().getName().toLowerCase());
            String duration = MathUtil.convertTicksToMinutes(potionEffect.getDuration());

            potionEffectStrings.add(String.valueOf(ChatColor.YELLOW.toString()) + ChatColor.BOLD + "* " + ChatColor.WHITE + effectName + " " + romanNumeral + ChatColor.GRAY + " (" + duration + ")");
        }

        this.inventoryUI = new InventoryUI(player.getName() + " Inventory", true, 6);
        for (int i = 0; i < 9; ++i) {
            this.inventoryUI.setItem(i + 27, new InventoryUI.EmptyClickableItem(contents[i]));
            this.inventoryUI.setItem(i + 18, new InventoryUI.EmptyClickableItem(contents[i + 27]));
            this.inventoryUI.setItem(i + 9, new InventoryUI.EmptyClickableItem(contents[i + 18]));
            this.inventoryUI.setItem(i, new InventoryUI.EmptyClickableItem(contents[i + 9]));
        }
        boolean potionMatch = false;
        boolean soupMatch = false;
        ItemStack[] contents2;
        for (int length = (contents2 = match.getKit().getContents()).length, k = 0; k < length; ++k) {
            ItemStack item = contents2[k];
            if (item != null) {
                if (item.getType() == Material.MUSHROOM_SOUP) {
                    soupMatch = true;
                    break;
                }
                if (item.getType() == Material.POTION && item.getDurability() == 16421) {
                    potionMatch = true;
                    break;
                }
            }
        }
        if (potionMatch) {
            int potCount = (int)Arrays.stream(contents).filter(Objects::nonNull).map(ItemStack::getDurability).filter(d -> d == 16421).count();
            this.inventoryUI.setItem(45, new InventoryUI.EmptyClickableItem(ItemUtil.reloreItem(ItemUtil.createItem(Material.POTION, String.valueOf(ChatColor.YELLOW.toString()) + ChatColor.BOLD + "Potions:"), String.valueOf(ChatColor.GOLD.toString()) + ChatColor.BOLD + "* " + ChatColor.WHITE + "Health Pots: " + ChatColor.GRAY + potCount + " Potion" + ((potCount > 1) ? "s" : ""), String.valueOf(ChatColor.GOLD.toString()) + ChatColor.BOLD + "* " + ChatColor.WHITE + "Missed Pots: " + ChatColor.GRAY + playerData.getMissedPots() + " Potion" + ((playerData.getMissedPots() > 1) ? "s" : ""))));
        }
        else if (soupMatch) {
            int soupCount = (int)Arrays.stream(contents).filter(Objects::nonNull).<Object>map((Function<? super ItemStack, ?>)ItemStack::getType).filter(d -> d == Material.MUSHROOM_SOUP).count();
            this.inventoryUI.setItem(45, new InventoryUI.EmptyClickableItem(ItemUtil.createItem(Material.MUSHROOM_SOUP, String.valueOf(ChatColor.YELLOW.toString()) + ChatColor.BOLD + "Soups Left: " + ChatColor.WHITE + soupCount, soupCount, (short)16421)));
        }
        double roundedHealth = Math.round(health / 2.0 * 2.0) / 2.0;
        this.inventoryUI.setItem(49, new InventoryUI.EmptyClickableItem(ItemUtil.createItem(Material.SKULL_ITEM, String.valueOf(ChatColor.YELLOW.toString()) + ChatColor.BOLD + roundedHealth + "HP", (int)Math.round(health / 2.0))));
        double roundedFood = Math.round(health / 2.0 * 2.0) / 2.0;
        this.inventoryUI.setItem(48, new InventoryUI.EmptyClickableItem(ItemUtil.createItem(Material.COOKED_BEEF, String.valueOf(ChatColor.YELLOW.toString()) + ChatColor.BOLD + roundedFood + " Hunger", (int)Math.round(food / 2.0))));
        this.inventoryUI.setItem(47, new InventoryUI.EmptyClickableItem(ItemUtil.reloreItem(ItemUtil.createItem(Material.BREWING_STAND_ITEM, String.valueOf(ChatColor.YELLOW.toString()) + ChatColor.BOLD + "Potion Effects", potionEffectStrings.size()), potionEffectStrings.toArray(new String[0]))));
        this.inventoryUI.setItem(46, new InventoryUI.EmptyClickableItem(ItemUtil.reloreItem(ItemUtil.createItem(Material.CAKE, String.valueOf(ChatColor.YELLOW.toString()) + ChatColor.BOLD + "Stats"), String.valueOf(ChatColor.GOLD.toString()) + ChatColor.BOLD + "* " + ChatColor.WHITE + "Longest Combo: " + ChatColor.GRAY + playerData.getLongestCombo() + " Hit" + ((playerData.getLongestCombo() > 1) ? "s" : ""), String.valueOf(ChatColor.GOLD.toString()) + ChatColor.BOLD + "* " + ChatColor.WHITE + "Total Hits: " + ChatColor.GRAY + playerData.getHits() + " Hit" + ((playerData.getHits() > 1) ? "s" : ""))));

        if (!match.isParty()) {
            this.inventoryUI.setItem(53, new InventoryUI.AbstractClickableItem(ItemUtil.reloreItem(ItemUtil.createItem(Material.EMPTY_MAP, ChatColor.YELLOW + "View Other Inventory"))) {
                @Override
                public void onClick(InventoryClickEvent inventoryClickEvent) {
                    Player clicker = (Player)inventoryClickEvent.getWhoClicked();
                    if (Practice.getInstance().getMatchManager().isRematching(player.getUniqueId())) {
                        clicker.closeInventory();
                        Practice.getInstance().getServer().dispatchCommand(clicker, "inventory " + Practice.getInstance().getMatchManager().getRematcherInventory(player.getUniqueId()));
                    }
                }
            });
        }
        for (int j = 36; j < 40; ++j) {
            this.inventoryUI.setItem(j, new InventoryUI.EmptyClickableItem(armor[39 - j]));
        }
    }
    
    public JSONObject toJson() {
        JSONObject object = new JSONObject();
        JSONObject inventoryObject = new JSONObject();
        for (int i = 0; i < this.originalInventory.length; ++i) {
            inventoryObject.put(i, this.encodeItem(this.originalInventory[i]));
        }
        object.put("inventory", inventoryObject);
        JSONObject armourObject = new JSONObject();
        for (int j = 0; j < this.originalArmor.length; ++j) {
            armourObject.put(j, this.encodeItem(this.originalArmor[j]));
        }
        object.put("armour", armourObject);
        return object;
    }
    
    private JSONObject encodeItem(ItemStack itemStack) {
        if (itemStack == null || itemStack.getType() == Material.AIR) {
            return null;
        }
        JSONObject object = new JSONObject();
        object.put("material", itemStack.getType().name());
        object.put("durability", itemStack.getDurability());
        object.put("amount", itemStack.getAmount());
        JSONObject enchants = new JSONObject();
        for (Enchantment enchantment : itemStack.getEnchantments().keySet()) {
            enchants.put(enchantment.getName(), itemStack.getEnchantments().get(enchantment));
        }
        object.put("enchants", enchants);
        return object;
    }
    
    public UUID getSnapshotId() {
        return this.snapshotId;
    }
    
    public InventoryUI getInventoryUI() {
        return this.inventoryUI;
    }
    
    public ItemStack[] getOriginalInventory() {
        return this.originalInventory;
    }
    
    public ItemStack[] getOriginalArmor() {
        return this.originalArmor;
    }
}
