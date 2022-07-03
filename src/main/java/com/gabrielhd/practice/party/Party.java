package com.gabrielhd.practice.party;

import com.gabrielhd.practice.Practice;
import com.gabrielhd.practice.lang.Lang;
import com.gabrielhd.practice.match.MatchTeam;
import com.gabrielhd.practice.utils.text.TextPlaceholders;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.stream.Stream;

@Getter @Setter
public class Party {

    private UUID leader;
    private Set<UUID> members;
    private int limit;
    private boolean open;

    public Party(UUID leader) {
        this.members = new HashSet<>();
        this.limit = 50;
        this.leader = leader;
        this.members.add(leader);
    }

    public void addMember(UUID uuid) {
        this.members.add(uuid);
    }

    public void removeMember(UUID uuid) {
        this.members.remove(uuid);
    }

    public void broadcast(Lang message, TextPlaceholders textPlaceholders) {
        this.members().forEach(member -> member.sendMessage(message.get(member, textPlaceholders)));
    }

    public MatchTeam[] split() {
        List<UUID> teamA = new ArrayList<>();
        List<UUID> teamB = new ArrayList<>();
        for (UUID member : this.members) {
            if (teamA.size() == teamB.size()) {
                teamA.add(member);
            } else {
                teamB.add(member);
            }
        }
        return new MatchTeam[] { new MatchTeam(teamA.get(0), teamA, 0), new MatchTeam(teamB.get(0), teamB, 1) };
    }

    public Stream<Player> members() {
        return this.members.stream().map(Bukkit::getPlayer).filter(Objects::nonNull);
    }
}
