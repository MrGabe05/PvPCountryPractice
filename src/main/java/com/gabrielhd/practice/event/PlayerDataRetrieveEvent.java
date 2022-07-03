package com.gabrielhd.practice.event;

import com.gabrielhd.practice.player.PlayerData;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class PlayerDataRetrieveEvent extends Event {

    private static final HandlerList HANDLERS;
    private final PlayerData playerData;
    
    static {
        HANDLERS = new HandlerList();
    }
    
    public static HandlerList getHandlerList() {
        return PlayerDataRetrieveEvent.HANDLERS;
    }
    
    @Override
    public HandlerList getHandlers() {
        return PlayerDataRetrieveEvent.HANDLERS;
    }
    
    public PlayerData getPlayerData() {
        return this.playerData;
    }
    
    public PlayerDataRetrieveEvent(PlayerData playerData) {
        this.playerData = playerData;
    }
}
