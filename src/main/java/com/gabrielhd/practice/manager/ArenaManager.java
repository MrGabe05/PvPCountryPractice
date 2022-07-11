package com.gabrielhd.practice.manager;

import com.gabrielhd.practice.Practice;
import com.gabrielhd.practice.arena.Arena;
import com.gabrielhd.practice.arena.StandArena;
import com.gabrielhd.practice.config.YamlConfig;
import com.gabrielhd.practice.kit.Kit;
import com.gabrielhd.practice.utils.inventory.InventoryUI;
import com.gabrielhd.practice.utils.items.ItemUtil;
import com.gabrielhd.practice.utils.others.LocUtils;
import lombok.Getter;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

@Getter
public class ArenaManager {

    private final Map<String, Arena> arenas;
    private final Map<StandArena, UUID> arenaMatchUUIDs;

    private int generatingArenaRunnables;

    public ArenaManager() {
        this.arenas = new HashMap<>();
        this.arenaMatchUUIDs = new HashMap<>();

        this.loadArenas();
    }

    private void loadArenas() {
        File folderArenas = new File(Practice.getInstance().getDataFolder(), "arenas/");
        if(!folderArenas.exists()) folderArenas.mkdir();

        Arrays.stream(folderArenas.listFiles()).filter(File::isFile).filter(file -> file.getPath().endsWith(".yml")).forEach(file -> {
            YamlConfig configArena = new YamlConfig(Practice.getInstance(), file);

            String name = configArena.getString("name");
            boolean enabled = configArena.getBoolean("enabled");
            Location a = LocUtils.StringToLocation(configArena.getString("a"));
            Location b = LocUtils.StringToLocation(configArena.getString("b"));
            Location max = LocUtils.StringToLocation(configArena.getString("max"));
            Location min = LocUtils.StringToLocation(configArena.getString("min"));

            List<StandArena> standArenas = new ArrayList<>();
            if(configArena.isSet("arenas")) {
                configArena.getConfigurationSection("arenas").getKeys(false).forEach(id -> {
                    Location standA = LocUtils.StringToLocation(configArena.getString("arenas." + id + ".a"));
                    Location standB = LocUtils.StringToLocation(configArena.getString("arenas." + id + ".b"));
                    Location standMax = LocUtils.StringToLocation(configArena.getString("arenas." + id + ".max"));
                    Location standMin = LocUtils.StringToLocation(configArena.getString("arenas." + id + ".min"));

                    standArenas.add(new StandArena(standA, standB, standMax, standMin));
                });
            }

            this.arenas.put(name.toLowerCase(Locale.ROOT), new Arena(name, standArenas, new ArrayList<>(standArenas), a, b, max, min, enabled));
        });
    }

    public void saveArenas() {
        this.arenas.values().forEach(arena -> {
            YamlConfig configArena = new YamlConfig(Practice.getInstance(), "arenas/" + arena.getName() + ".yml");

            configArena.set("name", arena.getName());
            configArena.set("enabled", arena.isEnabled());
            configArena.set("a", LocUtils.LocationToString(arena.getA()));
            configArena.set("b", LocUtils.LocationToString(arena.getB()));
            configArena.set("max", LocUtils.LocationToString(arena.getMin()));
            configArena.set("min", LocUtils.LocationToString(arena.getMax()));

            if(arena.getStandArenas() != null && !arena.getStandArenas().isEmpty()) {
                int i = 0;

                for(StandArena standArena : arena.getStandArenas()) {
                    configArena.set("arenas." + i + ".a", LocUtils.LocationToString(standArena.getA()));
                    configArena.set("arenas." + i + ".b", LocUtils.LocationToString(standArena.getB()));
                    configArena.set("arenas." + i + ".max", LocUtils.LocationToString(standArena.getMax()));
                    configArena.set("arenas." + i + ".min", LocUtils.LocationToString(standArena.getMin()));

                    i++;
                }
            }

            configArena.save();
        });
    }

