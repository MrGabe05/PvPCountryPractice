package com.gabrielhd.practice.manager;

import com.gabrielhd.practice.Practice;
import com.gabrielhd.practice.arena.Arena;
import com.gabrielhd.practice.kit.Kit;
import com.gabrielhd.practice.lang.Lang;
import com.gabrielhd.practice.match.Match;
import com.gabrielhd.practice.match.MatchTeam;
import com.gabrielhd.practice.party.Party;
import com.gabrielhd.practice.player.PlayerData;
import com.gabrielhd.practice.player.PlayerKit;
import com.gabrielhd.practice.player.PlayerState;
import com.gabrielhd.practice.queue.QueueType;
import com.gabrielhd.practice.utils.inventory.InventoryUI;
import com.gabrielhd.practice.utils.items.ItemUtil;
import com.gabrielhd.practice.utils.inventory.Snapshot;
import com.gabrielhd.practice.utils.text.Clickable;
import com.gabrielhd.practice.utils.text.TextPlaceholders;
import com.google.common.collect.Lists;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.*;

@Getter
public class InventoryManager {

    private final Practice plugin;

    private final InventoryUI unrankedInventory;
    private final InventoryUI rankedInventory;
    private final InventoryUI editorInventory;
    private final InventoryUI duelInventory;
    private final InventoryUI partySplitInventory;
    private final InventoryUI partyFFAInventory;
    private final InventoryUI partyEventInventory;
    private final InventoryUI partyInventory;
    private final InventoryUI partySettingsInventory;

    private final Map<String, InventoryUI> duelMapInventories;
    private final Map<String, InventoryUI> partySplitMapInventories;
    private final Map<String, InventoryUI> partyFFAMapInventories;

    private final Map<UUID, Snapshot> snapshots;
    
    public InventoryManager() {
        this.plugin = Practice.getInstance();

        this.unrankedInventory = new InventoryUI("Unranked", true, 2);
        this.rankedInventory = new InventoryUI("Ranked", true, 2);
        this.editorInventory = new InventoryUI("Editor de Kits", true, 2);
        this.duelInventory = new InventoryUI("Mandar duel", true, 2);
        this.partySplitInventory = new InventoryUI("Peleas en Grupos", true, 2);
        this.partyFFAInventory = new InventoryUI("FFA Party", true, 2);
        this.partyEventInventory = new InventoryUI("Eventos Party", true, 1);
        this.partyInventory = new InventoryUI("Pelear con otras Parties", true, 6);
        this.partySettingsInventory = new InventoryUI("Party Settings", true, 3);

        this.snapshots = new HashMap<>();
        this.duelMapInventories = new HashMap<>();
        this.partyFFAMapInventories = new HashMap<>();
        this.partySplitMapInventories = new HashMap<>();

        this.setupInventories();

        Bukkit.getScheduler().runTaskTimerAsynchronously(this.plugin, this::updateInventories, 20L, 20L);
    }
    
