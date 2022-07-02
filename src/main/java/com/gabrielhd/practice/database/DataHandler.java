package com.gabrielhd.practice.database;

import com.gabrielhd.practice.player.PlayerData;

import java.sql.Connection;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public abstract class DataHandler {

    public abstract Connection getConnection();

    public CompletionStage<Boolean> loadPlayer(PlayerData playerData) {
        return CompletableFuture.supplyAsync(() -> {
            return true;
        });
    }


    public CompletionStage<Boolean> uploadPlayer(PlayerData playerData) {
        return CompletableFuture.supplyAsync(() -> {
            return true;
        });
    }
}
