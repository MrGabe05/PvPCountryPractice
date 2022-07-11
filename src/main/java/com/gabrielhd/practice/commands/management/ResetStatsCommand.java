package com.gabrielhd.practice.commands.management;

import com.gabrielhd.practice.Practice;
import com.gabrielhd.practice.kit.Kit;
import com.gabrielhd.practice.lang.Lang;
import com.gabrielhd.practice.player.PlayerData;
import com.gabrielhd.practice.utils.text.TextPlaceholders;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ResetStatsCommand extends Command {
    
    private final Practice plugin;
    
    public ResetStatsCommand() {
        super("reset");

        this.plugin = Practice.getInstance();

        this.setUsage(ChatColor.RED + "Usage: /reset [player]");
    }
    
    @Override
    public boolean execute(CommandSender commandSender, String s, String[] args) {
        if (commandSender instanceof Player) {
            Player player = (Player)commandSender;
            if (!player.hasPermission("pvpcountry.admin")) {
                Lang.NO_PERMISSIONS.send(player);
                return true;
            }

            if (args.length == 0) {
                commandSender.sendMessage(this.getUsage());
                return true;
            }

            Player target = this.plugin.getServer().getPlayer(args[0]);
            if (target == null) {
                Lang.PLAYER_NOT_FOUND.send(target);
                return true;
            }

            PlayerData playerData = PlayerData.of(target);
            for (Kit kit : this.plugin.getKitManager().getKits().values()) {
                playerData.setElo(kit.getName(), 1000);
                playerData.setRankedWins(kit.getName(), 0);
                playerData.setRankedLosses(kit.getName(), 0);
            }

            Lang.PLAYER_STATS_RESET.send(player, new TextPlaceholders().set("%player%", target.getName()));
            return true;
        }

        return false;
    }
}
