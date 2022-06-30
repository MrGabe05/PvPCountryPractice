package com.gabrielhd.practice.manager;

import com.gabrielhd.practice.Practice;
import com.gabrielhd.practice.arena.Arena;
import com.gabrielhd.practice.arena.StandArena;
import com.gabrielhd.practice.config.YamlConfig;
import com.gabrielhd.practice.kit.Kit;
import com.gabrielhd.practice.utils.LocUtils;
import lombok.Getter;
import org.bukkit.Location;

import java.io.File;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

@Getter
public class ArenaManager {

    private final Map<String, Arena> arenas;

    public ArenaManager() {
        this.arenas = new HashMap<>();

        this.loadArenas();
    }

    private void loadArenas() {
        File folderArenas = new File(Practice.getInstance().getDataFolder(), "arenas/");

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
            YamlConfig configArena = new YamlConfig(Practice.getInstance(), arena.getName() + ".yml");
        });
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
            if (!arena.isEnabled() || kit.getExcludedArenas().contains(arena.getName()) || (kit.getArenas().size() > 0 && !kit.getArenas().contains(arena.getName()))) continue;

            enabledArenas.add(arena);
        }

        if (enabledArenas.isEmpty()) return null;

        return enabledArenas.get(ThreadLocalRandom.current().nextInt(enabledArenas.size()));
    }
}
