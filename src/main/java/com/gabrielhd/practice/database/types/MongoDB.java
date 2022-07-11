package com.gabrielhd.practice.database.types;

import com.gabrielhd.practice.Practice;
import com.gabrielhd.practice.config.YamlConfig;
import com.gabrielhd.practice.database.DataHandler;
import com.gabrielhd.practice.player.PlayerData;
import com.mongodb.*;

import java.sql.Connection;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public class MongoDB extends DataHandler {

    private final MongoClient mongoClient;
    private final DB database;

    public MongoDB(Practice plugin) {
        YamlConfig config = new YamlConfig(plugin, "Settings");

        String host = config.getString("Database.Host");
        String user = config.getString("Database.Username");
        String dbName = config.getString("Database.Database");
        String password = config.getString("Database.Password");
        String port = config.getString("Database.Port");

        mongoClient = new MongoClient(new MongoClientURI("mongodb://"+user+":"+password+"@"+host+":"+port+"/?authSource="+dbName+"&authMechanism=SCRAM-SHA-1"));
        database = mongoClient.getDB(dbName);

        if(!database.collectionExists("data")) database.createCollection("data", null);
    }

    public boolean checkPlayer(UUID uuid) {
        DBCollection collection = database.getCollection("data");
        BasicDBObject searchQuery = new BasicDBObject();
        searchQuery.put("uuid", uuid.toString());
        DBCursor cursor = collection.find(searchQuery);

        return cursor.hasNext();
    }

    @Override
    public Object getConnection() {
        return null;
    }

    @Override
    public CompletionStage<Boolean> loadPlayer(PlayerData playerData) {
        return CompletableFuture.supplyAsync(() -> {
            DBCollection collection = database.getCollection("data");
            BasicDBObject searchQuery = new BasicDBObject();
            searchQuery.put("uuid", playerData.getUuid().toString());
            DBCursor cursor = collection.find(searchQuery);

            if(cursor.hasNext()) {
                DBObject document = cursor.next();

            }
            return true;
        });
    }
}
