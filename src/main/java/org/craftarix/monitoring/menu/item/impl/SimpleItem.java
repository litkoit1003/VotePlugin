package org.craftarix.monitoring.menu.item.impl;

import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;
import org.craftarix.monitoring.menu.item.BaseItem;
import org.craftarix.monitoring.menu.item.Item;

public class SimpleItem extends BaseItem {
    public SimpleItem(ItemStack icon, int slot) {
        super(icon, slot);
    }

    @Override
    public void onClick(Event event) {

    }

    @Override
    public Item clone() {
        return super.clone();
    }
}
