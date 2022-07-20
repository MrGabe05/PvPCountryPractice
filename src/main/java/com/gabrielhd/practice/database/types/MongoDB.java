package com.gabrielhd.practice.database.types;

import com.gabrielhd.practice.Practice;
import com.gabrielhd.practice.config.YamlConfig;
import com.gabrielhd.practice.database.DataHandler;
import com.gabrielhd.practice.events.EventType;
import com.gabrielhd.practice.kit.Kit;
import com.gabrielhd.practice.player.PlayerData;
import com.gabrielhd.practice.player.PlayerKit;
import com.gabrielhd.practice.settings.ProfileOptions;
import com.gabrielhd.practice.settings.item.ProfileOptionsItemState;
import com.gabrielhd.practice.utils.items.ItemUtil;
import com.mongodb.*;

import java.sql.Connection;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public class MongoDB extends DataHandler {

    private final Practice plugin;

    private final DB database;
    private final MongoClient mongoClient;

    public MongoDB(Practice plugin, String host, String port, String dbName, String user, String password) {
        this.plugin = plugin;

        mongoClient = new MongoClient(new MongoClientURI("mongodb://"+user+":"+password+"@"+host+":"+port+"/?authSource="+dbName+"&authMechanism=SCRAM-SHA-1"));
        database = mongoClient.getDB(dbName);

        if(!database.collectionExists("data")) database.createCollection("data", null);
    }

    @Override
    public Connection getConnection() {
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

                playerData.setKills((Long) document.get("kills"));
                playerData.setDeaths((Long) document.get("deaths"));
                playerData.setBestElo((Long) document.get("bestelo"));
                playerData.setRankeds((Long) document.get("rankedsleft"));

                DBCursor cursorKits = (DBCursor) document.get("kits");
                if(cursorKits.hasNext()) {
                    DBObject documentKit = cursorKits.next();

                    this.plugin.getKitManager().getKits().values().forEach(kit -> {
                        String kitName = kit.getName().toLowerCase(Locale.ROOT);

                        playerData.setElo(kitName, (Integer) documentKit.get("elo_" + kitName));
                        playerData.setRankedWins(kitName, (Integer) documentKit.get("rankedwins_" + kitName));
                        playerData.setRankedLosses(kitName, (Integer) documentKit.get("rankedlosses_" + kitName));
                        playerData.setUnrankedWins(kitName, (Integer) documentKit.get("unrankedwins_" + kitName));
                        playerData.setUnrankedLosses(kitName, (Integer) documentKit.get("unrankedlosses_" + kitName));
                    });
                }

                DBCursor cursorPlayerKits = (DBCursor) document.get("playerkits");
                if(cursorPlayerKits.hasNext()) {
                    DBObject documentKit = cursorPlayerKits.next();

                    this.plugin.getKitManager().getKits().values().forEach(kit -> {
                        DBCursor kits = (DBCursor) documentKit.get(kit.getName());

                        while(kits.hasNext()) {
                            DBObject documentPlayerKit = kits.next();

                            int id = (int) documentPlayerKit.get("id");
                            PlayerKit playerKit = new PlayerKit(kit.getName(), id, ItemUtil.stringToContents((String) documentPlayerKit.get("contents")), (String) documentPlayerKit.get("displayName"));
                            playerData.addPlayerKit(id, playerKit);
                        }
                    });
                }

                DBCursor cursorEvents = (DBCursor) document.get("events");
                if(cursorEvents.hasNext()) {
                    DBObject documentEvents = cursorEvents.next();

                    playerData.setWinsEvent(EventType.SUMO, (Integer) documentEvents.get("wins"));
                    playerData.setLossesEvent(EventType.SUMO, (Integer) documentEvents.get("losses"));
                }

                DBCursor cursorOptions = (DBCursor) document.get("options");
                if(cursorOptions.hasNext()) {
                    DBObject documentOptions = cursorOptions.next();

                    playerData.getOptions().setScoreboard(ProfileOptionsItemState.get((String) documentOptions.get("scoreboard")));
                    playerData.getOptions().setTime(ProfileOptionsItemState.get((String) documentOptions.get("time")));
                    playerData.getOptions().setDuelRequests((boolean) documentOptions.get("duelrequest"));
                    playerData.getOptions().setPartyInvites((boolean) documentOptions.get("partyinvites"));
                    playerData.getOptions().setSpectators((boolean) documentOptions.get("spectators"));
                }

                return true;
            }
            return false;
        });
    }

    @Override
    public CompletionStage<Boolean> uploadPlayer(PlayerData playerData) {
        return CompletableFuture.supplyAsync(() -> {
            DBCollection collection = database.getCollection("data");
            BasicDBObject document = new BasicDBObject();
            document.put("uuid", playerData.getUuid().toString());
            document.put("kills", playerData.getKills());
            document.put("deaths", playerData.getDeaths());
            document.put("rankedsleft", playerData.getRankeds());

            BasicDBObject kitsDocument = new BasicDBObject();
            BasicDBObject playerKitsDocument = new BasicDBObject();
            for(Kit kit : this.plugin.getKitManager().getKits().values()) {
                String kitName = kit.getName().toLowerCase(Locale.ROOT);

                kitsDocument.put("elo_" + kitName, playerData.getElo(kitName));
                kitsDocument.put("rankedwins_" + kitName, playerData.getRankedWins(kitName));
                kitsDocument.put("rankedlosses_" + kitName, playerData.getRankedLosses(kitName));
                kitsDocument.put("unrankedwins_" + kitName, playerData.getUnrankedWins(kitName));
                kitsDocument.put("unrankedlosses_" + kitName, playerData.getUnrankedLosses(kitName));

                Map<Integer, PlayerKit> playerKits = playerData.getPlayerKits(kitName);
                if(playerKits != null && !playerKits.isEmpty()) {
                    BasicDBObject playerKitID = new BasicDBObject();

                    for(Map.Entry<Integer, PlayerKit> value : playerKits.entrySet()) {
                        BasicDBObject playerKit = new BasicDBObject();

                        playerKit.put("id", value.getKey());
                        playerKit.put("displayName", value.getValue().getDisplayName());
                        playerKit.put("contents", ItemUtil.contentsToString(value.getValue().getContents()));

                        playerKitID.put(String.valueOf(value.getKey()), playerKit);
                    }

                    playerKitsDocument.put(kitName, playerKitID);
                }
            }
            document.put("kits", kitsDocument);
            document.put("playerkits", playerKitsDocument);

            ProfileOptions options = playerData.getOptions();

            BasicDBObject optionsDocument = new BasicDBObject();
            optionsDocument.put("scoreboard", options.getScoreboard().name());
            optionsDocument.put("time", options.getTime().name());
            optionsDocument.put("duelrequest", options.isDuelRequests());
            optionsDocument.put("partyinvites", options.isPartyInvites());
            optionsDocument.put("spectators", options.isSpectators());
            document.put("options", optionsDocument);

            BasicDBObject eventsDocument = new BasicDBObject();
            eventsDocument.put("wins", playerData.getWinsEvent(EventType.SUMO));
            eventsDocument.put("losses", playerData.getLossesEvent(EventType.SUMO));
            document.put("events", eventsDocument);

            collection.insert(document);
            collection.save(document);

            return true;
        });
    }
}
