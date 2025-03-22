package org.craftarix.monitoring.menu.listener;


import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.craftarix.monitoring.menu.Menu;

public class MenuListener implements Listener {

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getInventory().getHolder() instanceof Menu menuInventory)) {
            return;
        }

        event.setCancelled(true);
        var item = menuInventory.getItem(event.getSlot());

        if (item == null) {
            return;
        }

        item.onClick(event);
    }

}
