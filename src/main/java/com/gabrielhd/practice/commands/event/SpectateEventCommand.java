package com.gabrielhd.practice.commands.event;

import com.gabrielhd.practice.Practice;
import com.gabrielhd.practice.events.CustomEvent;
import com.gabrielhd.practice.events.EventState;
import com.gabrielhd.practice.events.types.Sumo;
import com.gabrielhd.practice.party.Party;
import com.gabrielhd.practice.player.PlayerData;
import com.gabrielhd.practice.player.PlayerState;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;

public class SpectateEventCommand extends Command {

    private final Practice plugin;
    
    public SpectateEventCommand() {
        super("eventspectate");

        this.plugin = Practice.getInstance();

        this.setDescription("Spectate an event.");
        this.setUsage(ChatColor.RED + "Usage: /eventspectate <event>");
        this.setAliases(Arrays.asList("eventspec", "specevent"));
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
            player.sendMessage(ChatColor.RED + "Cannot execute this command in your current state.");
            return true;
        }
        CustomEvent event = this.plugin.getEventManager().getByName(args[0]);
        if (event == null) {
            player.sendMessage(ChatColor.RED + "That player is currently not in an event.");
            return true;
        }
        if (event.getState() != EventState.STARTED) {
            player.sendMessage(ChatColor.RED + "That event hasn't started, please wait.");
            return true;
        }
        if (playerData.getPlayerState() == PlayerState.SPECTATING) {
            if (this.plugin.getEventManager().getSpectators().containsKey(player.getUniqueId())) {
                player.sendMessage(ChatColor.RED + "You are already spectating this event.");
                return true;
            }
            this.plugin.getEventManager().removeSpectator(player);
        }
        player.sendMessage(ChatColor.GREEN + "You are now spectating " + ChatColor.GRAY + event.getName() + " Event" + ChatColor.GREEN + ".");
        if (event instanceof Sumo) {
            this.plugin.getEventManager().addSpectatorSumo(player, playerData, (Sumo) event);
        }
        return true;
    }
}
