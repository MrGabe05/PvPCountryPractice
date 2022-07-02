package com.gabrielhd.practice.player;

import com.gabrielhd.practice.Practice;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

@Getter @Setter
public class PlayerKit {

    private final String name;
    private final int index;
    private ItemStack[] contents;
    private String displayName;

    public PlayerKit(String name, int index, ItemStack[] contents, String displayName) {
        this.name = name;
        this.index = index;
        this.contents = contents;
        this.displayName = displayName;
    }

    public void applyToPlayer(Player player) {
        ItemStack[] content;
        for (int length = (content = this.contents).length, i = 0; i < length; ++i) {
            ItemStack itemStack = content[i];
            if (itemStack != null && itemStack.getAmount() <= 0) {
                itemStack.setAmount(1);
            }
        }

        player.getInventory().setContents(this.contents);
        player.getInventory().setArmorContents(Practice.getInstance().getKitManager().getKit(this.name).getArmor());
        player.updateInventory();
    }
}
