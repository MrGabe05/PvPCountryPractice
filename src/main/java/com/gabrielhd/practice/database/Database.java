package com.gabrielhd.practice.database;

import com.gabrielhd.practice.Practice;
import com.gabrielhd.practice.config.YamlConfig;
import com.gabrielhd.practice.database.types.MySQL;
import com.gabrielhd.practice.database.types.SQLite;
import org.bukkit.configuration.file.FileConfiguration;

public class Database {

    private static DataHandler storage;
    
    public Database(Practice plugin) {
        FileConfiguration data = new YamlConfig(plugin, "Settings");
        if (data.getString("StorageType", "SQLite").equalsIgnoreCase("MySQL")) {
            Database.storage = new MySQL(plugin, data.getString("MySQL.Host"), data.getString("MySQL.Port"), data.getString("MySQL.Database"), data.getString("MySQL.Username"), data.getString("MySQL.Password"));
        }
        else {
            Database.storage = new SQLite(plugin);
        }
    }
    
    public static DataHandler getStorage() {
        return Database.storage;
    }
}
