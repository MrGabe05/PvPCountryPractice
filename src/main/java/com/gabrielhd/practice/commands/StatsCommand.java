package com.gabrielhd.practice.commands;

import com.gabrielhd.practice.Practice;
import com.gabrielhd.practice.kit.Kit;
import com.gabrielhd.practice.lang.Lang;
import com.gabrielhd.practice.player.PlayerData;
import com.gabrielhd.practice.utils.text.TextPlaceholders;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.Locale;

public class StatsCommand extends Command {

    private final Practice plugin;
    
    public StatsCommand() {
        super("stats");

        this.plugin = Practice.getInstance();

        this.setAliases(Arrays.asList("elo", "statistics"));
        this.setUsage(ChatColor.RED + "Use: /stats [player]");
    }
    
    @Override
    public boolean execute(final CommandSender sender, final String s, final String[] args) {
        if(!(sender instanceof Player)) {
            return true;
        }
        Player player = (Player)sender;
        if (args.length == 0) {
            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> this.getStats(player, player));
            return true;
        }
        Player target = this.plugin.getServer().getPlayer(args[0]);
        if (target == null) {
            Lang.PLAYER_NOT_FOUND.send(player, new TextPlaceholders().set("%player%", args[0]));
            return true;
        }
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> this.getStats(player, target));
        return true;
    }
    
    public void getStats(Player send, Player stats) {
        PlayerData playerData = PlayerData.get(stats.getUniqueId());
        if(playerData != null) {
            TextPlaceholders textPlaceholders = new TextPlaceholders();
            textPlaceholders.set("%player%", stats.getName());
            textPlaceholders.set("%elo%", playerData.getElo());
            textPlaceholders.set("%ranked-wins%", playerData.getRankedWins());
            textPlaceholders.set("%ranked-losses%", playerData.getRankedLosses());
            textPlaceholders.set("%unranked-wins%", playerData.getUnrankedWins());
            textPlaceholders.set("%unranked-losses%", playerData.getUnrankedLosses());

            for (Kit kit : this.plugin.getKitManager().getKits().values()) {
                textPlaceholders.set("%kit%", kit.getName());
                textPlaceholders.set("%elo-" + kit.getName().toLowerCase(Locale.ROOT) + "%", playerData.getElo(kit.getName()));
                textPlaceholders.set("%ranked-wins-" + kit.getName().toLowerCase(Locale.ROOT) + "%", playerData.getRankedWins(kit.getName()));
                textPlaceholders.set("%ranked-losses-" + kit.getName().toLowerCase(Locale.ROOT) + "%", playerData.getRankedLosses(kit.getName()));
                textPlaceholders.set("%unranked-wins-" + kit.getName().toLowerCase(Locale.ROOT) + "%", playerData.getUnrankedWins(kit.getName()));
                textPlaceholders.set("%unranked-losses-" + kit.getName().toLowerCase(Locale.ROOT) + "%", playerData.getUnrankedLosses(kit.getName()));
            }

            Lang.PLAYER_STATS.send(send, textPlaceholders);
        }
    }
}
