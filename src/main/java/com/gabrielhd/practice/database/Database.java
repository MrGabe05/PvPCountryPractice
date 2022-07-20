package com.gabrielhd.practice.database;

import com.gabrielhd.practice.Practice;
import com.gabrielhd.practice.config.YamlConfig;
import com.gabrielhd.practice.database.types.MongoDB;
import com.gabrielhd.practice.database.types.MySQL;
import com.gabrielhd.practice.database.types.SQLite;
import org.bukkit.configuration.file.FileConfiguration;

public class Database {

    private static DataHandler storage;
    
    public Database(Practice plugin) {
        FileConfiguration data = new YamlConfig(plugin, "Settings");

        String host = data.getString("Database.Host");
        String port = data.getString("Database.Port");
        String db = data.getString("Database.Database");
        String user = data.getString("Database.Username");
        String pass = data.getString("Database.Password");

        switch (data.getString("StorageType", "sqlite").toLowerCase()) {
            case "mysql": {
                storage = new MySQL(plugin, host, port, db, user, pass);
                return;
            }
            case "mongo":
            case "mongodb": {
                storage = new MongoDB(plugin, host, port, db, user, pass);
                return;
            }
            case "sqlite":
            default: {
                storage = new SQLite(plugin);
                break;
            }
        }
    }
    
    public static DataHandler getStorage() {
        return Database.storage;
    }
}
