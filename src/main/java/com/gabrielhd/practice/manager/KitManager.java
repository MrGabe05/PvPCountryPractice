package com.gabrielhd.practice.manager;

import com.gabrielhd.practice.Practice;
import com.gabrielhd.practice.config.YamlConfig;
import com.gabrielhd.practice.kit.Kit;
import lombok.Getter;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

@Getter
public class KitManager {

    private final Map<String, Kit> kits;
    private final Set<String> rankedKits;

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
            String name = configKit.getString("name");

            Kit kit = new Kit(name);

            kit.setEnabled(enabled);
            kit.setSumo(configKit.getBoolean("sumo", false));
            kit.setCombo(configKit.getBoolean("combo", false));
            kit.setBuild(configKit.getBoolean("build", false));
            kit.setRanked(configKit.getBoolean("ranked", false));
            kit.setSpleef(configKit.getBoolean("spleef", false));
            kit.setParkour(configKit.getBoolean("parkour", false));

            kit.setArmor(((List<ItemStack>)configKit.get("armor")).toArray(new ItemStack[0]));
            kit.setContents(((List<ItemStack>)configKit.get("contents")).toArray(new ItemStack[0]));

            kit.setWhitelistArenas(new HashSet<>(configKit.getStringList("whitelist-arenas")));
            kit.setBlacklistArenas(new HashSet<>(configKit.getStringList("blacklist-arenas")));

            this.kits.put(name.toLowerCase(Locale.ROOT), kit);
        });
    }

    public void deleteKit(final String name) {
        this.kits.remove(name);
    }

    public void createKit(final String name) {
        this.kits.put(name, new Kit(name));
    }

    public Kit getKit(String name) {
        return this.kits.get(name.toLowerCase(Locale.ROOT));
    }

    public Set<String> getKitsNames() {
        return this.kits.keySet();
    }
}
