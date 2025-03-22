package org.craftarix.monitoring.menu.impl;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.craftarix.monitoring.menu.Menu;
import org.craftarix.monitoring.menu.item.impl.ButtonItem;
import org.craftarix.monitoring.menu.item.ClickHandler;
import org.craftarix.monitoring.menu.item.Item;
import org.craftarix.monitoring.menu.item.impl.SimpleItem;

import java.util.concurrent.ConcurrentHashMap;

public abstract class SimpleMenu implements Menu {
    private final Inventory inventory;
    private final ConcurrentHashMap<Integer, Item> items = new ConcurrentHashMap<>();

    public SimpleMenu(String title, int size) {
        inventory = Bukkit.createInventory(this, size, title);
    }

    protected abstract void drawInventory(Player player);

    protected void draw(Item item) {
        items.put(item.getSlot(), item);
    }

    protected void draw(int slot, ItemStack icon, ClickHandler clickHandler) {
        draw(new ButtonItem(icon, slot, clickHandler));
    }

    protected void draw(int slot, ItemStack icon) {
        draw(new SimpleItem(icon, slot));
    }

    public Item getItem(int slot) {
        return items.get(slot);
    }

    @Override
    public void openInventory(Player player) {
        drawInventory(player);
        inventory.clear();
        items.forEach((slot, item) -> {
            inventory.setItem(slot, item.getIcon());
        });
        player.openInventory(inventory);
    }
}
