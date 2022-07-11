package com.gabrielhd.practice.commands.management;

import com.gabrielhd.practice.Practice;
import com.gabrielhd.practice.arena.Arena;
import com.gabrielhd.practice.kit.Kit;
import com.gabrielhd.practice.lang.Lang;
import com.gabrielhd.practice.utils.items.ItemUtil;
import com.gabrielhd.practice.utils.text.TextPlaceholders;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Locale;

public class KitCommand extends Command {

    private final Practice plugin;
    
    public KitCommand() {
        super("kit");
        
        this.plugin = Practice.getInstance();
        
        this.setDescription("Kit command.");
        this.setUsage(ChatColor.RED + "Usage: /kit <subcommand> [args]");
    }
    
    @Override
    public boolean execute(CommandSender sender, String alias, String[] args) {
        if (!(sender instanceof Player)) {
            return true;
        }
        Player player = (Player)sender;
        if (!player.hasPermission("practice.admin")) {
            Lang.NO_PERMISSIONS.send(player);
            return true;
        }
        if (args.length < 2) {
            this.sendHelp(sender);
            return true;
        }

        Kit kit = this.plugin.getKitManager().getKit(args[1]);
        switch (args[0].toLowerCase()) {
            case "whitelistarena": {
                if (args.length < 3) {
                    this.sendHelp(sender);
                    return true;
                }
                if (kit == null) {
                    Lang.KIT_NOT_EXISTS.send(player, new TextPlaceholders().set("%kit%", kit.getName()));
                    return true;
                }

                Arena arena = this.plugin.getArenaManager().getArena(args[2]);
                if (arena != null) {
                    String name = arena.getName().toLowerCase(Locale.ROOT);

                    if(!kit.getWhitelistArenas().remove(name)) {
                        kit.getWhitelistArenas().add(name);
                    }

                    sender.sendMessage(kit.getWhitelistArenas().contains(arena.getName()) ? Lang.ARENA_WHITELISTED.get(player, new TextPlaceholders().set("%kit%", kit.getName()).set("%arena%", arena.getName())) : Lang.ARENA_NOT_WHITELISTED.get(player, new TextPlaceholders().set("%kit%", kit.getName()).set("%arena%", arena.getName())));
                    return true;
                }
                Lang.ARENA_NOT_EXISTS.send(player);
                return true;
            }
            case "excludearena": {
                if (args.length < 3) {
                    this.sendHelp(sender);
                    return true;
                }
                if (kit == null) {
                    Lang.KIT_NOT_EXISTS.send(player, new TextPlaceholders().set("%kit%", kit.getName()));
                    return true;
                }

                Arena arena = this.plugin.getArenaManager().getArena(args[2]);
                if (arena != null) {
                    String name = arena.getName().toLowerCase(Locale.ROOT);

                    if(!kit.getBlacklistArenas().remove(name)) {
                        kit.getBlacklistArenas().add(name);
                    }

                    sender.sendMessage(kit.getBlacklistArenas().contains(arena.getName()) ? Lang.ARENA_BLACKLISTED.get(player, new TextPlaceholders().set("%kit%", kit.getName()).set("%arena%", arena.getName())) : Lang.ARENA_NOT_BLACKLISTED.get(player, new TextPlaceholders().set("%kit%", kit.getName()).set("%arena%", arena.getName())));
                    return true;
                }
                Lang.ARENA_NOT_EXISTS.send(player);
                return true;
            }
            case "create": {
                if (kit == null) {
                    this.plugin.getKitManager().createKit(args[1]);
                    Lang.KIT_CREATED.send(player, new TextPlaceholders().set("%kit%", args[1]));
                    return true;
                }
                Lang.KIT_ALREADY_EXISTS.send(player);
                return true;
            }
            case "delete": {
                if (kit != null) {
                    this.plugin.getKitManager().deleteKit(args[1]);
                    Lang.KIT_DELETED.send(player, new TextPlaceholders().set("%kit%", kit.getName()));
                    return true;
                }
                Lang.KIT_NOT_EXISTS.send(player);
                return true;
            }
            case "disable":
            case "enable": {
                if (kit != null) {
                    kit.setEnabled(!kit.isEnabled());

                    sender.sendMessage(kit.isEnabled() ? Lang.KIT_ENABLED.get(player, new TextPlaceholders().set("%kit%", kit.getName())) : Lang.KIT_DISABLED.get(player, new TextPlaceholders().set("%kit%", kit.getName())));
                    return true;
                }
                break;
            }
            case "ranked": {
                if (kit != null) {
                    kit.setRanked(!kit.isRanked());

                    sender.sendMessage(kit.isRanked() ? Lang.KIT_RANKED_ENABLED.get(player, new TextPlaceholders().set("%kit%", kit.getName())) : Lang.KIT_RANKED_DISABLED.get(player, new TextPlaceholders().set("%kit%", kit.getName())));
                    return true;
                }
                Lang.KIT_NOT_EXISTS.send(player);
                return true;
            }
            case "getinv": {
                if (kit == null) {
                    Lang.KIT_NOT_EXISTS.send(player);
                    return true;
                }

                kit.applyToPlayer(player);

                Lang.KIT_GET_INVENTORY.send(player, new TextPlaceholders().set("%kit%", kit.getName()));
                return true;
            }
            case "setinv": {
                if (kit == null) {
                    Lang.KIT_NOT_EXISTS.send(player);
                    return true;
                }
                player.updateInventory();

                kit.setContents(player.getInventory().getContents());
                kit.setArmor(player.getInventory().getArmorContents());

                Lang.KIT_SET_INVENTORY.send(player, new TextPlaceholders().set("%kit%", kit.getName()));
                return true;
            }
            case "spleef": {
                if (kit != null) {
                    kit.setSpleef(!kit.isSpleef());
                    sender.sendMessage(kit.isSpleef() ? (ChatColor.GREEN + "Successfully enabled spleef mode for kit " + args[1] + ".") : (ChatColor.RED + "Successfully disabled spleef mode for kit " + args[1] + "."));
                    return true;
                }
                Lang.KIT_NOT_EXISTS.send(player);
                return true;
            }
            case "parkour": {
                if (kit != null) {
                    kit.setParkour(!kit.isParkour());
                    sender.sendMessage(kit.isParkour() ? (ChatColor.GREEN + "Successfully enabled parkour mode for kit " + args[1] + ".") : (ChatColor.RED + "Successfully disabled parkour mode for kit " + args[1] + "."));
                    return true;
                }
                Lang.KIT_NOT_EXISTS.send(player);
                return true;
            }
            case "help": {
                this.sendHelp(sender);
                return true;
            }
            case "icon": {
                if (kit == null) {
                    Lang.KIT_NOT_EXISTS.send(player);
                    return true;
                }
                if (player.getItemInHand().getType() != Material.AIR) {
                    ItemStack icon = ItemUtil.renameItem(player.getItemInHand().clone(), ChatColor.GREEN + kit.getName());
                    kit.setIcon(icon);

                    Lang.KIT_SET_ICON.send(player, new TextPlaceholders().set("%kit%", kit.getName()));
                    return true;
                }
                player.sendMessage(ChatColor.RED + "You must be holding an item to set the kit icon!");
                return true;
            }
            case "sumo": {
                if (kit != null) {
                    kit.setSumo(!kit.isSumo());
                    sender.sendMessage(kit.isSumo() ? (ChatColor.GREEN + "Successfully enabled sumo mode for kit " + args[1] + ".") : (ChatColor.RED + "Successfully disabled sumo mode for kit " + args[1] + "."));
                    return true;
                }
                Lang.KIT_NOT_EXISTS.send(player);
                return true;
            }
            case "build": {
                if (kit != null) {
                    kit.setBuild(!kit.isBuild());
                    sender.sendMessage(kit.isBuild() ? (ChatColor.GREEN + "Successfully enabled build mode for kit " + args[1] + ".") : (ChatColor.RED + "Successfully disabled build mode for kit " + args[1] + "."));
                    return true;
                }
                Lang.KIT_NOT_EXISTS.send(player);
                return true;
            }
            case "combo": {
                if (kit != null) {
                    kit.setCombo(!kit.isCombo());
                    sender.sendMessage(kit.isCombo() ? (ChatColor.GREEN + "Successfully enabled combo mode for kit " + args[1] + ".") : (ChatColor.RED + "Successfully disabled combo mode for kit " + args[1] + "."));
                    return true;
                }
                Lang.KIT_NOT_EXISTS.send(player);
                return true;
            }
            default:
                break;
        }

        this.sendHelp(sender);
        return true;
    }

