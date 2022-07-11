package com.gabrielhd.practice.commands.event;

import com.gabrielhd.practice.Practice;
import com.gabrielhd.practice.events.CustomEvent;
import com.gabrielhd.practice.events.EventState;
import com.gabrielhd.practice.lang.Lang;
import com.gabrielhd.practice.menus.Menu;
import com.gabrielhd.practice.utils.text.Clickable;
import com.gabrielhd.practice.utils.text.TextPlaceholders;
import org.apache.commons.lang.math.NumberUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Objects;

public class HostCommand extends Command {

    private final Practice plugin;
    
    public HostCommand() {
        super("host");

        this.plugin = Practice.getInstance();

        this.setDescription("Hostea un Evento");
        this.setUsage(ChatColor.RED + "Usa: /host <event>");
    }
    
    @Override
    public boolean execute(CommandSender sender, String alias, String[] args) {
        if (!(sender instanceof Player)) {
            return true;
        }
        Player player = (Player)sender;
        if (!player.hasPermission("pvpcountry.host")) {
            Lang.NO_PERMISSIONS.send(player);
            return true;
        }
        if (args.length < 1) {
            player.openInventory(Objects.requireNonNull(Menu.getPlayerMenu(player, "host")).getInv());
            return true;
        }
        String eventName = args[0];
        if (eventName == null) {
            return true;
        }
        if (this.plugin.getEventManager().getByName(eventName) == null) {
            Lang.EVENT_NOT_EXISTS.send(player);
            Lang.EVENT_AVAIABLES.send(player, new TextPlaceholders().set("%events%", this.plugin.getEventManager().getEvents().values().stream().findFirst().orElse(null).getName()));
            return true;
        }
        if (System.currentTimeMillis() < this.plugin.getEventManager().getCooldown()) {
            Lang.EVENT_COOLDOWN.send(player);
            return true;
        }
        CustomEvent event = this.plugin.getEventManager().getByName(eventName);
        if (event.getState() != EventState.UNANNOUNCED) {
            Lang.EVENT_ALREADY_STARTED.send(player);
            return true;
        }
        boolean eventBeingHosted = this.plugin.getEventManager().getEvents().values().stream().anyMatch(e -> e.getState() != EventState.UNANNOUNCED);
        if (eventBeingHosted) {
            Lang.EVENT_ALREADY_STARTED.send(player);
            return true;
        }

        this.plugin.getServer().getOnlinePlayers().forEach(online -> {
            Clickable message = new Clickable(Lang.EVENT_STARTING.get(online, new TextPlaceholders().set("%host%", player.getName()).set("%event%", event.getName())), Lang.EVENT_STARTING_HOVER.get(player), "/join " + event.getName());
        });
        event.setMaxPlayers(55);
        if (args.length == 2 && player.hasPermission("pvpcountry.host.unlimited")) {
            if (!NumberUtils.isNumber(args[1])) {
                player.sendMessage(ChatColor.RED + "That's not a correct amount.");
                return true;
            }
            event.setMaxPlayers(Integer.parseInt(args[1]));
        }
        Practice.getInstance().getEventManager().hostEvent(event, player);

        event.joinEvent(player);
        return true;
    }
}
