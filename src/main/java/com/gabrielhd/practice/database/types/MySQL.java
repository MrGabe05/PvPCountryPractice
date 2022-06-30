package com.gabrielhd.practice.database.types;

import com.gabrielhd.practice.Practice;
import com.gabrielhd.practice.config.YamlConfig;
import com.gabrielhd.practice.database.DataHandler;
import com.gabrielhd.practice.player.PlayerData;
import com.zaxxer.hikari.HikariDataSource;
import com.zaxxer.hikari.pool.HikariPool;

import java.sql.*;
import java.util.UUID;
import java.util.logging.Level;

public class MySQL implements DataHandler {

    private final Practice plugin;

    private final String url;
    private final String username;
    private final String password;
    private final String table;
    private Connection connection;
    private HikariDataSource ds;
    
    public MySQL(Practice plugin, String host, String port, String database, String username, String password) {
        this.plugin = plugin;
        this.url = "jdbc:mysql://" + host + ":" + port + "/" + database;
        this.username = username;
        this.password = password;
        this.table = new YamlConfig(plugin, "Settings").getString("MySQL.TableName");
        try {
            this.setConnectionArguments();
        } catch (RuntimeException e) {
            if (e instanceof IllegalArgumentException) {
                plugin.getLogger().log(Level.SEVERE, "Invalid database arguments! Please check your configuration!");
                plugin.getLogger().log(Level.SEVERE, "If this error persists, please report it to the developer!");

                throw new IllegalArgumentException(e);
            }
            if (e instanceof HikariPool.PoolInitializationException) {
                plugin.getLogger().log(Level.SEVERE, "Can't initialize database connection! Please check your configuration!");
                plugin.getLogger().log(Level.SEVERE, "If this error persists, please report it to the developer!");
                throw new HikariPool.PoolInitializationException(e);
            }
            plugin.getLogger().log(Level.SEVERE, "Can't use the Hikari Connection Pool! Please, report this error to the developer!");
            throw e;
        }
        this.setupTable();
    }
    
    private synchronized void setConnectionArguments() throws RuntimeException {
        (this.ds = new HikariDataSource()).setPoolName("Practice MySQL");

        this.ds.setDriverClassName("com.mysql.jdbc.Driver");
        this.ds.setJdbcUrl(this.url);
        this.ds.addDataSourceProperty("cachePrepStmts", "true");
        this.ds.addDataSourceProperty("prepStmtCacheSize", "250");
        this.ds.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        this.ds.addDataSourceProperty("characterEncoding", "utf8");
        this.ds.addDataSourceProperty("encoding", "UTF-8");
        this.ds.addDataSourceProperty("useUnicode", "true");
        this.ds.addDataSourceProperty("useSSL", "false");
        this.ds.setUsername(this.username);
        this.ds.setPassword(this.password);
        this.ds.setMaxLifetime(180000L);
        this.ds.setIdleTimeout(60000L);
        this.ds.setMinimumIdle(1);
        this.ds.setMaximumPoolSize(8);
        try {
            this.connection = this.ds.getConnection();
        }
        catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Error on setting connection!");
        }

        plugin.getLogger().log(Level.INFO, "Connection arguments loaded, Hikari ConnectionPool ready!");
    }
    
    public ResultSet query(String query) {
        try {
            Statement stmt = this.connection.createStatement();
            return stmt.executeQuery(query);
        }
        catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    public boolean playerExists(UUID uuid) {
        try {
            ResultSet rs = this.query("SELECT * FROM " + this.table + " WHERE UUID='" + uuid.toString() + "'");
            return rs != null && rs.next() && rs.getString("UUID") != null;
        }
        catch (SQLException ex) {
            ex.printStackTrace();
            return false;
        }
    }
    
    public void setupTable() {
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
    }
    
    public void createPlayer(UUID uuid) {
        if (this.connection != null) {
            try {
                if (!this.playerExists(uuid)) {
                    this.connection.createStatement().executeUpdate("INSERT INTO " + this.table + " (UUID, Wins, Kills, Deaths, Played, Level, Exp) VALUES ('" + uuid + "', '0', '0', '0', '0', '1', '10');");
                }
            }
            catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
    
    @Override
    public void loadPlayer(PlayerData playerData) {
        ResultSet resultSet = this.query("SELECT * FROM '" + this.table + "' WHERE UUID='" + playerData.getUuid() + "'");
        try {
            if (resultSet != null && resultSet.next()) {

            }
            else {
                this.createPlayer(playerData.getUuid());
            }
        }
        catch (SQLException ex) {
            this.createPlayer(playerData.getUuid());
        }
    }
    
    @Override
    public void uploadPlayer(PlayerData playerData) {
        if (this.connection != null) {

        }
    }
    
    @Override
    public void close() {
        try {
            if (this.connection != null && !this.connection.isClosed()) {
                this.connection.close();
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }
}