    private void setupInventories() {
        Collection<Kit> kits = this.plugin.getKitManager().getKits().values();
        for (Kit kit : kits) {
            if(!kit.isEnabled()) continue;

            this.unrankedInventory.addItem(new InventoryUI.AbstractClickableItem(kit.getIcon()) {
                @Override
                public void onClick(InventoryClickEvent event) {
                    Player player = (Player)event.getWhoClicked();
                    InventoryManager.this.addToQueue(player, PlayerData.of(player), kit, Party.of(player.getUniqueId()), QueueType.UNRANKED);
                }
            });
            if (kit.isRanked()) {
                this.rankedInventory.addItem(new InventoryUI.AbstractClickableItem(kit.getIcon()) {
                    @Override
                    public void onClick(InventoryClickEvent event) {
                        Player player = (Player)event.getWhoClicked();
                        InventoryManager.this.addToQueue(player, PlayerData.of(player), kit, Party.of(player.getUniqueId()), QueueType.RANKED);
                    }
                });
            }
            this.editorInventory.addItem(new InventoryUI.AbstractClickableItem(ItemUtil.createItem(kit.getIcon().getType(), ChatColor.GREEN + kit.getName(), 1, kit.getIcon().getDurability())) {
                @Override
                public void onClick(InventoryClickEvent event) {
                    Player player = (Player)event.getWhoClicked();

                }
            });
            this.duelInventory.addItem(new InventoryUI.AbstractClickableItem(ItemUtil.createItem(kit.getIcon().getType(), ChatColor.GREEN + kit.getName(), 1, kit.getIcon().getDurability())) {
                @Override
                public void onClick(InventoryClickEvent event) {
                    InventoryManager.this.handleDuelClick((Player)event.getWhoClicked(), kit);
                }
            });
            this.partySplitInventory.addItem(new InventoryUI.AbstractClickableItem(ItemUtil.createItem(kit.getIcon().getType(), ChatColor.GREEN + kit.getName(), 1, kit.getIcon().getDurability())) {
                @Override
                public void onClick(InventoryClickEvent event) {
                    InventoryManager.this.handlePartySplitClick((Player)event.getWhoClicked(), kit);
                }
            });
            this.partyFFAInventory.addItem(new InventoryUI.AbstractClickableItem(ItemUtil.createItem(kit.getIcon().getType(), ChatColor.GREEN + kit.getName(), 1, kit.getIcon().getDurability())) {
                @Override
                public void onClick(InventoryClickEvent event) {
                    InventoryManager.this.handleFFAClick((Player)event.getWhoClicked(), kit);
                }
            });

        }
        this.partyEventInventory.setItem(3, new InventoryUI.AbstractClickableItem(ItemUtil.createItem(Material.FIREWORK_CHARGE, ChatColor.RED + "Split Fights")) {
            @Override
            public void onClick(InventoryClickEvent event) {
                Player player = (Player)event.getWhoClicked();
                player.closeInventory();
                player.openInventory(InventoryManager.this.getPartySplitInventory().getCurrentPage());
            }
        });
        this.partyEventInventory.setItem(5, new InventoryUI.AbstractClickableItem(ItemUtil.createItem(Material.SLIME_BALL, ChatColor.AQUA + "Party FFA")) {
            @Override
            public void onClick(InventoryClickEvent event) {
                Player player = (Player)event.getWhoClicked();
                player.closeInventory();
                player.openInventory(InventoryManager.this.getPartyFFAInventory().getCurrentPage());
            }
        });
        this.partySettingsInventory.setItem(11, new InventoryUI.AbstractClickableItem(ItemUtil.createItem(Material.IRON_DOOR, ChatColor.GREEN+"Modo Publica/Cerrada")) {
            @Override
            public void onClick(InventoryClickEvent event) {
                Player player = (Player)event.getWhoClicked();
                Party party = Party.of(player.getUniqueId());
                if(party != null && party.isLeader(player)) {
                    party.setOpen(!party.isOpen());

                    party.broadcast(Lang.PARTY_STATUS, new TextPlaceholders().set("%status%", (party.isOpen() ? "&aOpen" : "&cClosed")));
                }
            }
        });
        this.partySettingsInventory.setItem(15, new InventoryUI.AbstractClickableItem(ItemUtil.createItem(Material.JUKEBOX, ChatColor.GREEN+"Anunciar party publica")) {
            @Override
            public void onClick(InventoryClickEvent event) {
                Player player = (Player)event.getWhoClicked();
                player.chat("/party alert");
            }
        });
        for (Kit kit : this.plugin.getKitManager().getKits().values()) {
            InventoryUI dlInventory = new InventoryUI("Seleccionar Arena", true, 6);
            InventoryUI pSplitInventory = new InventoryUI("Seleccionar Arena", true, 6);
            InventoryUI pFFAInventory = new InventoryUI("Seleccionar Arena", true, 6);
            for (Arena arena : this.plugin.getArenaManager().getArenas().values()) {
                if (!arena.isEnabled() || kit.getBlacklistArenas().contains(arena.getName()) || (kit.getWhitelistArenas().size() > 0 && !kit.getWhitelistArenas().contains(arena.getName()))) {
                    continue;
                }
                ItemStack book = ItemUtil.createItem(Material.PAPER, ChatColor.YELLOW + arena.getName());
                dlInventory.addItem(new InventoryUI.AbstractClickableItem(book) {
                    @Override
                    public void onClick(InventoryClickEvent event) {
                        InventoryManager.this.handleDuelMapClick((Player)event.getWhoClicked(), arena, kit);
                    }
                });
                pSplitInventory.addItem(new InventoryUI.AbstractClickableItem(book) {
                    @Override
                    public void onClick(InventoryClickEvent event) {
                        InventoryManager.this.handlePartySplitMapClick((Player)event.getWhoClicked(), arena, kit);
                    }
                });
                pFFAInventory.addItem(new InventoryUI.AbstractClickableItem(book) {
                    @Override
                    public void onClick(InventoryClickEvent event) {
                        InventoryManager.this.handlePartyFFAMapClick((Player)event.getWhoClicked(), arena, kit);
                    }
                });
            }
            this.duelMapInventories.put(kit.getName(), dlInventory);
            this.partySplitMapInventories.put(kit.getName(), pSplitInventory);
            this.partyFFAMapInventories.put(kit.getName(), pFFAInventory);
        }
    }
    
