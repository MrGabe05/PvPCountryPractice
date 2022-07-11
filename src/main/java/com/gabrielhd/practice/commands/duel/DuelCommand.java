package com.gabrielhd.practice.commands.duel;

import com.gabrielhd.practice.Practice;
import com.gabrielhd.practice.lang.Lang;
import com.gabrielhd.practice.party.Party;
import com.gabrielhd.practice.player.PlayerData;
import com.gabrielhd.practice.player.PlayerState;
import com.gabrielhd.practice.utils.text.TextPlaceholders;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class DuelCommand extends Command {

    private final Practice plugin;
    
    public DuelCommand() {
        super("duel");

        this.plugin = Practice.getInstance();

        this.setDescription("Duel a player");
        this.setUsage(ChatColor.RED + "Usage: /duel <player>");
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
            Lang.COMMAND_NOT_AVAILABLE.send(player);
            return true;
        }
        Player target = this.plugin.getServer().getPlayer(args[0]);
        if (target == null) {
            Lang.PLAYER_NOT_FOUND.send(player, new TextPlaceholders().set("%player%", args[0]));
            return true;
        }
        Party party = Party.of(player.getUniqueId());
        Party targetParty = Party.of(target.getUniqueId());
        if (player.getName().equals(target.getName())) {
            Lang.NOT_FIGHT_YOURSELF.send(player);
            return true;
        }
        if (party != null && !party.isLeader(player)) {
            Lang.PARTY_NOT_LEADER.send(player);
            return true;
        }
        if (targetParty != null && party == targetParty) {
            Lang.NOT_FIGHT_YOURSELF.send(player);
            return true;
        }
        PlayerData targetData = PlayerData.of(target);
        if (targetData.getPlayerState() != PlayerState.SPAWN) {
            Lang.PLAYER_BUSY_OTHER.send(player, new TextPlaceholders().set("%player%", target.getName()));
            return true;
        }
        if (!targetData.getOptions().isDuelRequests()) {
            Lang.PLAYER_IGNORING_REQUESTS.send(player);
            return true;
        }
        if (party == null && targetParty != null) {
            Lang.PARTY_ALREADY_IN.send(player, new TextPlaceholders().set("%player%", target.getName()));
            return true;
        }
        if (party != null && targetParty == null) {
            Lang.PARTY_ALREADY.send(player);
            return true;
        }
        playerData.setDuelSelecting(target.getUniqueId());
        player.openInventory(this.plugin.getInventoryManager().getDuelInventory().getCurrentPage());
        return true;
    }
}
