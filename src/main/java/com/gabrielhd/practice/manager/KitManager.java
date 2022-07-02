package com.gabrielhd.practice.manager;

import com.gabrielhd.practice.Practice;
import com.gabrielhd.practice.config.YamlConfig;
import com.gabrielhd.practice.kit.Kit;
import lombok.Getter;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.util.*;

public class KitManager {

    private final Map<String, Kit> kits;
    @Getter private final Set<String> rankedKits;

    public KitManager() {
        this.kits = new HashMap<>();
        this.rankedKits = new HashSet<>();

        this.loadKits();
    }

    private void loadKits() {
        File folderKits = new File(Practice.getInstance().getDataFolder(), "kits/");
        if(!folderKits.exists()) folderKits.mkdir();

        Arrays.stream(folderKits.listFiles()).filter(File::isFile).filter(file -> file.getPath().endsWith(".yml")).forEach(file -> {
            YamlConfig configKit = new YamlConfig(Practice.getInstance(), file);

            boolean enabled = configKit.getBoolean("enabled", false);
            if(enabled) {
                String name = configKit.getString("name");

                Kit kit = new Kit(name);

                kit.setSumo(configKit.getBoolean("sumo", false));
                kit.setCombo(configKit.getBoolean("combo", false));
                kit.setBuild(configKit.getBoolean("build", false));
                kit.setRanked(configKit.getBoolean("ranked", false));
                kit.setSpleef(configKit.getBoolean("spleef", false));
                kit.setParkour(configKit.getBoolean("parkour", false));

                kit.setArmor(((List<ItemStack>)configKit.get("armor")).toArray(new ItemStack[0]));
                kit.setContents(((List<ItemStack>)configKit.get("contents")).toArray(new ItemStack[0]));
                kit.setKitEditContents(((List<ItemStack>)configKit.get("kitedit")).toArray(new ItemStack[0]));

                kit.setWhitelistArenas(configKit.getStringList("whitelist-arenas"));
                kit.setBlacklistArenas(configKit.getStringList("blacklist-arenas"));

                this.kits.put(name.toLowerCase(Locale.ROOT), kit);
            }
        });
    }

    public Set<String> getKitsNames() {
        return this.kits.keySet();
    }
}
