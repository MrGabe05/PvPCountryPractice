package com.gabrielhd.practice.utils;

import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.inventory.ItemStack;

public class Glow extends Enchantment
{
    public Glow(final int n) {
        super(n);
    }
    
    @Override
    public String getName() {
        return "Glowing";
    }
    
    @Override
    public int getMaxLevel() {
        return 10;
    }
    
    @Override
    public int getStartLevel() {
        return 1;
    }
    
    @Override
    public EnchantmentTarget getItemTarget() {
        return null;
    }
    
    public boolean isTreasure() {
        return false;
    }
    
    public boolean isCursed() {
        return false;
    }
    
    @Override
    public boolean conflictsWith(final Enchantment enchantment) {
        return false;
    }
    
    @Override
    public boolean canEnchantItem(final ItemStack itemStack) {
        return false;
    }
}
