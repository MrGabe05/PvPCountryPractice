package com.gabrielhd.practice.commands.time;

import com.gabrielhd.practice.player.PlayerData;
import com.gabrielhd.practice.settings.item.ProfileOptionsItemState;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class NightCommand extends Command
{
    public NightCommand() {
        super("night");
        this.setDescription("Set player time to night.");
        this.setUsage(ChatColor.RED + "Usage: /night");
    }
    
    @Override
    public boolean execute(CommandSender sender, String alias, String[] args) {
        if (!(sender instanceof Player)) {
            return true;
        }
        ((Player)sender).setPlayerTime(18000L, true);
        PlayerData playerData = PlayerData.of((Player)sender);
        playerData.getOptions().setTime(ProfileOptionsItemState.NIGHT);
        return true;
    }
}
