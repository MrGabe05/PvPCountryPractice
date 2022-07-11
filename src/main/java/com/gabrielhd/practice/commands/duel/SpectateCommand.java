package com.gabrielhd.practice.commands.duel;

import com.gabrielhd.practice.Practice;
import com.gabrielhd.practice.events.CustomEvent;
import com.gabrielhd.practice.events.types.Sumo;
import com.gabrielhd.practice.lang.Lang;
import com.gabrielhd.practice.match.Match;
import com.gabrielhd.practice.match.MatchTeam;
import com.gabrielhd.practice.party.Party;
import com.gabrielhd.practice.player.PlayerData;
import com.gabrielhd.practice.player.PlayerState;
import com.gabrielhd.practice.utils.text.TextPlaceholders;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;

public class SpectateCommand extends Command
{
    private final Practice plugin;
    
    public SpectateCommand() {
        super("spectate");

        this.plugin = Practice.getInstance();

        this.setDescription("Spectate single player combat.");
        this.setUsage(ChatColor.RED + "Usage: /spectate <player>");
        this.setAliases(Collections.singletonList("spec"));
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
        Party party = Party.of(player.getUniqueId());
        if (party != null || (playerData.getPlayerState() != PlayerState.SPAWN && playerData.getPlayerState() != PlayerState.SPECTATING)) {
            Lang.COMMAND_NOT_AVAILABLE.send(player);
            return true;
        }
        Player target = this.plugin.getServer().getPlayer(args[0]);
        if (target == null) {
            Lang.PLAYER_NOT_FOUND.send(player, new TextPlaceholders().set("%player%", args[0]));
            return true;
        }
        PlayerData targetData = PlayerData.of(target);
        if (targetData.getPlayerState() == PlayerState.EVENT) {
            CustomEvent event = this.plugin.getEventManager().getEventPlaying(target);
            if (event == null) {
                Lang.PLAYER_NOT_IN_EVENT.send(player);
                return true;
            }
            if (event instanceof Sumo) {
                player.performCommand("eventspectate Sumo");
            }
        } else {
            if (targetData.getPlayerState() != PlayerState.FIGHTING) {
                Lang.PLAYER_NOT_IN_GAME.send(player);
                return true;
            }
            Match targetMatch = this.plugin.getMatchManager().getMatch(targetData);
            if (!targetMatch.isParty()) {
                if (!targetData.getOptions().isSpectators() && !player.hasPermission("pvpcountry.ignoring.bypass")) {
                    Lang.PLAYER_IGNORING_SPECS.send(player);
                    return true;
                }
                MatchTeam team = targetMatch.getTeams().get(0);
                MatchTeam team2 = targetMatch.getTeams().get(1);
                PlayerData otherPlayerData = PlayerData.of((team.getPlayers().get(0) == target.getUniqueId()) ? team2.getPlayers().get(0) : team.getPlayers().get(0));
                if (otherPlayerData != null && !otherPlayerData.getOptions().isSpectators() && !player.hasPermission("pvpcountry.ignoring.bypass")) {
                    Lang.PLAYER_IGNORING_SPECS.send(player);
                    return true;
                }
            }
            if (playerData.getPlayerState() == PlayerState.SPECTATING) {
                Match match = this.plugin.getMatchManager().getSpectatingMatch(player.getUniqueId());
                if (match.equals(targetMatch)) {
                    Lang.ALREADY_SPECTATING.send(player);
                    return true;
                }
                match.removeSpectator(player.getUniqueId());
            }
            Lang.NOW_SPECTATING.send(player, new TextPlaceholders().set("%player%", target.getName()));

            this.plugin.getMatchManager().addSpectator(player, playerData, target, targetMatch);
        }
        return true;
    }
}
