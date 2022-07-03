package com.gabrielhd.practice.utils.others;

import com.gabrielhd.practice.Practice;
import org.bukkit.*;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scheduler.BukkitRunnable;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class PlayerUtil {

    public static void setFirstSlotOfType(Player player, Material type, ItemStack itemStack) {
        for (int i = 0; i < player.getInventory().getContents().length; ++i) {
            ItemStack itemStack2 = player.getInventory().getContents()[i];
            if (itemStack2 == null || itemStack2.getType() == type || itemStack2.getType() == Material.AIR) {
                player.getInventory().setItem(i, itemStack);
                break;
            }
        }
    }
    
    public static int getPing(Player player) {
        return ((CraftPlayer)player).getHandle().ping;
    }
    
    public static void clearPlayer(Player player) {
        player.setHealth(20.0);
        player.setFoodLevel(20);
        player.setSaturation(12.8f);
        player.setFireTicks(0);
        player.setFallDistance(0.0f);
        player.setLevel(0);
        player.setExp(0.0f);
        player.setWalkSpeed(0.2f);
        player.getInventory().setHeldItemSlot(0);
        player.setAllowFlight(false);
        player.setFlying(false);
        player.getInventory().clear();
        player.getInventory().setArmorContents(null);
        player.closeInventory();
        player.setGameMode(GameMode.ADVENTURE);
        player.getActivePotionEffects().stream().map(PotionEffect::getType).forEach(player::removePotionEffect);
        ((CraftPlayer)player).getHandle().getDataWatcher().watch(9, (byte)0);
        player.updateInventory();
    }
    
    public static void sendFirework(FireworkEffect effect, Location location) {
        Firework f = location.getWorld().spawn(location, Firework.class);
        FireworkMeta fm = f.getFireworkMeta();
        fm.addEffect(effect);
        f.setFireworkMeta(fm);
        try {
            Class<?> entityFireworkClass = getClass("net.minecraft.server.", "EntityFireworks");
            Class<?> craftFireworkClass = getClass("org.bukkit.craftbukkit.", "entity.CraftFirework");
            Object firework = craftFireworkClass.cast(f);
            Method handle = firework.getClass().getMethod("getHandle");
            Object entityFirework = handle.invoke(firework);
            Field expectedLifespan = entityFireworkClass.getDeclaredField("expectedLifespan");
            Field ticksFlown = entityFireworkClass.getDeclaredField("ticksFlown");
            ticksFlown.setAccessible(true);
            ticksFlown.setInt(entityFirework, expectedLifespan.getInt(entityFirework) - 1);
            ticksFlown.setAccessible(false);
        }
        catch (ClassNotFoundException | IllegalAccessException | IllegalArgumentException | NoSuchFieldException | NoSuchMethodException | SecurityException | InvocationTargetException ignored) {
        }
    }
    
    public static void respawnPlayer(PlayerDeathEvent event) {
        new BukkitRunnable() {
            @Override
            public void run() {
                try {
                    Object nmsPlayer = event.getEntity().getClass().getMethod("getHandle", new Class[0]).invoke(event.getEntity());
                    Object con = nmsPlayer.getClass().getDeclaredField("playerConnection").get(nmsPlayer);
                    Class<?> EntityPlayer = Class.forName(nmsPlayer.getClass().getPackage().getName() + ".EntityPlayer");
                    Field minecraftServer = con.getClass().getDeclaredField("minecraftServer");
                    minecraftServer.setAccessible(true);
                    Object mcserver = minecraftServer.get(con);
                    Object playerlist = mcserver.getClass().getDeclaredMethod("getPlayerList", new Class[0]).invoke(mcserver);
                    Method moveToWorld = playerlist.getClass().getMethod("moveToWorld", EntityPlayer, Integer.TYPE, Boolean.TYPE);
                    moveToWorld.invoke(playerlist, nmsPlayer, 0, false);
                }
                catch (ClassNotFoundException | IllegalAccessException | IllegalArgumentException | NoSuchFieldException | NoSuchMethodException | SecurityException | InvocationTargetException ignored) {
                }
            }
        }.runTaskLater(Practice.getInstance(), 2L);
    }
    
    private static Class<?> getClass(String prefix, String nmsClassString) throws ClassNotFoundException {
        String version = Bukkit.getServer().getClass().getPackage().getName().replace(".", ",").split(",")[3] + ".";
        String name = prefix + version + nmsClassString;
        return Class.forName(name);
    }
}
