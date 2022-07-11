package com.gabrielhd.practice.cache;

import com.gabrielhd.practice.player.PlayerData;
import com.gabrielhd.practice.player.PlayerState;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class StatusCache extends Thread {

    @Getter private static StatusCache instance;

    private int fighting;
    private int queueing;
    
    public StatusCache() {
        StatusCache.instance = this;
    }
    
    @Override
    public void run() {
        int figh = 0;
        int queue = 0;
        for (PlayerData playerData : PlayerData.getAllData()) {
            if (playerData.getPlayerState() == PlayerState.FIGHTING) {
                ++figh;
            }
            if (playerData.getPlayerState() == PlayerState.QUEUE) {
                ++queue;
            }
        }
        this.fighting = figh;
        this.queueing = queue;
        try {
            Thread.sleep(500L);
        }
        catch (InterruptedException ignored) {}
    }
}
