package com.gabrielhd.practice.commands.management;

import com.gabrielhd.practice.Practice;
import com.gabrielhd.practice.arena.Arena;
import com.gabrielhd.practice.lang.Lang;
import com.gabrielhd.practice.tasks.ArenaCommandRunnable;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ArenaCommand extends Command {

    private final Practice plugin;
    
    public ArenaCommand() {
        super("arena");

        this.plugin = Practice.getInstance();

        this.setDescription("Arenas command.");
        this.setUsage(ChatColor.RED + "Usage: /arena <subcommand> [args]");
    }
    
    @Override
    public boolean execute(CommandSender sender, String alias, String[] args) {
        if (!(sender instanceof Player)) {
            return true;
        }
        Player player = (Player)sender;
        if (!player.hasPermission("pvpcountry.admin")) {
            Lang.NO_PERMISSIONS.send(player);
            return true;
        }
        if (args.length == 0) {
            this.sendHelp(sender);
            return true;
        }
        Arena arena = this.plugin.getArenaManager().getArena(args[1]);
        switch (args[0].toLowerCase()) {
            case "create": {
                if (arena == null) {
                    this.plugin.getArenaManager().createArena(args[1]);
                    sender.sendMessage(ChatColor.GREEN + "Successfully created arena " + args[1] + ".");
                    return true;
                }
                Lang.ARENA_ALREADY_EXISTS.send(player);
                return true;
            }
            case "delete": {
                if (arena != null) {
                    this.plugin.getArenaManager().deleteArena(args[1]);
                    sender.sendMessage(ChatColor.GREEN + "Successfully deleted arena " + args[1] + ".");
                    return true;
                }
                Lang.ARENA_NOT_EXISTS.send(player);
                return true;
            }
            case "enable":
            case "disable": {
                if(arena != null) {
                    arena.setEnabled(!arena.isEnabled());
                    sender.sendMessage(arena.isEnabled() ? (ChatColor.GREEN + "Successfully enabled arena " + args[1] + ".") : (ChatColor.RED + "Successfully disabled arena " + args[1] + "."));
                    return true;
                }
                Lang.ARENA_NOT_EXISTS.send(player);
                return true;
            }
            case "manage": {
                this.plugin.getArenaManager().openArenaSystemUI(player);
                return true;
            }
            case "a": {
                if (arena != null) {
                    Location location = player.getLocation();
                    if (args.length < 3 || !args[2].equalsIgnoreCase("-e")) {
                        location.setX(location.getBlockX() + 0.5);
                        location.setY(location.getBlockY() + 3.0);
                        location.setZ(location.getBlockZ() + 0.5);
                    }
                    arena.setA(location);
                    sender.sendMessage(ChatColor.GREEN + "Successfully set position A for arena " + args[1] + ".");
                    return true;
                }
                Lang.ARENA_NOT_EXISTS.send(player);
                return true;
            }
            case "b": {
                if (arena != null) {
                    Location location = player.getLocation();
                    if (args.length < 3 || !args[2].equalsIgnoreCase("-e")) {
                        location.setX(location.getBlockX() + 0.5);
                        location.setY(location.getBlockY() + 3.0);
                        location.setZ(location.getBlockZ() + 0.5);
                    }
                    arena.setB(location);
                    sender.sendMessage(ChatColor.GREEN + "Successfully set position B for arena " + args[1] + ".");
                    return true;
                }
                Lang.ARENA_NOT_EXISTS.send(player);
                return true;
            }
            case "max": {
                if (arena != null) {
                    arena.setMax(player.getLocation());
                    sender.sendMessage(ChatColor.GREEN + "Successfully set maximum position for arena " + args[1] + ".");
                    return true;
                }
                Lang.ARENA_NOT_EXISTS.send(player);
                return true;
            }
            case "min": {
                if (arena != null) {
                    arena.setMax(player.getLocation());
                    sender.sendMessage(ChatColor.GREEN + "Successfully set minimum position for arena " + args[1] + ".");
                    return true;
                }
                Lang.ARENA_NOT_EXISTS.send(player);
                return true;
            }
            case "help": {
                this.sendHelp(sender);
                return true;
            }
            case "save": {
                this.plugin.getArenaManager().reloadArenas();

                sender.sendMessage(ChatColor.GREEN + "Successfully reloaded the arenas.");
                return true;
            }
            case "generate": {
                if (args.length == 3) {
                    int arenas = Integer.parseInt(args[2]);
                    this.plugin.getServer().getScheduler().runTask(this.plugin, new ArenaCommandRunnable(this.plugin, arena, arenas));
                    this.plugin.getArenaManager().setGeneratingArenaRunnables(this.plugin.getArenaManager().getGeneratingArenaRunnables() + 1);
                    return true;
                }
                sender.sendMessage(ChatColor.RED + "Usage: /arena generate <arena> <arenas>");
                return true;
            }
            default:
                this.sendHelp(sender);
                return true;
        }
    }

    public void sendHelp(CommandSender sender) {
        sender.sendMessage("");
        sender.sendMessage(ChatColor.RED + "Arena Help");
        sender.sendMessage("");
        sender.sendMessage(ChatColor.RED + "/arena create <name> - Create Arena");
        sender.sendMessage(ChatColor.RED + "/arena delete <name> - Delete Arena");
        sender.sendMessage(ChatColor.RED + "/arena a <name> - Set A position for Arena");
        sender.sendMessage(ChatColor.RED + "/arena b <name> - Set B position for Arena");
        sender.sendMessage(ChatColor.RED + "/arena min <name> - Set bottom corner location");
        sender.sendMessage(ChatColor.RED + "/arena max <name> - Set top corner location");
        sender.sendMessage(ChatColor.RED + "/arena enable <name> - Enable Arena");
        sender.sendMessage(ChatColor.RED + "/arena disable <name> - Disable Arena");
        sender.sendMessage(ChatColor.RED + "/arena generate <arena> <arenas amount> - Generate Arena");
        sender.sendMessage(ChatColor.RED + "/arena save - Save all arenas");
        sender.sendMessage(ChatColor.RED + "/arena manage - Open Arena GUI");
        sender.sendMessage("");
    }
}
