package com.gabrielhd.practice.player;

import lombok.Getter;

import java.util.UUID;

@Getter
public class PlayerData {

    private final UUID uuid;

    public PlayerData(UUID uuid) {
        this.uuid = uuid;
    }
}
