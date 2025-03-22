package org.craftarix.monitoring.menu.item.impl;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.event.Event;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.craftarix.monitoring.menu.item.ClickHandler;
import org.craftarix.monitoring.menu.item.BaseItem;
import org.craftarix.monitoring.menu.item.Item;

public class ButtonItem extends BaseItem {
    @Getter
    private ClickHandler clickHandler;

    public ButtonItem(ItemStack icon, int slot, ClickHandler clickHandler) {
        super(icon, slot);
        this.clickHandler = clickHandler;
    }

    @Override
    public void onClick(Event event) {
        if (event instanceof InventoryClickEvent)
            clickHandler.handle((InventoryClickEvent) event);
    }

    @Override
    public Item clone() {
        ButtonItem newItem = (ButtonItem) super.clone();
        newItem.clickHandler = clickHandler;
        return newItem;
    }
}
