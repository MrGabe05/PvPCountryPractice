package com.gabrielhd.practice.event.match;

import com.gabrielhd.practice.match.Match;

public class MatchStartEvent extends MatchEvent {

    public MatchStartEvent(Match match) {
        super(match);
    }
}
