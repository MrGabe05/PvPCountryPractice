package com.gabrielhd.practice.commands.management;

import com.gabrielhd.practice.Practice;
import com.gabrielhd.practice.lang.Lang;
import com.gabrielhd.practice.utils.text.Color;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SpawnsCommand extends Command {

    private final Practice plugin;
    
    public SpawnsCommand() {
        super("spawn");
        this.plugin = Practice.getInstance();
        this.setDescription("Spawn command.");
        this.setUsage(ChatColor.RED + "Usage: /spawn <subcommand>");
    }
    
    @Override
    public boolean execute(final CommandSender sender, final String alias, final String[] args) {
        if (!(sender instanceof Player)) {
            return true;
        }
        Player player = (Player)sender;
        if (!player.hasPermission("pvpcountry.admin")) {
            Lang.NO_PERMISSIONS.send(player);
            return true;
        }
        if (args.length < 1) {
            this.sendHelp(sender);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "remove": {
                if(Practice.getInstance().getSpawnsLocation().isEmpty()) {
                    break;
                }
                Practice.getInstance().getSpawnsLocation().remove((Practice.getInstance().getSpawnsLocation().size() - 1));

                Lang.SPAWN_REMOVED.send(player);
                break;
            }
            case "max": {
                Practice.getInstance().setRegionSpawnMax(player.getLocation());

                Lang.SPAWN_MAX.send(player);
                break;
            }
            case "min": {
                Practice.getInstance().setRegionSpawnMin(player.getLocation());

                Lang.SPAWN_MIN.send(player);
                break;
            }
            case "add":
            default: {
                Practice.getInstance().getSpawnsLocation().add(player.getLocation());

                Lang.SPAWN_ADDED.send(player);
                break;
            }
        }
        return false;
    }

    public void sendHelp(CommandSender sender) {
        sender.sendMessage(Color.text("&c/spawn add &8- Add a new main spawn."));
        sender.sendMessage(Color.text("&c/spawn remove &8- Remove last added spawn."));
        sender.sendMessage(Color.text("&c/spawn max &8- You set the max location of the spawn."));
        sender.sendMessage(Color.text("&c/spawn min &8- You set the min location of the spawn."));
    }
}
