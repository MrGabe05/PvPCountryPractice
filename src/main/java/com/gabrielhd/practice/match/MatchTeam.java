package com.gabrielhd.practice.match;

import com.gabrielhd.practice.team.KillableTeam;
import lombok.Getter;

import java.util.List;
import java.util.UUID;

@Getter
public class MatchTeam extends KillableTeam {

    private final int teamID;

    public MatchTeam(final UUID leader, final List<UUID> players, final int teamID) {
        super(leader, players);
        this.teamID = teamID;
    }
}
