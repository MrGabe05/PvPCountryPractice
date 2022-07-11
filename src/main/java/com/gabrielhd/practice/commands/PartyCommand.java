package com.gabrielhd.practice.commands;

import com.gabrielhd.practice.Practice;
import com.gabrielhd.practice.lang.Lang;
import com.gabrielhd.practice.party.Party;
import com.gabrielhd.practice.player.PlayerData;
import com.gabrielhd.practice.player.PlayerState;
import com.gabrielhd.practice.utils.text.Clickable;
import com.gabrielhd.practice.utils.text.TextPlaceholders;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.*;

public class PartyCommand extends Command {

    private final Practice plugin;

    private static final String NOT_LEADER;
    private static final String[] HELP_MESSAGE;
    
    static {
        NOT_LEADER = ChatColor.RED + "No eres el líder de la party!";
        HELP_MESSAGE = new String[] {
                ChatColor.GRAY + "" + ChatColor.STRIKETHROUGH + "----------------------------------------------------",
                ChatColor.GOLD + "Comandos Party:",
                ChatColor.RESET + "/party help " + ChatColor.GRAY + "- Muestra el menú de ayuda.",
                ChatColor.RESET + "/party create " + ChatColor.GRAY + "- Creas una party",
                ChatColor.RESET + "/party leave " + ChatColor.GRAY + "- Te sales de tu party actual",
                ChatColor.RESET + "/party info " + ChatColor.GRAY + "- Muestra la información de la party",
                ChatColor.RESET + "/party join (player) " + ChatColor.GRAY + "- entras a una party (invitado o publica)",
                "",
                ChatColor.GOLD + "Comandos de Lider:",
                ChatColor.RESET + "/party open " + ChatColor.GRAY + "- Abre tu party para que otros se unan",
                ChatColor.RESET + "/party lock " + ChatColor.GRAY + "- Bloquee su party para que otros se unan",
                ChatColor.RESET + "/party setlimit (amount) " + ChatColor.GRAY + "- Establece un límite para tu party",
                ChatColor.RESET + "/party invite (player) " + ChatColor.GRAY + "- Invita a un jugador a tu fiesta",
                ChatColor.RESET + "/party kick (player) " + ChatColor.GRAY + "- Expulsa a un jugador de tu party",
                ChatColor.GRAY.toString() + ChatColor.STRIKETHROUGH + "----------------------------------------------------" };
    }
    
    public PartyCommand() {
        super("party");

        this.plugin = Practice.getInstance();

        this.setDescription("Party Command.");
        this.setAliases(Collections.singletonList("p"));
        this.setUsage(ChatColor.RED + "Use: /party <subcommand> [player]");
    }
    
