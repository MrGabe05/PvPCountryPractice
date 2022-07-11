package com.gabrielhd.practice.commands;

import com.gabrielhd.practice.Practice;
import com.gabrielhd.practice.lang.Lang;
import com.gabrielhd.practice.player.PlayerData;
import com.gabrielhd.practice.utils.text.TextPlaceholders;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;

public class RankedsCommand extends Command {
    
    private final Practice plugin;
    
    public RankedsCommand() {
        super("rankeds");

        this.plugin = Practice.getInstance();

        this.setDescription("Ranked Command.");
        this.setPermission("pvpcountry.command.rankeds");
        this.setAliases(Collections.singletonList("ranks"));
        this.setUsage(ChatColor.RED + "Use: /rankeds give <player> <amount>");
    }

    @Override
    public boolean execute(CommandSender cs, String string, String[] args) {
        if((cs instanceof Player)) {
            Player player = (Player) cs;

            if(args.length != 3) {
                cs.sendMessage(this.getUsage());
                return true;
            }

            if(args[0].equalsIgnoreCase("give")) {
                Player target = Bukkit.getPlayer(args[1]);

                if(target == null) {
                    Lang.PLAYER_NOT_FOUND.send(player, new TextPlaceholders().set("%player%", args[1]));
                    return true;
                }
                PlayerData playerData = PlayerData.of(target);

                int amount = Integer.parseInt(args[2]);
                long rankeds = playerData.getRankeds() + amount;

                playerData.setRankeds(rankeds);
                Lang.GIVED_RANKEDS.send(player, new TextPlaceholders().set("%player%", target.getName()).set("%amount%", amount));
                return true;
            }
            cs.sendMessage(this.getUsage());
            return true;
        }
        return false;
    }
    
}
