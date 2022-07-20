package com.gabrielhd.practice.utils.items;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.inventory.ItemStack;

@Getter
public class ActionItem {

    private final ItemStack item;
    private ActionItemType type;

    @Setter private String command;

    public ActionItem(ItemStack item, String type) {
        this.item = item;

        this.type = ActionItemType.COMMAND;
        this.command = "";

        if(type.contains(":")) {
            String[] split = type.split(":");
            if (split.length > 0) {
                this.type = ActionItemType.of(split[0]);
                this.command = split[1];
            }
        } else {
            this.type = ActionItemType.of(type);
        }
    }

    public enum ActionItemType {
        FFA,
        DUEL,
        INFO,
        LEAVE,
        EVENT,
        PARTY,
        EDITOR,
        RANKED,
        UNRANKED,
        OPTIONS,
        COMMAND;

        public static ActionItemType of(String name) {
            for(ActionItemType type : values()) {
                if(type.name().equalsIgnoreCase(name)) {
                    return type;
                }
            }

            return COMMAND;
        }
    }
}
