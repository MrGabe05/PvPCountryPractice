package com.gabrielhd.practice.settings.item;

public enum ProfileOptionsItemState
{
    ENABLED("ENABLED", 0), 
    DISABLED("DISABLED", 1), 
    SHOW_PING("SHOW_PING", 2), 
    DAY("DAY", 3), 
    SUNSET("SUNSET", 4), 
    NIGHT("NIGHT", 5);
    
    ProfileOptionsItemState(final String s, final int n) {
    }
}