    private void updateInventories() {
        for (int i = 0; i < 18; ++i) {
            InventoryUI.ClickableItem unrankedItem = this.unrankedInventory.getItem(i);
            if (unrankedItem != null) {
                unrankedItem.setItemStack(this.updateQueueLore(unrankedItem.getItemStack(), QueueType.UNRANKED));
                this.unrankedInventory.setItem(i, unrankedItem);
            }
            InventoryUI.ClickableItem rankedItem = this.rankedInventory.getItem(i);
            if (rankedItem != null) {
                rankedItem.setItemStack(this.updateQueueLore(rankedItem.getItemStack(), QueueType.RANKED));
                this.rankedInventory.setItem(i, rankedItem);
            }
        }
    }
    
    private ItemStack updateQueueLore(ItemStack itemStack, QueueType type) {
        if (itemStack == null) {
            return null;
        }
        if (itemStack.hasItemMeta() && itemStack.getItemMeta().hasDisplayName()) {
            String ladder = ChatColor.stripColor(itemStack.getItemMeta().getDisplayName());
            int queueSize = this.plugin.getQueueManager().getQueueSize(ladder, type);
            int inGameSize = this.plugin.getMatchManager().getFighters(ladder, type);

            return ItemUtil.reloreItem(itemStack, "§eEn Juego: §6"+inGameSize, "§eEn Espera: §6"+queueSize);
        }
        return null;
    }
    
    private void addToQueue(Player player, PlayerData playerData, Kit kit, Party party, QueueType queueType) {
        if (kit != null) {
            if (party == null) {
                this.plugin.getQueueManager().addPlayerToQueue(player, playerData, kit.getName(), queueType);
            }
        }
    }
    
    public void addSnapshot(Snapshot snapshot) {
        this.snapshots.put(snapshot.getSnapshotId(), snapshot);

        this.plugin.getServer().getScheduler().runTaskLater(this.plugin, () -> this.removeSnapshot(snapshot.getSnapshotId()), 1200L);
    }
    
    public void removeSnapshot(UUID snapshotId) {
        Snapshot snapshot = this.snapshots.get(snapshotId);
        if (snapshot != null) {
            this.snapshots.remove(snapshotId);
        }
    }
    
    public Snapshot getSnapshot(UUID snapshotId) {
        return this.snapshots.get(snapshotId);
    }
    
    public void addParty(Player player) {
        ItemStack skull = ItemUtil.createItem(Material.SKULL_ITEM, ChatColor.GOLD + player.getName() + " (" + ChatColor.GREEN + "1" + ChatColor.GOLD + ")");
        this.partyInventory.addItem(new InventoryUI.AbstractClickableItem(skull) {
            @Override
            public void onClick(InventoryClickEvent inventoryClickEvent) {
                player.closeInventory();
                if (inventoryClickEvent.getWhoClicked() instanceof Player) {
                    Player sender = (Player)inventoryClickEvent.getWhoClicked();
                    sender.performCommand("duel " + player.getName());
                }
            }
        });
    }
    
    public void updateParty(Party party) {
        Player player = this.plugin.getServer().getPlayer(party.getLeader());
        for (int i = 0; i < this.partyInventory.getSize(); ++i) {
            InventoryUI.ClickableItem item = this.partyInventory.getItem(i);
            if (item != null) {
                ItemStack stack = item.getItemStack();
                if (stack.getItemMeta().hasDisplayName() && stack.getItemMeta().getDisplayName().contains(player.getName())) {
                    List<String> lores = new ArrayList<>();
                    party.members().forEach(member -> lores.add(ChatColor.RED + member.getName()));
                    ItemUtil.reloreItem(stack, lores.toArray(new String[0]));
                    ItemUtil.renameItem(stack, ChatColor.GOLD + player.getName() + " (" + ChatColor.WHITE + party.getMembers().size() + ChatColor.GOLD + ")");
                    item.setItemStack(stack);
                    break;
                }
            }
        }
    }
    
