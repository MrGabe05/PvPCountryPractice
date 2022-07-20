package com.gabrielhd.practice.database;

import com.gabrielhd.practice.Practice;
import com.gabrielhd.practice.player.PlayerData;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.logging.Level;

public abstract class DataHandler {

    public abstract Connection getConnection();

    private static final String TABLE = "pvpcountry_";

    private final String CREATE_TABLE = "CREATE TABLE IF NOT EXISTS " + TABLE + " (uuid VARCHAR(100), kills long, deaths long, bestelo long, rankedsleft long, elo TEXT, rankedwins TEXT, rankedlosses TEXT, unrankedwins TEXT, unrankedlosses TEXT, eventswins TEXT, eventslosses TEXT, PRIMARY KEY ('uuid'));";
    private final String INSERT_DATA = "INSERT INTO " + TABLE + " (uuid, kills, deaths, bestelo, rankedsleft, elo, rankedwins, rankedlosses, unrankedwins, unrankedlosses, eventswins, eventslosses) VALUES ('%s', '0', '0', '0', '0', 'empty', 'empty', 'empty', 'empty', 'empty', 'empty', 'empty');";
    private final String UPDATE_DATA = "UPDATE " + TABLE + " SET kills='%s', deaths='%s', bestelo='%s', rankedsleft='%s', elo='%s', rankedwins='%s', rankedlosses='%s', unrankedwins='%s', unrankedlosses='%s', eventswins='%s', eventslosses='%s' WHERE uuid='%s';";

    private final String SELECT_PLAYER = "SELECT * FROM " + TABLE + " WHERE uuid='%s'";

    public synchronized void setupTable() {
        try {
            this.execute(CREATE_TABLE);
        } catch (SQLException e) {
            Practice.getInstance().getLogger().log(Level.SEVERE, "Error inserting columns! Please check your configuration!");
            Practice.getInstance().getLogger().log(Level.SEVERE, "If this error persists, please report it to the developer!");

            e.printStackTrace();
        }
    }

    private void execute(String sql, Object... replacements) throws SQLException {
        Connection connection = this.getConnection();
        try(PreparedStatement statement = connection.prepareStatement(String.format(sql, replacements))) {
            statement.execute();
        }
    }

    private boolean isClosed() {
        Connection connection = this.getConnection();
        try {
            if(connection != null && !connection.isClosed() && !connection.isValid(5000)) {
                return false;
            }
        } catch (SQLException ignored) {}
        return false;
    }

    private Map<String, Integer> get(String s) {
        Map<String, Integer> value = new HashMap<>();

        String[] split = s.split(";");

        for(String s2 : split) {
            String[] split2 = s2.split(":");

            value.put(split2[0], Integer.valueOf(split2[1]));
        }

        return value;
    }

    private String get(Map<String, Integer> value) {
        StringBuilder builder = new StringBuilder();

        for(Map.Entry<String, Integer> entry : value.entrySet()) {
            if(builder.length() > 0) {
                builder.append(";");
            }

            builder.append(entry.getKey()).append(entry.getValue());
        }

        return builder.toString();
    }

    public CompletionStage<Boolean> loadPlayer(PlayerData playerData) {
        return CompletableFuture.supplyAsync(() -> {
            if(isClosed()) return false;

            Connection connection = this.getConnection();

            try (PreparedStatement statement = connection.prepareStatement(String.format(SELECT_PLAYER, playerData.getUuid().toString()))) {
                ResultSet rs = statement.executeQuery();

                if(rs == null || !rs.next()) {
                    this.execute(INSERT_DATA, playerData.getUuid().toString());
                    return true;
                }

                playerData.setKills(rs.getLong("kills"));
                playerData.setDeaths(rs.getLong("deaths"));
                playerData.setBestElo(rs.getLong("bestelo"));
                playerData.setRankeds(rs.getLong("rankedsleft"));
                playerData.setRankedEloMap(get(rs.getString("elo")));
                playerData.setRankedWinsMap(get(rs.getString("rankedwins")));
                playerData.setRankedLossesMap(get(rs.getString("rankedlosses")));
                playerData.setUnrankedWinsMap(get(rs.getString("unrankedwins")));
                playerData.setUnrankedLossesMap(get(rs.getString("unrankedlosses")));

                playerData.setEventsWinsMap(get(rs.getString("eventswins")));
                playerData.setEventsLossesMap(get(rs.getString("eventslosses")));
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }

            return true;
        });
    }

    public CompletionStage<Boolean> uploadPlayer(PlayerData playerData) {
        return CompletableFuture.supplyAsync(() -> {
            if(isClosed()) return false;

            Connection connection = this.getConnection();
            try (PreparedStatement statement = connection.prepareStatement(String.format(SELECT_PLAYER, playerData.getUuid().toString())); ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    this.execute(UPDATE_DATA, playerData.getKills(), playerData.getDeaths(), playerData.getBestElo(), playerData.getRankeds(), get(playerData.getRankedEloMap()), get(playerData.getRankedWinsMap()), get(playerData.getRankedLossesMap()), get(playerData.getUnrankedWinsMap()), get(playerData.getUnrankedLossesMap()), get(playerData.getEventsWinsMap()), get(playerData.getEventsLossesMap()), playerData.getUuid().toString());
                }
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
            return true;
        });
    }
}
