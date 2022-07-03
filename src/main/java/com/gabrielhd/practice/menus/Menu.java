package com.gabrielhd.practice.menus;

import com.gabrielhd.practice.utils.items.BuilderItem;
import com.gabrielhd.practice.utils.text.Color;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;

@Getter @Setter
public abstract class Menu {

    private static final HashMap<String, HashMap<String, Menu>> menus = new HashMap<>();

    private final String menuId;
    private final String player;
    private String back;

    private Inventory inv;

    public Menu(Player player, String menuId, String name, int rows) {
        this(player, menuId, name, rows, "none");
    }

    public Menu(Player player, String menuId, String name, int rows, String back) {
        this.player = player.getName();
        this.menuId = menuId;

        this.inv = Bukkit.createInventory(null, rows * 9, Color.text(name));
        this.back = back;

        HashMap<String, Menu> playerMenus = getPlayerMenus(player);
        playerMenus.put(menuId, this);

        menus.put(player.getName(), playerMenus);
    }

    public Menu addItem(ItemStack itemStack) {
        this.inv.addItem(itemStack);
        return this;
    }

    public Menu addItem(BuilderItem itemBuilder) {
        return this.addItem(itemBuilder.build());
    }

    public Menu setItem(int n, BuilderItem itemBuilder) {
        this.inv.setItem(n, itemBuilder.build());
        return this;
    }

    public Menu setItem(int n, int n2, BuilderItem itemBuilder) {
        this.inv.setItem((n - 1) * 9 + (n2 - 1), itemBuilder.build());
        return this;
    }

    public Menu setItem(int n, int n2, ItemStack itemStack) {
        this.inv.setItem(n * 9 + n2, itemStack);
        return this;
    }

    public void newInventoryName(String s) {
        this.inv = Bukkit.createInventory(null, this.inv.getSize(), s);
    }

    public void addFullLine(int n, BuilderItem itemBuilder) {
        itemBuilder.setTitle(" &r");
        for (int i = 1; i < 10; ++i) {
            this.setItem(n, i, itemBuilder);
        }
    }

    public abstract void onOpen(InventoryOpenEvent p0);

    public abstract void onClose(InventoryCloseEvent p0);

    public abstract void onClick(InventoryClickEvent p0);

    public abstract void update();

    public static void remove(Player player) {
        menus.remove(player.getName());
    }

    public static HashMap<String, Menu> getPlayerMenus(Player player) {
        if (menus.containsKey(player.getName())) {
            return menus.get(player.getName());
        }

        return new HashMap<>();
    }

    public static Menu getPlayerMenu(Player player, String s) {
        if (getPlayerMenus(player).containsKey(s)) {
            return getPlayerMenus(player).get(s);
        }

        return null;
    }
}