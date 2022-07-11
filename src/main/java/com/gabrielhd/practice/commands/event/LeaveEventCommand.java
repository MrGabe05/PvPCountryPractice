package com.gabrielhd.practice.commands.event;

import com.gabrielhd.practice.Practice;
import com.gabrielhd.practice.events.CustomEvent;
import com.gabrielhd.practice.lang.Lang;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class LeaveEventCommand extends Command {

    private final Practice plugin;
    
    public LeaveEventCommand() {
        super("leave");

        this.plugin = Practice.getInstance();

        this.setDescription("Leave an event or tournament.");
        this.setUsage(ChatColor.RED + "Usage: /leave");
    }
    
    @Override
    public boolean execute(CommandSender sender, String alias, String[] args) {
        if (!(sender instanceof Player)) {
            return true;
        }
        Player player = (Player)sender;
        boolean inEvent = this.plugin.getEventManager().getEventPlaying(player) != null;
        if (inEvent) {
            this.leaveEvent(player);
        }
        else {
            player.sendMessage(ChatColor.RED + "There is nothing to leave.");
        }
        return true;
    }
    
    private void leaveEvent(Player player) {
        CustomEvent event = this.plugin.getEventManager().getEventPlaying(player);
        if (event == null) {
            Lang.EVENT_NOT_EXISTS.send(player);
            return;
        }
        if (!this.plugin.getEventManager().isPlaying(player, event)) {
            player.sendMessage(ChatColor.RED + "You are not in an event.");
            return;
        }
        event.leave(player);
    }
}
