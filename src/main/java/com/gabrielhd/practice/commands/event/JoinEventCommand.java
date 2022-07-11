package com.gabrielhd.practice.commands.event;

import com.gabrielhd.practice.Practice;
import com.gabrielhd.practice.events.CustomEvent;
import com.gabrielhd.practice.events.EventState;
import com.gabrielhd.practice.lang.Lang;
import com.gabrielhd.practice.party.Party;
import com.gabrielhd.practice.player.PlayerData;
import com.gabrielhd.practice.player.PlayerState;
import org.apache.commons.lang.math.NumberUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class JoinEventCommand extends Command {
    private final Practice plugin;
    
    public JoinEventCommand() {
        super("join");

        this.plugin = Practice.getInstance();

        this.setDescription("Únete a un evento o torneo.");
        this.setUsage(ChatColor.RED + "Usa: /join <id>");
    }
    
    @Override
    public boolean execute(CommandSender sender, String alias, String[] args) {
        if (!(sender instanceof Player)) {
            return true;
        }
        Player player = (Player)sender;
        if (args.length < 1) {
            player.sendMessage(this.usageMessage);
            return true;
        }

        PlayerData playerData = PlayerData.of(player);

        if (Party.of(player.getUniqueId()) != null || playerData.getPlayerState() != PlayerState.SPAWN) {
            Lang.COMMAND_NOT_AVAILABLE.send(player);
            return true;
        }
        boolean inEvent = this.plugin.getEventManager().getEventPlaying(player) != null;
        String eventId = args[0].toLowerCase();
        if (!NumberUtils.isNumber(eventId)) {
            CustomEvent event = this.plugin.getEventManager().getByName(eventId);
            if (event == null) {
                Lang.EVENT_NOT_EXISTS.send(player);
                return true;
            }
            if (event.getState() != EventState.WAITING) {
                player.sendMessage(ChatColor.RED + "Ese evento no está disponible actualmente.");
                return true;
            }
            if (event.getPlayers().containsKey(player.getUniqueId())) {
                player.sendMessage(ChatColor.RED + "Ya estás en este evento.");
                return true;
            }
            if (event.getPlayers().size() >= event.getMaxPlayers() && !player.hasPermission("practice.joinevent.bypass")) {
                player.sendMessage(ChatColor.RED + "¡Lo siento! El evento ya está lleno.");
            }
            event.joinEvent(player);
        } else {
            if (inEvent) {
                Lang.COMMAND_NOT_AVAILABLE.send(player);
                return true;
            }
        }
        return true;
    }
}