    public void removeParty(Party party) {
        Player player = this.plugin.getServer().getPlayer(party.getLeader());
        for (int i = 0; i < this.partyInventory.getSize(); ++i) {
            InventoryUI.ClickableItem item = this.partyInventory.getItem(i);
            if (item != null) {
                ItemStack stack = item.getItemStack();
                if (stack.getItemMeta().hasDisplayName() && stack.getItemMeta().getDisplayName().contains(player.getName())) {
                    this.partyInventory.removeItem(i);
                    break;
                }
            }
        }
    }
    
    private void handleSavingKit(Player player, PlayerData playerData, Kit kit, Map<Integer, PlayerKit> kitMap, int kitIndex) {
        if (kitMap != null && kitMap.containsKey(kitIndex)) {
            kitMap.get(kitIndex).setContents(player.getInventory().getContents().clone());
            player.sendMessage(ChatColor.GREEN + "Kit guardado con éxito #" + ChatColor.RED + kitIndex + ChatColor.RED + ".");
            return;
        }
        PlayerKit playerKit = new PlayerKit(kit.getName(), kitIndex, player.getInventory().getContents().clone(), kit.getName() + " Kit " + kitIndex);
        playerData.addPlayerKit(kitIndex, playerKit);

        player.sendMessage(ChatColor.GREEN + "Kit guardado con éxito #" + ChatColor.RED + kitIndex + ChatColor.RED + ".");
    }
    
    private void handleLoadKit(Player player, int kitIndex, Map<Integer, PlayerKit> kitMap) {
        if (kitMap != null && kitMap.containsKey(kitIndex)) {
            ItemStack[] contents = kitMap.get(kitIndex).getContents();
            ItemStack[] array;
            for (int length = (array = contents).length, i = 0; i < length; ++i) {
                ItemStack itemStack = array[i];
                if (itemStack != null && itemStack.getAmount() <= 0) {
                    itemStack.setAmount(1);
                }
            }
            player.getInventory().setContents(contents);
            player.updateInventory();
        }
    }
    
    private void handleDuelClick(Player player, Kit kit) {
        PlayerData playerData = PlayerData.of(player);
        Player selected = Bukkit.getPlayer(playerData.getDuelSelecting());
        if (selected == null) {
            Lang.PLAYER_NOT_FOUND.send(player, new TextPlaceholders().set("%player%", playerData.getDuelSelecting()));
            return;
        }
        PlayerData targetData = PlayerData.of(selected);
        if (targetData.getPlayerState() != PlayerState.SPAWN) {
            player.sendMessage(ChatColor.RED + "Ese jugador está ocupado actualmente.");
            return;
        }
        Party targetParty = Party.of(selected.getUniqueId());
        Party party = Party.of(player.getUniqueId());
        boolean partyDuel = party != null;
        if (partyDuel && targetParty == null) {
            player.sendMessage(ChatColor.RED + "Ese jugador no está en una party.");
            return;
        }

        player.closeInventory();
        player.openInventory(this.duelMapInventories.get(kit.getName()).getCurrentPage());
    }
    
    private void handlePartySplitClick(Player player, Kit kit) {
        Party party = Party.of(player.getUniqueId());
        if (party == null || kit == null || !party.isLeader(player)) {
            return;
        }
        player.closeInventory();
        if (party.getMembers().size() < 2) {
            Lang.MORE_PLAYERS.send(player);
            return;
        }

        player.openInventory(this.partySplitMapInventories.get(kit.getName()).getCurrentPage());
    }
    
    private void handleFFAClick(Player player, Kit kit) {
        Party party = Party.of(player.getUniqueId());
        if (party == null || kit == null || !party.isLeader(player)) {
            return;
        }
        player.closeInventory();
        if (party.getMembers().size() < 2) {
            Lang.MORE_PLAYERS.send(player);
            return;
        }

        player.openInventory(this.partyFFAMapInventories.get(kit.getName()).getCurrentPage());
    }
    
