package com.gabrielhd.practice.database.types;

import com.gabrielhd.practice.Practice;
import com.gabrielhd.practice.config.YamlConfig;
import com.gabrielhd.practice.database.DataHandler;

import java.sql.*;
import java.util.logging.Level;

public class SQLite extends DataHandler {

    private final String table;
    private Connection connection;
    
    public SQLite(Practice plugin) {
        this.table = "pvpcountry_";

        this.connect(plugin);
        this.setupTable();
    }
    
    private synchronized void connect(Practice plugin) {
        try {
            Class.forName("org.sqlite.JDBC");
            this.connection = DriverManager.getConnection("jdbc:sqlite:" + plugin.getDataFolder() + "/Database.db");
        } catch (SQLException | ClassNotFoundException ex) {
            plugin.getLogger().log(Level.SEVERE, "Can't initialize database connection! Please check your configuration!");
            plugin.getLogger().log(Level.SEVERE, "If this error persists, please report it to the developer!");

            ex.printStackTrace();
        }
    }


    @Override
    public Connection getConnection() {
        return null;
    }
}
