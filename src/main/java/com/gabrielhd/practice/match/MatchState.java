package com.gabrielhd.practice.match;

public enum MatchState
{
    STARTING("STARTING", 0), 
    FIGHTING("FIGHTING", 1), 
    SWITCHING("SWITCHING", 2), 
    ENDING("ENDING", 3);
    
    MatchState(final String s, final int n) {
    }
}
