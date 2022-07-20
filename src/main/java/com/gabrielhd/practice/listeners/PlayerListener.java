package com.gabrielhd.practice.listeners;

import com.gabrielhd.practice.Practice;
import com.gabrielhd.practice.events.CustomEvent;
import com.gabrielhd.practice.kit.Kit;
import com.gabrielhd.practice.lang.Lang;
import com.gabrielhd.practice.match.Match;
import com.gabrielhd.practice.match.MatchState;
import com.gabrielhd.practice.menus.Menu;
import com.gabrielhd.practice.party.Party;
import com.gabrielhd.practice.player.PlayerData;
import com.gabrielhd.practice.player.PlayerKit;
import com.gabrielhd.practice.player.PlayerState;
import com.gabrielhd.practice.utils.items.ActionItem;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Sign;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.*;
import org.bukkit.event.player.*;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Map;
import java.util.Objects;

public class PlayerListener implements Listener {

    private final Practice plugin;

    public PlayerListener() {
        this.plugin = Practice.getInstance();
    }

    @EventHandler
    public void onJoinPlayer(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        PlayerData.load(player);

        Practice.getInstance().getPlayerManager().sendToSpawnAndReset(player);

        for(Player player2 : Bukkit.getOnlinePlayers()) {
            if(player2.getWorld() == player.getWorld()) {
                player2.showPlayer(player);
                player.showPlayer(player2);
            } else {
                player2.hidePlayer(player);
                player.hidePlayer(player2);
            }
        }
    }

    @EventHandler
    public void onQuitPlayer(PlayerQuitEvent event) {
        Player player = event.getPlayer();

        PlayerData.save(player, true);
    }

    @EventHandler
    public void onKickPlayer(PlayerKickEvent event) {
        Player player = event.getPlayer();

        PlayerData.save(player, true);
    }

    @EventHandler
    public void onPlayerInteractSoup(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (!player.isDead() && player.getItemInHand().getType() == Material.MUSHROOM_SOUP && player.getHealth() < 19.0) {
            double newHealth = Math.min(player.getHealth() + 7.0, 20.0);
            player.setHealth(newHealth);
            player.getItemInHand().setType(Material.BOWL);
            player.updateInventory();
        }
    }

