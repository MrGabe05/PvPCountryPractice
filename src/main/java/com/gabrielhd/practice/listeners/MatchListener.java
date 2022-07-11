package com.gabrielhd.practice.listeners;

import com.gabrielhd.practice.Practice;
import com.gabrielhd.practice.arena.Arena;
import com.gabrielhd.practice.event.match.MatchEndEvent;
import com.gabrielhd.practice.event.match.MatchStartEvent;
import com.gabrielhd.practice.events.CustomEvent;
import com.gabrielhd.practice.events.EventPlayer;
import com.gabrielhd.practice.events.types.Sumo;
import com.gabrielhd.practice.kit.Kit;
import com.gabrielhd.practice.lang.Lang;
import com.gabrielhd.practice.match.Match;
import com.gabrielhd.practice.match.MatchState;
import com.gabrielhd.practice.player.PlayerData;
import com.gabrielhd.practice.player.PlayerState;
import com.gabrielhd.practice.tasks.MatchRunnable;
import com.gabrielhd.practice.utils.block.BlockUtil;
import com.gabrielhd.practice.utils.inventory.Snapshot;
import com.gabrielhd.practice.utils.others.EloUtil;
import com.gabrielhd.practice.utils.text.Clickable;
import com.gabrielhd.practice.utils.text.TextPlaceholders;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.HashSet;
import java.util.Set;

public class MatchListener implements Listener {

    private final Practice plugin;
    
    public MatchListener() {
        this.plugin = Practice.getInstance();
    }
    
    @EventHandler
    public void onMatchStart(MatchStartEvent event) {
        Match match = event.getMatch();
        Kit kit = match.getKit();
        if (!kit.isEnabled()) {
            match.broadcast(Lang.KIT_DISABLED, new TextPlaceholders().set("%kit%", kit.getName()));
            this.plugin.getMatchManager().removeMatch(match);
            return;
        }
        if (kit.isBuild() || kit.isSpleef()) {
            Arena arena = event.getMatch().getArena();
            if (arena.getAvailableArenas().isEmpty()) {
                match.broadcast(Lang.NOT_AVAILABLE_ARENAS, new TextPlaceholders());
                this.plugin.getMatchManager().removeMatch(match);
                return;
            }
            match.setStandArena(arena.getAvailableArena());
            arena.removeAvailableArena(match.getStandArena());
            arena.getAvailableArenas().remove(match.getStandArena());

            this.plugin.getArenaManager().setArenaMatchUUID(match.getStandArena(), match.getMatchId());
        }
        Set<Player> matchPlayers = new HashSet<>();
        match.getTeams().forEach(team -> team.alivePlayers().forEach(player -> {
            matchPlayers.add(player);

            this.plugin.getMatchManager().removeMatchRequests(player.getUniqueId());

            PlayerData playerData = PlayerData.of(player);
            if(match.getType().isRanked()) {
                int newRanked = (int) (playerData.getRankeds() - 1);
                playerData.setRankeds(newRanked);
            }

            playerData.setPlayerState(PlayerState.FIGHTING);
            playerData.setCurrentMatchID(match.getMatchId());
            playerData.setTeamID(team.getTeamID());
            playerData.setMissedPots(0);
            playerData.setLongestCombo(0);
            playerData.setCombo(0);
            playerData.setHits(0);

            Location locationA = ((match.getStandArena() != null) ? match.getStandArena().getA() : match.getArena().getA()).clone();
            Location locationB = ((match.getStandArena() != null) ? match.getStandArena().getB() : match.getArena().getB()).clone();
            locationA.setY(locationA.getY() + 1);
            locationB.setY(locationB.getY() + 1);

            player.teleport((team.getTeamID() == 1) ? locationA : locationB);

            this.plugin.getMatchManager().giveKits(player, kit);
        }));

        new MatchRunnable(match).runTaskTimer(this.plugin, 20L, 20L);
    }
    