    public void sendHelp(CommandSender sender) {
        sender.sendMessage("");
        sender.sendMessage(ChatColor.RED + "Kit Help");
        sender.sendMessage("");
        sender.sendMessage(ChatColor.RED + "/kit create <name> - Create Kit");
        sender.sendMessage(ChatColor.RED + "/kit delete <name> - Delete Kit");
        sender.sendMessage(ChatColor.RED + "/kit enable <name> - Enable Kit");
        sender.sendMessage(ChatColor.RED + "/kit disable <name> - Disable Kit");
        sender.sendMessage(ChatColor.RED + "/kit combo <name> - Combo Mode Kit");
        sender.sendMessage(ChatColor.RED + "/kit build <name> - Build Mode Kit");
        sender.sendMessage(ChatColor.RED + "/kit sumo <name> - Sumo Mode Kit");
        sender.sendMessage(ChatColor.RED + "/kit spleef <name> - Spleef Mode Kit");
        sender.sendMessage(ChatColor.RED + "/kit parkour <name> - Parkour Mode Kit");
        sender.sendMessage(ChatColor.RED + "/kit ranked <name> - Enable ranked matchs for Kit");
        sender.sendMessage(ChatColor.RED + "/kit whitelistarena <name> <arena name> - Whitelist arena for Kit");
        sender.sendMessage(ChatColor.RED + "/kit excludearena <name> <arena name> - Whitelist arena for Kit");
        sender.sendMessage(ChatColor.RED + "/kit icon <name> - Set Icon for Kit");
        sender.sendMessage(ChatColor.RED + "/kit setinv <name> - Set Inventory for Kit");
        sender.sendMessage(ChatColor.RED + "/kit getinv <name> - get Inventory from Kit");
        sender.sendMessage("");
    }
}
