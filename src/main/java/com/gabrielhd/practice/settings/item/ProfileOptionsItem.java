package com.gabrielhd.practice.settings.item;

import com.gabrielhd.practice.utils.items.BuilderItem;
import com.gabrielhd.practice.utils.items.UtilItem;
import org.apache.commons.lang3.StringEscapeUtils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public enum ProfileOptionsItem {

    DUEL_REQUESTS("DUEL_REQUESTS", 0, UtilItem.createItem(Material.DIAMOND_SWORD, 1, (short)0, String.valueOf(ChatColor.GOLD.toString()) + ChatColor.BOLD + "Duelos"), "¿Quieres aceptar solicitudes de duelo?"),
    PARTY_INVITES("PARTY_INVITES", 1, UtilItem.createItem(Material.NAME_TAG, 1, (short)0, String.valueOf(ChatColor.GOLD.toString()) + ChatColor.BOLD + "Invitaciones de Party"), "¿Quieres aceptar invitaciones de party?"),
    TOGGLE_SCOREBOARD("TOGGLE_SCOREBOARD", 2, UtilItem.createItem(Material.EMPTY_MAP, 1, (short)0, String.valueOf(ChatColor.GOLD.toString()) + ChatColor.BOLD + "Alternar Scoreboard"), "Alterna el estado de tu Scoreboard"), 
    ALLOW_SPECTATORS("ALLOW_SPECTATORS", 3, UtilItem.createItem(Material.COMPASS, 1, (short)0, String.valueOf(ChatColor.GOLD.toString()) + ChatColor.BOLD + "Permitir Espectadores"), "¿Permitir que los jugadores vean tus combates?"), 
    TOGGLE_TIME("TOGGLE_TIME", 4, UtilItem.createItem(Material.SLIME_BALL, 1, (short)0, String.valueOf(ChatColor.GOLD.toString()) + ChatColor.BOLD + "Alternar Tiempo"), "Alternar entre día, atardecer y noche."),
    TRAILS("TRAILS", 5, UtilItem.createItem(Material.ARROW, 1, (short)0, String.valueOf(ChatColor.GOLD.toString()) + ChatColor.BOLD + "Trails"), "Elige tu rastro de flecha preferido.");
    
    private final ItemStack item;
    private final List<String> description;
    
    ProfileOptionsItem(final String s, final int n, final ItemStack item, final String description) {
        this.item = item;
        (this.description = new ArrayList<>()).add(String.valueOf(ChatColor.DARK_GRAY.toString()) + ChatColor.STRIKETHROUGH + "------------------------");
        StringBuilder parts = new StringBuilder();
        for (int i = 0; i < description.split(" ").length; ++i) {
            final String part = description.split(" ")[i];
            parts.append(part).append(" ");
            if (i == 4 || i + 1 == description.split(" ").length) {
                this.description.add(ChatColor.GRAY + parts.toString().trim());
                parts = new StringBuilder();
            }
        }
        this.description.add(" ");
    }
    
    public ItemStack getItem(final ProfileOptionsItemState state) {
        String e = String.valueOf(ChatColor.DARK_GRAY.toString()) + ChatColor.STRIKETHROUGH + "------------------------";
        if (this == ProfileOptionsItem.DUEL_REQUESTS || this == ProfileOptionsItem.PARTY_INVITES || this == ProfileOptionsItem.ALLOW_SPECTATORS) {
            final List<String> lore = new ArrayList<>(this.description);
            lore.add("  " + ((state == ProfileOptionsItemState.ENABLED) ? (ChatColor.GREEN + StringEscapeUtils.unescapeHtml4("&#9658;") + " ") : "  ") + ChatColor.GRAY + this.getOptionDescription(ProfileOptionsItemState.ENABLED));
            lore.add("  " + ((state == ProfileOptionsItemState.DISABLED) ? (ChatColor.RED + StringEscapeUtils.unescapeHtml4("&#9658;") + " ") : "  ") + ChatColor.GRAY + this.getOptionDescription(ProfileOptionsItemState.DISABLED));
            lore.add(e);
            return new BuilderItem(this.item).setLore(lore).build();
        }
        if (this == ProfileOptionsItem.TOGGLE_TIME) {
            final List<String> lore = new ArrayList<>(this.description);
            lore.add("  " + ((state == ProfileOptionsItemState.DAY) ? (ChatColor.YELLOW + StringEscapeUtils.unescapeHtml4("&#9658;") + " ") : "  ") + ChatColor.GRAY + this.getOptionDescription(ProfileOptionsItemState.DAY));
            lore.add("  " + ((state == ProfileOptionsItemState.SUNSET) ? (ChatColor.GOLD + StringEscapeUtils.unescapeHtml4("&#9658;") + " ") : "  ") + ChatColor.GRAY + this.getOptionDescription(ProfileOptionsItemState.SUNSET));
            lore.add("  " + ((state == ProfileOptionsItemState.NIGHT) ? (ChatColor.BLUE + StringEscapeUtils.unescapeHtml4("&#9658;") + " ") : "  ") + ChatColor.GRAY + this.getOptionDescription(ProfileOptionsItemState.NIGHT));
            lore.add(e);
            return new BuilderItem(this.item).setLore(lore).build();
        }
        if (this == ProfileOptionsItem.TOGGLE_SCOREBOARD) {
            final List<String> lore = new ArrayList<>(this.description);
            lore.add("  " + ((state == ProfileOptionsItemState.ENABLED) ? (ChatColor.GREEN + StringEscapeUtils.unescapeHtml4("&#9658;") + " ") : "  ") + ChatColor.GRAY + this.getOptionDescription(ProfileOptionsItemState.ENABLED));
            lore.add("  " + ((state == ProfileOptionsItemState.SHOW_PING) ? (ChatColor.YELLOW + StringEscapeUtils.unescapeHtml4("&#9658;") + " ") : "  ") + ChatColor.GRAY + this.getOptionDescription(ProfileOptionsItemState.SHOW_PING));
            lore.add("  " + ((state == ProfileOptionsItemState.DISABLED) ? (ChatColor.RED + StringEscapeUtils.unescapeHtml4("&#9658;") + " ") : "  ") + ChatColor.GRAY + this.getOptionDescription(ProfileOptionsItemState.DISABLED));
            lore.add(e);
            return new BuilderItem(this.item).setLore(lore).build();
        }
        return this.getItem(ProfileOptionsItemState.DISABLED);
    }
    
    public String getOptionDescription(final ProfileOptionsItemState state) {
        if (null != this) switch (this) {
            case DUEL_REQUESTS:
            case PARTY_INVITES:
            case ALLOW_SPECTATORS:
                if (state == ProfileOptionsItemState.ENABLED) {
                    return "Enable";
                }   if (state == ProfileOptionsItemState.DISABLED) {
                    return "Disable";
                }   break;
            case TOGGLE_TIME:
                if (state == ProfileOptionsItemState.DAY) {
                    return "Day";
                }   if (state == ProfileOptionsItemState.SUNSET) {
                    return "Sunset";
                }   if (state == ProfileOptionsItemState.NIGHT) {
                    return "Night";
                }   break;
            case TOGGLE_SCOREBOARD:
                if (state == ProfileOptionsItemState.ENABLED) {
                    return "Enable";
                }   if (state == ProfileOptionsItemState.SHOW_PING) {
                    return "Show Ping";
                }   if (state == ProfileOptionsItemState.DISABLED) {
                    return "Disable";
                }   break;
            default:
                break;
        }
        return this.getOptionDescription(ProfileOptionsItemState.DISABLED);
    }
    
    public static ProfileOptionsItem fromItem(final ItemStack itemStack) {
        ProfileOptionsItem[] values;
        for (int length = (values = values()).length, i = 0; i < length; ++i) {
            final ProfileOptionsItem item = values[i];
            ProfileOptionsItemState[] values2;
            for (int length2 = (values2 = ProfileOptionsItemState.values()).length, j = 0; j < length2; ++j) {
                final ProfileOptionsItemState state = values2[j];
                if (item.getItem(state).isSimilar(itemStack)) {
                    return item;
                }
            }
        }
        return null;
    }
}
