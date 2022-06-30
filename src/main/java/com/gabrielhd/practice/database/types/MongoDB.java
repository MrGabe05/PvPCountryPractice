package com.gabrielhd.practice.database.types;

import com.gabrielhd.practice.Practice;
import com.gabrielhd.practice.config.YamlConfig;
import com.gabrielhd.practice.database.DataHandler;
import com.gabrielhd.practice.player.PlayerData;
import com.mongodb.DB;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;

public class MongoDB implements DataHandler {

    private final MongoClient mongoClient;
    private final DB database;

    public MongoDB(Practice plugin) {
        YamlConfig config = new YamlConfig(plugin, "Settings");

        String host = config.getString("Database.Host");
        String user = config.getString("Database.Username");
        String dbName = config.getString("Database.DBName");
        String password = config.getString("Database.Password");
        String port = config.getString("Database.Port");

        mongoClient = new MongoClient(new MongoClientURI("mongodb://"+user+":"+password+"@"+host+":"+port+"/?authSource="+dbName+"&authMechanism=SCRAM-SHA-1"));
        database = mongoClient.getDB(config.getString("Database.DBName"));

        if(!database.collectionExists("data")) database.createCollection("data", null);
    }

    @Override
    public void loadPlayer(PlayerData p0) {

    }

    @Override
    public void uploadPlayer(PlayerData p0) {

    }

    @Override
    public void close() {

    }
}
