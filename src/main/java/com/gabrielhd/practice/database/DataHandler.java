package com.gabrielhd.practice.database;

import com.gabrielhd.practice.player.PlayerData;

public interface DataHandler {
    void loadPlayer(PlayerData p0);
    
    void uploadPlayer(PlayerData p0);

    void close();
}
