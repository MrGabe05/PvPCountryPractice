package com.gabrielhd.practice.database.types;

import com.gabrielhd.practice.Practice;
import com.gabrielhd.practice.config.YamlConfig;
import com.gabrielhd.practice.database.DataHandler;
import com.gabrielhd.practice.player.PlayerData;

import java.sql.*;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.logging.Level;

public class SQLite extends DataHandler {

    private final String table;
    private Connection connection;
    
    public SQLite(Practice plugin) {
        this.table = new YamlConfig(plugin,"Settings").getString("MySQL.TableName");
        this.connect(plugin);
        this.setup(plugin);
    }
    
    private synchronized void connect(Practice plugin) {
        try {
            Class.forName("org.sqlite.JDBC");
            this.connection = DriverManager.getConnection("jdbc:sqlite:" + plugin.getDataFolder() + "/Data/Database.db");
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
            DatabaseMetaData dm = this.connection.getMetaData();
            ResultSet wins = dm.getColumns(null, null, this.table, "Wins");
            if (!wins.next()) {
                statement.executeUpdate("ALTER TABLE " + this.table + " ADD COLUMN Wins int AFTER UUID;");
            }
            wins.close();
            ResultSet kills = dm.getColumns(null, null, this.table, "Kills");
            if (!kills.next()) {
                statement.executeUpdate("ALTER TABLE " + this.table + " ADD COLUMN Kills double AFTER Wins;");
            }
            kills.close();
            ResultSet losses = dm.getColumns(null, null, this.table, "Deaths");
            if (!losses.next()) {
                statement.executeUpdate("ALTER TABLE " + this.table + " ADD COLUMN Deaths int AFTER Kills;");
            }
            losses.close();
            ResultSet played = dm.getColumns(null, null, this.table, "Played");
            if (!played.next()) {
                statement.executeUpdate("ALTER TABLE " + this.table + " ADD COLUMN Played int AFTER Deaths;");
            }
            played.close();
            ResultSet level = dm.getColumns(null, null, this.table, "Level");
            if (!level.next()) {
                statement.executeUpdate("ALTER TABLE " + this.table + " ADD COLUMN Level int AFTER Played;");
            }
            level.close();
            ResultSet exp = dm.getColumns(null, null, this.table, "Exp");
            if (!exp.next()) {
                statement.executeUpdate("ALTER TABLE " + this.table + " ADD COLUMN Exp int AFTER Level;");
            }
            exp.close();
            statement.close();
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Error inserting columns! Please check your configuration!");
            plugin.getLogger().log(Level.SEVERE, "If this error persists, please report it to the developer!");

            e.printStackTrace();
        }

        plugin.getLogger().log(Level.INFO, "SQLite Setup finished");
    }

    @Override
    public Connection getConnection() {
        return null;
    }
}
