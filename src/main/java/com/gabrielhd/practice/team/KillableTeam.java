package com.gabrielhd.practice.team;

import com.gabrielhd.practice.Practice;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Stream;

@Getter @Setter
public class KillableTeam {

    protected final Practice plugin;

    private final List<UUID> players;
    private final List<UUID> alivePlayers;
    private final String leaderName;
    private UUID leader;

    public KillableTeam(final UUID leader, final List<UUID> players) {
        this.plugin = Practice.getInstance();
        this.alivePlayers = new ArrayList<>();
        this.leader = leader;
        this.leaderName = this.plugin.getServer().getPlayer(leader).getName();
        this.players = players;
        this.alivePlayers.addAll(players);
    }

    public void killPlayer(UUID playerUUID) {
        this.alivePlayers.remove(playerUUID);
    }

    public Stream<Player> alivePlayers() {
        return this.alivePlayers.stream().map(this.plugin.getServer()::getPlayer).filter(Objects::nonNull);
    }

    public Stream<Player> players() {
        return this.players.stream().map(this.plugin.getServer()::getPlayer).filter(Objects::nonNull);
    }
}
