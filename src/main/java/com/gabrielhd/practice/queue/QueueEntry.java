package com.gabrielhd.practice.queue;

import lombok.Getter;

@Getter
public class QueueEntry {

    private final int elo;
    private final boolean party;
    private final String kitName;
    private final QueueType queueType;
    
    public QueueEntry(QueueType queueType, String kitName, int elo, boolean party) {
        this.queueType = queueType;
        this.kitName = kitName;
        this.elo = elo;
        this.party = party;
    }
}
