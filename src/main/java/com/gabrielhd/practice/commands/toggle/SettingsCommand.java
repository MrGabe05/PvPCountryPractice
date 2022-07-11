package com.gabrielhd.practice.commands.toggle;

import com.gabrielhd.practice.Practice;
import com.gabrielhd.practice.player.PlayerData;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;

public class SettingsCommand extends Command {

    private final Practice plugin;
    
    public SettingsCommand() {
        super("settings");
        this.plugin = Practice.getInstance();

        this.setUsage(ChatColor.RED + "Use: /settings");
        this.setAliases(Arrays.asList("options", "toggle"));
    }
    
    @Override
    public boolean execute(CommandSender sender, String alias, String[] args) {
        if (!(sender instanceof Player)) {
            return true;
        }
        Player player = (Player)sender;
        PlayerData playerData = PlayerData.of(player);
        player.openInventory(playerData.getOptions().getInventory());
        return true;
    }
}