    private void handleDuelMapClick(Player player, Arena arena, Kit kit) {
        PlayerData playerData = PlayerData.of(player);
        Player selected = Bukkit.getPlayer(playerData.getDuelSelecting());
        if (selected == null) {
            Lang.PLAYER_NOT_FOUND.send(player, new TextPlaceholders().set("%player%", playerData.getDuelSelecting()));
            return;
        }

        PlayerData targetData = PlayerData.of(selected);
        if (targetData.getPlayerState() != PlayerState.SPAWN) {
            player.sendMessage(ChatColor.RED + "Ese jugador está ocupado actualmente.");
            return;
        }

        Party targetParty = Party.of(selected.getUniqueId());
        Party party = Party.of(player.getUniqueId());

        boolean partyDuel = party != null;
        if (partyDuel && targetParty == null) {
            Lang.NOT_IN_PARTY.send(player);
            return;
        }

        if (this.plugin.getMatchManager().getMatchRequest(player.getUniqueId(), selected.getUniqueId()) != null) {
            player.sendMessage(ChatColor.RED + "Ya has enviado una solicitud de duelo a este jugador, por favor espera.");
            return;
        }
        this.sendDuel(player, selected, kit, partyDuel, party, targetParty, arena);
    }
    
    private void handlePartyFFAMapClick(Player player, Arena arena, Kit kit) {
        Party party = Party.of(player.getUniqueId());
        if (party == null || !party.isLeader(player)) {
            return;
        }
        player.closeInventory();

        if (party.getMembers().size() < 2) {
            Lang.MORE_PLAYERS.send(player);
            return;
        }

        this.createFFAMatch(party, arena, kit);
    }
    
    private void handlePartySplitMapClick(Player player, Arena arena, Kit kit) {
        Party party = Party.of(player.getUniqueId());
        if (party == null || !party.isLeader(player)) {
            return;
        }
        player.closeInventory();

        if (party.getMembers().size() < 2) {
            Lang.MORE_PLAYERS.send(player);
            return;
        }

        this.createPartySplitMatch(party, arena, kit);
    }
    
    private void sendDuel(Player player, Player selected, Kit kit, boolean partyDuel, Party party, Party targetParty, Arena arena) {
        player.closeInventory();

        this.plugin.getMatchManager().createMatchRequest(player, selected, arena, kit.getName(), partyDuel);

        if (partyDuel) {
            Player leader = Bukkit.getPlayer(targetParty.getLeader());
            Clickable requestMessage = new Clickable(Lang.PARTY_DUEL_REQUEST_RECEIVED.get(leader, new TextPlaceholders().set("%player%", player.getName()).set("%size%", party.getMembers().size()).set("%kit%", kit.getName())), Lang.PARTY_HOVER_DUEL_MESSAGE.get(leader), "/accept " + player.getName() + " " + kit.getName());
            requestMessage.sendToPlayer(leader);

            party.broadcast(Lang.PARTY_DUEL_REQUEST_SENT, new TextPlaceholders().set("%player%", selected.getName()).set("%size%", party.getMembers().size()).set("%kit%", kit.getName()));
            return;
        }

        Clickable requestMessage = new Clickable(Lang.PARTY_DUEL_REQUEST_RECEIVED.get(selected, new TextPlaceholders().set("%player%", player.getName()).set("%kit%", kit.getName())), Lang.PLAYER_DUEL_HOVER_MESSAGE.get(selected), "/accept " + player.getName() + " " + kit.getName());
        requestMessage.sendToPlayer(selected);

        Lang.PLAYER_DUEL_REQUEST_SENT.send(player, new TextPlaceholders().set("%player%", selected.getName()).set("%kit%", kit.getName()));
    }
    
    private void createPartySplitMatch(Party party, Arena arena, Kit kit) {
        MatchTeam[] teams = party.split();
        Match match = new Match(arena, kit, QueueType.UNRANKED, teams);
        Player leaderA = this.plugin.getServer().getPlayer(teams[0].getLeader());
        Player leaderB = this.plugin.getServer().getPlayer(teams[1].getLeader());

        match.broadcast(Lang.MATCH_START, new TextPlaceholders().set("%mode%", "Party Split").set("%kit%", kit.getName()));
        this.plugin.getMatchManager().createMatch(match);
    }
    
    private void createFFAMatch(Party party, Arena arena, Kit kit) {
        MatchTeam team = new MatchTeam(party.getLeader(), Lists.newArrayList((Iterable)party.getMembers()), 0);
        Match match = new Match(arena, kit, QueueType.UNRANKED, team);

        match.broadcast(Lang.MATCH_START, new TextPlaceholders().set("%mode%", "FFA").set("%kit%", kit.getName()));
        this.plugin.getMatchManager().createMatch(match);
    }
}