    @EventHandler
    public void onMatchEnd(MatchEndEvent event) {
        Match match = event.getMatch();

        match.setMatchState(MatchState.ENDING);
        match.setWinningTeamId(event.getWinningTeam().getTeamID());
        match.setCountdown(4);

        if (match.isFFA()) {
            StringBuilder winnersString = new StringBuilder();
            StringBuilder lossersString = new StringBuilder();

            event.getWinningTeam().players().forEach(player -> {
                if(winnersString.length() > 0) winnersString.append(", ");
                winnersString.append(player.getName());
            });
            event.getLosingTeam().players().forEach(player -> {
                if(lossersString.length() > 0) lossersString.append(", ");
                lossersString.append(player.getName());
            });

            match.broadcast(Lang.FINISH_MESSAGE, new TextPlaceholders().set("%winners%", winnersString.toString()).set("%lossers%", lossersString.toString()));
        } else {
            Player winner = event.getWinningTeam().players().findFirst().orElse(null);
            Player losser = event.getLosingTeam().players().findFirst().orElse(null);

            String kitName = match.getKit().getName();

            PlayerData winnerLeaderData = PlayerData.of(winner);
            PlayerData loserLeaderData = PlayerData.of(losser);
            if(match.getType().isUnranked()) {
                winnerLeaderData.setUnrankedWins(kitName, winnerLeaderData.getUnrankedWins(kitName) + 1);
                loserLeaderData.setUnrankedLosses(kitName, loserLeaderData.getUnrankedLosses(kitName) + 1);
            }

            if (match.getType().isRanked()) {
                int[] preElo = new int[2];
                int[] newElo = new int[2];
                int winnerElo = winnerLeaderData.getElo(kitName);
                int loserElo = loserLeaderData.getElo(kitName);
                preElo[0] = winnerElo;
                preElo[1] = loserElo;
                int newWinnerElo = EloUtil.getNewRating(winnerElo, loserElo, true);
                int newLoserElo = EloUtil.getNewRating(loserElo, winnerElo, false);
                newElo[0] = newWinnerElo;
                newElo[1] = newLoserElo;

                winnerLeaderData.setElo(kitName, newWinnerElo);
                loserLeaderData.setElo(kitName, newLoserElo);
                winnerLeaderData.setRankedWins(kitName, winnerLeaderData.getRankedWins(kitName) + 1);
                loserLeaderData.setRankedLosses(kitName, loserLeaderData.getRankedLosses(kitName) + 1);

                match.broadcast(Lang.ELO_CHANGES, new TextPlaceholders().set("%winner%", winner.getName()).set("%losser%", losser.getName()).set("%elo-winner%", (newWinnerElo - winnerElo)).set("%new-elo-winner%", newWinnerElo).set("%elo-losser%", (newLoserElo - loserElo)).set("%new-elo-losser%", newLoserElo));
            }

            match.getTeams().forEach(team -> team.players().forEach(player -> {
                if (!match.hasSnapshot(player.getUniqueId())) {
                    match.addSnapshot(player);
                }

                Clickable winnerClickable = new Clickable(Lang.WINNER_MESSAGE.get(player, new TextPlaceholders().set("%player%", winner.getName())), Lang.WINNER_HOVER_MESSAGE.get(player), "/inventory " + match.getSnapshot(player.getUniqueId()).getSnapshotId());
                Clickable loserClickable = new Clickable(Lang.LOSSER_MESSAGE.get(player, new TextPlaceholders().set("%player%", losser.getName())), Lang.LOSSER_HOVER_MESSAGE.get(player), "/inventory " + match.getSnapshot(player.getUniqueId()).getSnapshotId());
                winnerClickable.sendToPlayer(player);
                loserClickable.sendToPlayer(player);
            }));

            for (Snapshot snapshot : match.getSnapshots().values()) {
                this.plugin.getInventoryManager().addSnapshot(snapshot);
            }
        }
    }
    
    @EventHandler
    public void onMove(PlayerMoveEvent e) {
        Player player = e.getPlayer();

        PlayerData playerData = PlayerData.of(player);
        if (playerData == null) {
            return;
        }

        Location to = e.getTo();
        Location from = e.getFrom();

        if (playerData.getPlayerState() == PlayerState.FIGHTING) {
            Match match = this.plugin.getMatchManager().getMatch(playerData);
            if (match == null) {
                return;
            }
            if (match.getKit().isSumo()) {
                if (BlockUtil.isOnLiquid(to, 0) || BlockUtil.isOnLiquid(to, 1)) {
                    this.plugin.getMatchManager().removeFighter(player, playerData, true);
                }
                if ((to.getX() != from.getX() || to.getZ() != from.getZ()) && match.getMatchState() == MatchState.STARTING) {
                    player.teleport(from);

                    //((CraftPlayer)player).getHandle().playerConnection.checkMovement = false;
                }
            }
        }

        CustomEvent event = this.plugin.getEventManager().getEventPlaying(player);
        if (event != null) {
            if (event instanceof Sumo) {
                Sumo sumoEvent = (Sumo)event;
                if (sumoEvent.getPlayer(player.getUniqueId()).getState() == EventPlayer.PlayerState.PREPARING) {
                    player.teleport(from);

                    //((CraftPlayer)player).getHandle().playerConnection.checkMovement = false;
                }
            }
        }
    }
}
