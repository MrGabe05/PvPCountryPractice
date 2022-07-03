package com.gabrielhd.practice.match;

import com.gabrielhd.practice.arena.Arena;
import lombok.Getter;

import java.util.UUID;

@Getter
public class MatchRequest {

    private final Arena arena;
    private final UUID requester;
    private final UUID requested;
    private final String kitName;

    private final boolean party;
    
    public MatchRequest(final UUID requester, final UUID requested, final Arena arena, final String kitName, final boolean party) {
        this.requester = requester;
        this.requested = requested;
        this.arena = arena;
        this.kitName = kitName;
        this.party = party;
    }
}