    public void openArenaSystemUI(Player player) {
        if (this.arenas.isEmpty()) {
            player.sendMessage(ChatColor.RED + "No hay arenas.");
            return;
        }

        InventoryUI inventory = new InventoryUI("Sistema de Arenas", true, 6);
        for (Arena arena : this.arenas.values()) {
            ItemStack item = ItemUtil.createItem(Material.PAPER, ChatColor.YELLOW + arena.getName() + ChatColor.GRAY + " (" + (arena.isEnabled() ? (String.valueOf(ChatColor.GREEN.toString()) + ChatColor.BOLD + "HABILITADA") : (String.valueOf(ChatColor.RED.toString()) + ChatColor.BOLD + "DESHABILITADA")) + ChatColor.GRAY + ")");
            ItemUtil.reloreItem(item, ChatColor.GRAY + "Arenas: " + ChatColor.GREEN + ((arena.getStandArenas().isEmpty()) ? "Single Arena (Invisible Players)" : (arena.getStandArenas().size() + " Arenas")), ChatColor.GRAY + "Arenas independientes: " + ChatColor.GREEN + ((arena.getAvailableArenas().isEmpty()) ? "Ninguna" : (arena.getAvailableArenas().size() + " Arenas disponibles")), "", String.valueOf(ChatColor.YELLOW.toString()) + ChatColor.BOLD + "CLICK IZQUIERDO " + ChatColor.GRAY + "Teletransporte a Arena", String.valueOf(ChatColor.YELLOW.toString()) + ChatColor.BOLD + "CLICK DERECHO " + ChatColor.GRAY + "Generar arenas independientes");
            inventory.addItem(new InventoryUI.AbstractClickableItem(item) {
                @Override
                public void onClick(InventoryClickEvent event) {
                    Player player = (Player)event.getWhoClicked();
                    if (event.getClick() == ClickType.LEFT) {
                        player.teleport(arena.getA());
                    }
                    else {
                        InventoryUI generateInventory = new InventoryUI("Generar Arenas", true, 1);
                        int[] batches = { 10, 20, 30, 40, 50, 60, 70, 80, 90, 100, 110, 120, 130, 140, 150 };
                        int[] array;
                        for (int length = (array = batches).length, i = 0; i < length; ++i) {
                            int batch = array[i];
                            ItemStack item = ItemUtil.createItem(Material.PAPER, String.valueOf(ChatColor.RED.toString()) + ChatColor.BOLD + batch + " ARENAS");
                            generateInventory.addItem(new InventoryUI.AbstractClickableItem(item) {
                                @Override
                                public void onClick(InventoryClickEvent event) {
                                    Player player = (Player)event.getWhoClicked();
                                    player.performCommand("arena generate " + arena.getName() + " " + batch);
                                    player.sendMessage(ChatColor.GREEN + "Generando " + batch + " arenas, compruebe la consola para ver el progreso.");
                                    player.closeInventory();
                                }
                            });
                        }
                        player.openInventory(generateInventory.getCurrentPage());
                    }
                }
            });
        }
        player.openInventory(inventory.getCurrentPage());
    }

    public void reloadArenas() {
        this.saveArenas();
        this.arenas.clear();
        this.loadArenas();
    }

    public void createArena(String name) {
        this.arenas.put(name, new Arena(name));
    }

    public void deleteArena(String name) {
        this.arenas.remove(name);
    }

    public Arena getArena(String name) {
        return this.arenas.get(name);
    }

    public Arena getRandomArena(Kit kit) {
        List<Arena> enabledArenas = new ArrayList<>();
        for (Arena arena : this.arenas.values()) {
            if (!arena.isEnabled() || kit.getBlacklistArenas().contains(arena.getName()) || (kit.getWhitelistArenas().size() > 0 && !kit.getWhitelistArenas().contains(arena.getName()))) continue;

            enabledArenas.add(arena);
        }

        if (enabledArenas.isEmpty()) return null;

        return enabledArenas.get(ThreadLocalRandom.current().nextInt(enabledArenas.size()));
    }

    public void removeArenaMatchUUID(StandArena arena) {
        this.arenaMatchUUIDs.remove(arena);
    }

    public UUID getArenaMatchUUID(StandArena arena) {
        return this.arenaMatchUUIDs.get(arena);
    }

    public void setArenaMatchUUID(StandArena arena, UUID matchUUID) {
        this.arenaMatchUUIDs.put(arena, matchUUID);
    }

    public int getGeneratingArenaRunnables() {
        return this.generatingArenaRunnables;
    }

    public void setGeneratingArenaRunnables(int generatingArenaRunnables) {
        this.generatingArenaRunnables = generatingArenaRunnables;
    }
}
