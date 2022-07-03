package com.gabrielhd.practice.queue;

import lombok.Getter;

public enum QueueType {

    UNRANKED("UNRANKED", 0, "Unranked"), 
    RANKED("RANKED", 1, "Ranked");
    
    @Getter private final String name;
    
    public boolean isRanked() {
        return this == QueueType.RANKED;
    }
    
    public boolean isUnranked() {
        return this == QueueType.UNRANKED;
    }
    
    QueueType(String s, int n, String name) {
        this.name = name;
    }
}
