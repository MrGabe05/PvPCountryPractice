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
        this.setup(plugin);
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

    private synchronized void setup(Practice plugin) {
        try {
            Statement statement = this.connection.createStatement();
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS " + this.table + " (UUID VARCHAR(100))");
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Error inserting columns! Please check your configuration!");
            plugin.getLogger().log(Level.SEVERE, "If this error persists, please report it to the developer!");

            e.printStackTrace();
        }

        plugin.getLogger().log(Level.INFO, "SQLite Setup finished");
    }

    @Override
    public Object getConnection() {
        return null;
    }
}
