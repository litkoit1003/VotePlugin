package org.craftarix.monitoring.menu.item;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.inventory.ItemStack;

@Getter

public abstract class BaseItem implements Item, Cloneable {
    @Setter
    private ItemStack icon;
    private int slot;

    public BaseItem(ItemStack icon, int slot) {
        this.icon = icon;
        this.slot = slot;
    }

    public Item clone() {
        try {
            BaseItem item = (BaseItem) super.clone();

            item.icon = icon.clone();
            item.slot = slot;

            return item;
        } catch (CloneNotSupportedException var2) {
            throw new Error(var2);
        }
    }

}
