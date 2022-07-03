package com.gabrielhd.practice.event.match;

import com.gabrielhd.practice.match.Match;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class MatchEvent extends Event
{
    private static final HandlerList HANDLERS;
    private final Match match;
    
    static {
        HANDLERS = new HandlerList();
    }
    
    public static HandlerList getHandlerList() {
        return MatchEvent.HANDLERS;
    }
    
    @Override
    public HandlerList getHandlers() {
        return MatchEvent.HANDLERS;
    }
    
    public Match getMatch() {
        return this.match;
    }
    
    public MatchEvent(Match match) {
        this.match = match;
    }
}
