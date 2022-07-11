package com.gabrielhd.practice.commands.duel;

import com.gabrielhd.practice.Practice;
import com.gabrielhd.practice.kit.Kit;
import com.gabrielhd.practice.lang.Lang;
import com.gabrielhd.practice.match.Match;
import com.gabrielhd.practice.match.MatchRequest;
import com.gabrielhd.practice.match.MatchTeam;
import com.gabrielhd.practice.party.Party;
import com.gabrielhd.practice.player.PlayerData;
import com.gabrielhd.practice.player.PlayerState;
import com.gabrielhd.practice.queue.QueueType;
import com.gabrielhd.practice.utils.text.TextPlaceholders;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class AcceptCommand extends Command {

    private final Practice plugin;
    
    public AcceptCommand() {
        super("accept");
        this.plugin = Practice.getInstance();

        this.setDescription("You accept a duel from a player.");
        this.setUsage(ChatColor.RED + "Usage: /accept <player>");
    }
    
    @Override
    public boolean execute(CommandSender sender, String alias, String[] args) {
        if (!(sender instanceof Player)) {
            return true;
        }
        Player player = (Player)sender;
        if (args.length < 1) {
            player.sendMessage(this.usageMessage);
            return true;
        }
        PlayerData playerData = PlayerData.of(player);
        if (playerData.getPlayerState() != PlayerState.SPAWN) {
            Lang.PLAYER_BUSY.send(player);
            return true;
        }
        Player target = this.plugin.getServer().getPlayer(args[0]);
        if (target == null) {
            Lang.PLAYER_NOT_FOUND.send(player, new TextPlaceholders().set("%player%", args[0]));
            return true;
        }
        if (player.getName().equals(target.getName())) {
            Lang.NOT_FIGHT_YOURSELF.send(player);
            return true;
        }
        PlayerData targetData = PlayerData.of(target);
        if (targetData.getPlayerState() != PlayerState.SPAWN) {
            Lang.PLAYER_BUSY_OTHER.send(player, new TextPlaceholders().set("%player%", target.getName()));
            return true;
        }
        MatchRequest request = this.plugin.getMatchManager().getMatchRequest(target.getUniqueId(), player.getUniqueId());
        if (args.length > 1) {
            Kit kit = this.plugin.getKitManager().getKit(args[1]);
            if (kit != null) {
                request = this.plugin.getMatchManager().getMatchRequest(target.getUniqueId(), player.getUniqueId(), kit.getName());
            }
        }
        if (request == null) {
            Lang.NOT_DUEL_REQUESTS.send(player);
            return true;
        }
        if (request.getRequester().equals(target.getUniqueId())) {
            List<UUID> playersA = new ArrayList<>();
            List<UUID> playersB = new ArrayList<>();
            Party party = Party.of(player.getUniqueId());
            Party targetParty = Party.of(target.getUniqueId());
            if (request.isParty()) {
                if (party == null || targetParty == null || !party.isLeader(player) || !party.isLeader(target)) {
                    Lang.PARTY_NOT_LEADER.send(player);
                    return true;
                }
                playersA.addAll(party.getMembers());
                playersB.addAll(targetParty.getMembers());
            } else {
                if (party != null || targetParty != null) {
                    Lang.PLAYER_ALREADY_IN_PARTY.send(player);
                    return true;
                }
                playersA.add(player.getUniqueId());
                playersB.add(target.getUniqueId());
            }
            Kit kit2 = this.plugin.getKitManager().getKit(request.getKitName());
            MatchTeam teamA = new MatchTeam(target.getUniqueId(), playersB, 0);
            MatchTeam teamB = new MatchTeam(player.getUniqueId(), playersA, 1);
            Match match = new Match(request.getArena(), kit2, QueueType.UNRANKED, teamA, teamB);
            Player leaderA = this.plugin.getServer().getPlayer(teamA.getLeader());
            Player leaderB = this.plugin.getServer().getPlayer(teamB.getLeader());
            String teamMatch = match.isPartyMatch() ? " Party" : "";

            match.broadcast(Lang.STARTING_DUEL, new TextPlaceholders().set("%player-1%", leaderA.getName() + teamMatch).set("%player-2%", leaderB.getName() + teamMatch));
            this.plugin.getMatchManager().createMatch(match);
        }
        return true;
    }
}