    @EventHandler
    public void onPlayerItemConsume(PlayerItemConsumeEvent event) {
        if (event.getItem().getType() == Material.GOLDEN_APPLE) {
            if (!event.getItem().hasItemMeta() || !event.getItem().getItemMeta().getDisplayName().contains("Golden Head")) {
                return;
            }
            PlayerData playerData = PlayerData.of(event.getPlayer());
            if (playerData.getPlayerState() == PlayerState.FIGHTING) {
                Player player = event.getPlayer();
                event.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 200, 1));
                event.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, 2400, 0));
                player.setFoodLevel(Math.min(player.getFoodLevel() + 6, 20));
            }
        }
    }

    @EventHandler
    public void onRegenerate(EntityRegainHealthEvent event) {
        if (!(event.getEntity() instanceof Player)) {
            return;
        }
        if (event.getRegainReason() != EntityRegainHealthEvent.RegainReason.SATIATED) {
            return;
        }
        Player player = (Player)event.getEntity();
        PlayerData playerData = PlayerData.of(player);
        if (playerData.getPlayerState() == PlayerState.FIGHTING) {
            Match match = Practice.getInstance().getMatchManager().getMatch(player.getUniqueId());
            if (match.getKit().isBuild()) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onPlayerInteractFromPlayer(PlayerInteractEntityEvent e) {
        Player player = e.getPlayer();
        if((e.getRightClicked() instanceof Player)) {
            PlayerData playerData = PlayerData.of(player);
            Party party = Party.of(player.getUniqueId());
            if(playerData.getPlayerState() == PlayerState.SPECTATING) {
                ItemStack item = e.getPlayer().getItemInHand();
                if (item == null) {
                    return;
                }
                if(item.getType() == Material.NETHER_STAR) {
                    if (Practice.getInstance().getEventManager().getSpectators().containsKey(player.getUniqueId())) {
                        Practice.getInstance().getEventManager().removeSpectator(player);
                    }
                    if(party == null) {
                        Practice.getInstance().getMatchManager().removeSpectator(player);
                        return;
                    }
                    party.leaveParty(player);
                }
            }
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (player.getGameMode() == GameMode.CREATIVE) {
            return;
        }
        PlayerData playerData = PlayerData.of(player);

        if (event.getAction().name().endsWith("_BLOCK")) {
            if (event.getClickedBlock().getType().name().contains("SIGN") && event.getClickedBlock().getState() instanceof Sign) {
                Sign sign = (Sign)event.getClickedBlock().getState();
                if (ChatColor.stripColor(sign.getLine(1)).equals("[Soup]")) {
                    event.setCancelled(true);
                    Inventory inventory = Bukkit.createInventory(null, 54, ChatColor.DARK_GRAY + "Soup Refill");
                    for (int i = 0; i < 54; ++i) {
                        inventory.setItem(i, new ItemStack(Material.MUSHROOM_SOUP));
                    }
                    event.getPlayer().openInventory(inventory);
                }
            }
            if (event.getClickedBlock().getType() == Material.CHEST || event.getClickedBlock().getType() == Material.ENDER_CHEST) {
                event.setCancelled(true);
            }
        }

        if (event.getAction().name().startsWith("RIGHT_")) {
            ItemStack item = event.getItem();
            if (item == null) {
                return;
            }

            Party party = Party.of(player.getUniqueId());

            switch (playerData.getPlayerState()) {
                case FIGHTING: {
                    Match match = Practice.getInstance().getMatchManager().getMatch(playerData);
                    switch (item.getType()) {
                        case ENDER_PEARL: {
                            if (match.getMatchState() == MatchState.STARTING) {
                                event.setCancelled(true);

                                player.sendMessage(ChatColor.RED + "¡No puedes lanzar enderpearls en tu estado actual!");
                                player.updateInventory();
                                break;
                            }
                            break;
                        }

                        case ENCHANTED_BOOK: {
                            Kit kit = match.getKit();
                            PlayerInventory inventory2 = player.getInventory();
                            int kitIndex = inventory2.getHeldItemSlot();
                            if (kitIndex == 8) {
                                kit.applyToPlayer(player);
                                break;
                            }
                            Map<Integer, PlayerKit> kits = playerData.getPlayerKits(kit.getName());
                            PlayerKit playerKit = kits.get(kitIndex + 1);
                            if (playerKit != null) {
                                playerKit.applyToPlayer(player);
                                break;
                            }
                            break;
                        }
                    }
                    break;
                }
                case SPAWN: {
                    if(party != null) {
                        for(ActionItem actionItem : this.plugin.getItemManager().getPartyItems()) {
                            if(actionItem.getItem().isSimilar(item)) {
                                switch (actionItem.getType()) {
                                    case INFO: {
                                        player.performCommand("party info");
                                        return;
                                    }
                                    case DUEL: {
                                        if (party != null && !party.isLeader(player)) {
                                            Lang.PARTY_NOT_LEADER.send(player);
                                            return;
                                        }
                                        player.openInventory(this.plugin.getInventoryManager().getPartyInventory().getCurrentPage());
                                        return;
                                    }
                                    case EVENT: {
                                        if (party != null && !party.isLeader(player)) {
                                            player.sendMessage(ChatColor.RED + "No eres el líder de esta party.");
                                            return;
                                        }
                                        player.openInventory(this.plugin.getInventoryManager().getPartyEventInventory().getCurrentPage());
                                        return;
                                    }
                                    case EDITOR: {
                                        player.openInventory(this.plugin.getInventoryManager().getEditorInventory().getCurrentPage());
                                        return;
                                    }
                                    case LEAVE: {
                                        if(party != null) party.leaveParty(player);
                                        return;
                                    }
                                    case OPTIONS: {
                                        if (party != null && !party.isLeader(player)) {
                                            Lang.PARTY_NOT_LEADER.send(player);
                                            return;
                                        }
                                        player.openInventory(this.plugin.getInventoryManager().getPartySettingsInventory().getCurrentPage());
                                        return;
                                    }
                                    case COMMAND: {
                                        String cmd = actionItem.getCommand();

                                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd.replace("%player%", player.getName()));
                                        return;
                                    }
                                    default: {
                                        return;
                                    }
                                }
                            }
                        }
                        return;
                    }

                    for(ActionItem actionItem : this.plugin.getItemManager().getSpawnItems()) {
                        if(actionItem.getItem().isSimilar(item)) {
                            switch (actionItem.getType()) {
                                case RANKED: {
                                    if (party != null && !party.isLeader(player)) {
                                        Lang.PARTY_NOT_LEADER.send(player);
                                        return;
                                    }
                                    if(playerData.getRankeds() == 0) {
                                        player.sendMessage(ChatColor.RED + "¡No te quedan Rankeds! Juega partidas UnRankeds para obtener Rankeds gratis.");
                                        return;
                                    }
                                    player.openInventory(Practice.getInstance().getInventoryManager().getRankedInventory().getCurrentPage());
                                    return;
                                }
                                case UNRANKED: {
                                    if (party != null && !party.isLeader(player)) {
                                        Lang.PARTY_NOT_LEADER.send(player);
                                        return;
                                    }
                                    player.openInventory(this.plugin.getInventoryManager().getUnrankedInventory().getCurrentPage());
                                    return;
                                }
                                case EVENT: {
                                    player.openInventory(Objects.requireNonNull(Menu.getPlayerMenu(player, "host")).getInv());
                                    return;
                                }
                                case PARTY: {
                                    Party.create(player);
                                    return;
                                }
                                case EDITOR: {
                                    player.openInventory(this.plugin.getInventoryManager().getEditorInventory().getCurrentPage());
                                    return;
                                }
                                case OPTIONS: {
                                    player.openInventory(playerData.getOptions().getInventory());
                                    return;
                                }
                                case COMMAND: {
                                    String cmd = actionItem.getCommand();

                                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd.replace("%player%", player.getName()));
                                    return;
                                }
                                default: {
                                    return;
                                }
                            }
                        }
                    }
                    return;
                }
                case QUEUE: {
                    for(ActionItem actionItem : this.plugin.getItemManager().getQueueItems()) {
                        if(actionItem.getItem().isSimilar(item)) {
                            switch (actionItem.getType()) {
                                case LEAVE: {
                                    this.plugin.getQueueManager().removePlayerFromQueue(player);
                                    return;
                                }
                                case COMMAND: {
                                    String cmd = actionItem.getCommand();

                                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd.replace("%player%", player.getName()));
                                    return;
                                }
                                default: {
                                    return;
                                }
                            }
                        }
                    }
                    return;
                }
                case EVENT: {
                    CustomEvent practiceEvent = this.plugin.getEventManager().getEventPlaying(player);

                    for(ActionItem actionItem : this.plugin.getItemManager().getEventItems()) {
                        if(actionItem.getItem().isSimilar(item)) {
                            switch (actionItem.getType()) {
                                case LEAVE: {
                                    if (practiceEvent != null) {
                                        practiceEvent.leave(player);
                                        return;
                                    }
                                    return;
                                }
                                case COMMAND: {
                                    String cmd = actionItem.getCommand();

                                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd.replace("%player%", player.getName()));
                                    return;
                                }
                                default: {
                                    return;
                                }
                            }
                        }
                    }
                    return;
                }
                case SPECTATING: {
                    for(ActionItem actionItem : this.plugin.getItemManager().getSpecItems()) {
                        if(actionItem.getItem().isSimilar(item)) {
                            switch (actionItem.getType()) {
                                case LEAVE: {
                                    if (this.plugin.getEventManager().getSpectators().containsKey(player.getUniqueId())) {
                                        this.plugin.getEventManager().removeSpectator(player);
                                        return;
                                    }
                                    if (party == null) {
                                        this.plugin.getMatchManager().removeSpectator(player);
                                        return;
                                    }
                                    party.leaveParty(player);
                                    return;
                                }
                                case COMMAND: {
                                    String cmd = actionItem.getCommand();

                                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd.replace("%player%", player.getName()));
                                    return;
                                }
                                default: {
                                    return;
                                }
                            }
                        }
                    }
                    return;
                }
                default: {
                    break;
                }
            }
        }
    }

    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        PlayerData playerData = PlayerData.of(player);
        Material drop = event.getItemDrop().getItemStack().getType();
        if(drop == Material.BOWL) {
            event.getItemDrop().remove();
        }
        switch (playerData.getPlayerState()) {
            case FFA: {
                if (drop != Material.BOWL) {
                    event.setCancelled(true);
                    break;
                }
                event.getItemDrop().remove();
                break;
            }
            case FIGHTING: {
                if (drop == Material.ENCHANTED_BOOK) {
                    event.setCancelled(true);
                    break;
                }
                if (drop == Material.GLASS_BOTTLE) {
                    event.getItemDrop().remove();
                    break;
                }
                Match match = this.plugin.getMatchManager().getMatch(event.getPlayer().getUniqueId());
                this.plugin.getMatchManager().addDroppedItem(match, event.getItemDrop());
                break;
            }
            default: {
                event.setCancelled(true);
                break;
            }
        }
    }

    @EventHandler
    public void onPlayerConsumeItem(PlayerItemConsumeEvent event) {
        Player player = event.getPlayer();
        PlayerData playerData = PlayerData.of(player);

        Material drop = event.getItem().getType();
        if(player.getLocation().getWorld().getName().equalsIgnoreCase("lobby")) {
            event.setCancelled(true);
        }
        switch (playerData.getPlayerState()) {
            case FIGHTING:
            case EVENT: {
                if (drop.getId() == 373) {
                    Bukkit.getScheduler().runTaskLaterAsynchronously(Practice.getInstance(), () -> {
                        player.setItemInHand(new ItemStack(Material.AIR));
                        player.updateInventory();
                    }, 1L);
                    break;
                }
                break;
            }
        }
    }

    @EventHandler
    public void onPlayerPickupItem(PlayerPickupItemEvent event) {
        Player player = event.getPlayer();
        PlayerData playerData = PlayerData.of(player);

        if (playerData.getPlayerState() == PlayerState.FIGHTING) {
            Match match = Practice.getInstance().getMatchManager().getMatch(player.getUniqueId());
            if (match.getEntitiesToRemove().contains(event.getItem())) {
                match.removeEntityToRemove(event.getItem());
            }
            else {
                event.setCancelled(true);
            }
        }
        else if (playerData.getPlayerState() != PlayerState.FFA) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onAsyncPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        Party party = Party.of(player.getUniqueId());
        String chatMessage = event.getMessage();
        if (party != null) {
            if (chatMessage.startsWith("!") || chatMessage.startsWith("@")) {
                event.setCancelled(true);
                String message = ChatColor.GOLD + "(Party) " + ChatColor.WHITE + player.getName() + ChatColor.GRAY + ": " + chatMessage.replaceFirst("!", "").replaceFirst("@", "");
                party.broadcast(message);
            }
        }
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        PlayerData playerData = PlayerData.of(player);

        switch (playerData.getPlayerState()) {
            case FIGHTING: {
                Practice.getInstance().getMatchManager().removeFighter(player, playerData, true);
                break;
            }
            case EVENT: {
                CustomEvent currentEvent = Practice.getInstance().getEventManager().getEventPlaying(player);
                if (currentEvent == null) {
                    break;
                }
                currentEvent.onDeath().accept(player);
                break;
            }
        }
        event.setDroppedExp(0);
        event.setDeathMessage(null);
        event.getDrops().clear();
    }

    @EventHandler
    public void onFoodLevelChange(FoodLevelChangeEvent event) {
        Player player = (Player)event.getEntity();
        PlayerData playerData = PlayerData.of(player);

        if (playerData.getPlayerState() == PlayerState.FIGHTING) {
            Match match = Practice.getInstance().getMatchManager().getMatch(player.getUniqueId());
            if (match.getKit().isParkour() || match.getKit().isSumo() || Practice.getInstance().getEventManager().getEventPlaying(player) != null) {
                event.setCancelled(true);
            }
        } else {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onProjectileLaunch(ProjectileLaunchEvent event) {
        if (event.getEntity().getShooter() instanceof Player) {
            Player shooter = (Player)event.getEntity().getShooter();

            PlayerData shooterData = PlayerData.of(shooter);
            if (shooterData.getPlayerState() == PlayerState.FIGHTING) {
                Match match = Practice.getInstance().getMatchManager().getMatch(shooter.getUniqueId());
                match.addEntityToRemove(event.getEntity());
            }
        }
    }

    @EventHandler
    public void onProjectileHit(ProjectileHitEvent event) {
        if (event.getEntity().getShooter() instanceof Player) {
            Player shooter = (Player)event.getEntity().getShooter();

            PlayerData shooterData = PlayerData.of(shooter);
            if (shooterData != null && shooterData.getPlayerState() == PlayerState.FIGHTING) {
                Match match = Practice.getInstance().getMatchManager().getMatch(shooter.getUniqueId());
                match.removeEntityToRemove(event.getEntity());
                if (event.getEntityType() == EntityType.ARROW) {
                    event.getEntity().remove();
                }
            }
        }
    }
}
