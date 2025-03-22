package org.craftarix.monitoring.menu;

import org.bukkit.entity.Player;
import org.bukkit.inventory.InventoryHolder;
import org.craftarix.monitoring.menu.item.Item;

public interface Menu extends InventoryHolder {
    void openInventory(Player player);

    Item getItem(int slot);
}
