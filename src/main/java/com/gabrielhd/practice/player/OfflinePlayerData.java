package com.gabrielhd.practice.player;

import java.util.UUID;

public class OfflinePlayerData extends PlayerData {

    public OfflinePlayerData(UUID uuid) {
        super(uuid, false);
    }
}