    @Override
    public boolean execute(CommandSender sender, String alias, String[] args) {
        if (!(sender instanceof Player)) {
            return true;
        }
        Player player = (Player)sender;

        PlayerData playerData = PlayerData.of(player);
        Party party = Party.of(player.getUniqueId());
        if (party == null) {
            Lang.NOT_IN_PARTY.send(player);
            return true;
        }

        String subCommand = (args.length < 1) ? "help" : args[0];
        switch (subCommand.toLowerCase()) {
            case "accept": {
                if (party != null) {
                    Lang.PARTY_ALREADY.send(player);
                    return true;
                }
                if (args.length < 2) {
                    player.sendMessage(ChatColor.RED + "Use: /party accept <player>");
                    return true;
                }
                if (playerData.getPlayerState() != PlayerState.SPAWN) {
                    Lang.COMMAND_NOT_AVAILABLE.send(player);
                    return true;
                }
                Player target = this.plugin.getServer().getPlayer(args[1]);
                if (target == null) {
                    Lang.PLAYER_NOT_FOUND.send(player, new TextPlaceholders().set("%player%", args[1]));
                    return true;
                }
                Party targetParty = Party.of(target.getUniqueId());
                if (targetParty == null) {
                    Lang.NOT_IN_PARTY.send(player, new TextPlaceholders().set("%player%", target.getName()));
                    return true;
                }
                if (targetParty.getMembers().size() == targetParty.getLimit()) {
                    Lang.PARTY_LIMIT.send(player);
                    return true;
                }
                if (!targetParty.hasPartyInvite(player.getUniqueId())) {
                    Lang.NOT_PARTY_REQUESTS.send(player);
                    return true;
                }
                party.joinPlayer(player);
                return true;
            }
            case "alert": {
                if(!party.isOpen()) {
                    Lang.PARTY_PUBLIC_REQUIRED.send(player);
                    return true;
                }

                if (player.hasPermission("pvpcountry.party.broadcast")) {
                    for (Player online : Bukkit.getOnlinePlayers()) {
                        Clickable text = new Clickable("");
                        text.add(Lang.PARTY_BROADCAST.get(online, new TextPlaceholders().set("%player%", player.getName())), Lang.PARTY_HOVER_BROADCAST.get(online, new TextPlaceholders()), "/party join " + player.getName());
                        text.sendToPlayer(online);
                    }
                }
            }
            case "create": {
                if (party != null) {
                    Lang.PARTY_ALREADY.send(player);
                    return true;
                }
                if (playerData.getPlayerState() != PlayerState.SPAWN) {
                    Lang.COMMAND_NOT_AVAILABLE.send(player);
                    return true;
                }
                Party.create(player);
                return true;
            }
            case "invite": {
                if (party == null) {
                    Lang.NOT_IN_PARTY.send(player);
                    return true;
                }
                if (!party.isLeader(player)) {
                    Lang.PARTY_NOT_LEADER.send(player);
                    return true;
                }
                if (args.length < 2) {
                    player.sendMessage(ChatColor.RED + "Usa: /party invite (player)");
                    return true;
                }
                if (party.isOpen()) {
                    Lang.PARTY_IS_OPEN.send(player);
                    return true;
                }
                if (party.getMembers().size() == party.getLimit()) {
                    Lang.PARTY_LIMIT.send(player);
                    return true;
                }
                Player target = this.plugin.getServer().getPlayer(args[1]);
                if (target == null) {
                    Lang.PLAYER_NOT_FOUND.send(player, new TextPlaceholders().set("%player%", args[1]));
                    return true;
                }
                PlayerData targetData = PlayerData.of(target);
                if (target.getUniqueId() == player.getUniqueId()) {
                    Lang.PARTY_CANT_INVITE_YOURSELF.send(player);
                    return true;
                }
                if (Party.of(target.getUniqueId()) != null) {
                    Lang.PARTY_ALREADY_IN.send(player, new TextPlaceholders().set("%player%", target.getName()));
                    return true;
                }
                if (targetData.getPlayerState() != PlayerState.SPAWN) {
                    Lang.COMMAND_NOT_AVAILABLE_FOR_PLAYER.send(player, new TextPlaceholders().set("%player%", target.getName()));
                    return true;
                }
                if (party.hasPartyInvite(target.getUniqueId())) {
                    Lang.PARTY_INVITE_ALREADY_SENT.send(player);
                    return true;
                }
                party.createPartyInvite(target.getUniqueId());
                Clickable partyInvite = new Clickable(Lang.PARTY_INVITE_MESSAGE.get(target, new TextPlaceholders().set("%player%", player.getName())), Lang.PARTY_INVITE_HOVER_MESSAGE.get(target), "/party accept " + sender.getName());
                partyInvite.sendToPlayer(target);

                party.broadcast(Lang.PARTY_INVITE_BROADCAST, new TextPlaceholders().set("%player%", target.getName()));
                return true;
            }
            case "info": {
                if (party == null) {
                    Lang.NOT_IN_PARTY.send(player);
                    return true;
                }
                List<UUID> members = new ArrayList<>(party.getMembers());
                members.remove(party.getLeader());

                StringBuilder builder = new StringBuilder();
                members.stream().map(this.plugin.getServer()::getPlayer).filter(Objects::nonNull).forEach(member -> {
                    if(builder.length() > 0) {
                        builder.append(", ");
                    }
                    builder.append(member.getName());
                });

                TextPlaceholders textPlaceholders = new TextPlaceholders();
                textPlaceholders.set("%leader%", Bukkit.getPlayer(party.getLeader()).getName());
                textPlaceholders.set("%members%", builder.toString());
                textPlaceholders.set("%status%", (party.isOpen() ? "&aOpen" : "&cClosed"));

                Lang.PARTY_INFO.send(player, textPlaceholders);
                return true;
            }
            case "join": {
                if (party != null) {
                    Lang.PARTY_ALREADY.send(player);
                    return true;
                }
                if (args.length < 2) {
                    player.sendMessage(ChatColor.RED + "Usa: /party join <player>.");
                    return true;
                }
                if (playerData.getPlayerState() != PlayerState.SPAWN) {
                    Lang.COMMAND_NOT_AVAILABLE.send(player);
                    return true;
                }
                Player target = this.plugin.getServer().getPlayer(args[1]);
                if (target == null) {
                    Lang.PLAYER_NOT_FOUND.send(target, new TextPlaceholders().set("%player%", args[1]));
                    return true;
                }
                Party targetParty = Party.of(target.getUniqueId());
                if(targetParty == null) {
                    Lang.PARTY_NOT_EXISTS.send(player, new TextPlaceholders().set("%party%", target.getName()));
                    return true;
                }

                if(!targetParty.isOpen() && !targetParty.hasPartyInvite(player.getUniqueId())) {
                    Lang.PARTY_IS_CLOSED.send(player);
                    return true;
                }

                if(targetParty.getMembers().size() >= targetParty.getLimit()) {
                    Lang.PARTY_LIMIT.send(player);
                    return true;
                }

                party.joinPlayer(player);
                return true;
            }
            case "kick": {
                if (party == null) {
                    Lang.NOT_IN_PARTY.send(player);
                    return true;
                }
                if (args.length < 2) {
                    player.sendMessage(ChatColor.RED + "Usa: /party kick <player>.");
                    return true;
                }
                if (party.getLeader() != player.getUniqueId()) {
                    Lang.PARTY_NOT_LEADER.send(player);
                    return true;
                }
                Player target = this.plugin.getServer().getPlayer(args[1]);
                if (target == null) {
                    Lang.PLAYER_NOT_FOUND.send(player, new TextPlaceholders().set("%player%", args[1]));
                    return true;
                }
                Party targetParty = Party.of(target.getUniqueId());
                if (targetParty == null) {
                    Lang.NOT_IN_YOUR_PARTY.send(player, new TextPlaceholders().set("%player%", target.getName()));
                    return true;
                }
                party.leaveParty(target);
                return true;
            }
            case "lock":
            case "open": {
                if (party.getLeader() != player.getUniqueId()) {
                    Lang.PARTY_NOT_LEADER.send(player);
                    return true;
                }
                party.setOpen(!party.isOpen());

                party.broadcast(Lang.PARTY_STATUS, new TextPlaceholders().set("%status%", (party.isOpen() ? "&aOpen" : "&cClosed")));
                return true;
            }
            case "leave": {
                if (party == null) {
                    Lang.NOT_IN_PARTY.send(player);
                    return true;
                }
                if (playerData.getPlayerState() != PlayerState.SPAWN) {
                    Lang.COMMAND_NOT_AVAILABLE.send(player);
                    return true;
                }
                party.leaveParty(player);
                return true;
            }
            default:
                break;
        }

        player.sendMessage(PartyCommand.HELP_MESSAGE);
        return true;
    }
}
