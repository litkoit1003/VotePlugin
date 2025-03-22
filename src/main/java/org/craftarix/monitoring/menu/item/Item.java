package org.craftarix.monitoring.menu.item;

import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;

public interface Item {
    void onClick(Event event);

    ItemStack getIcon();

    void setIcon(ItemStack icon);

    int getSlot();

    Item clone();
}
