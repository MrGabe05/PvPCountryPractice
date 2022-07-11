package com.gabrielhd.practice.database;

import com.gabrielhd.practice.player.PlayerData;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public abstract class DataHandler {

    public abstract Object getConnection();

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
