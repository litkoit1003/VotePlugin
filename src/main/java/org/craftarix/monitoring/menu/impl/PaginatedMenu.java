package org.craftarix.monitoring.menu.impl;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.craftarix.monitoring.menu.Menu;
import org.craftarix.monitoring.menu.item.ClickHandler;
import org.craftarix.monitoring.menu.item.Item;
import org.craftarix.monitoring.menu.item.impl.ButtonItem;
import org.craftarix.monitoring.menu.item.impl.SimpleItem;
import org.craftarix.monitoring.util.BukkitTasks;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public abstract class PaginatedMenu implements Menu {
    @Getter
    private final Inventory inventory;
    private final ConcurrentHashMap<Integer, Item> items = new ConcurrentHashMap<>();
    private final List<Item> list = new ArrayList<>();
    @Setter
    private List<Integer> markupList = new ArrayList<>();
    private int currentPage = 0;
    private int pagesCount;
    @Setter
    private SimpleItem nextIcon;
    @Setter
    private SimpleItem prevIcon;

    public PaginatedMenu(String title, int size) {
        inventory = Bukkit.createInventory(this, size, title);
    }

    protected void replaceItem(int slot, ItemStack newIcon, int delay) {
        var oldItem = items.remove(slot);
        inventory.setItem(slot, newIcon);
        BukkitTasks.runTaskLater(() -> {
            items.put(slot, oldItem);
            inventory.setItem(slot, oldItem.getIcon());
        }, delay);
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

    private void addMarkup(Item item) {
        list.add(item);
    }

    protected void addToMarkup(ItemStack icon, ClickHandler clickHandler) {
        addMarkup(new ButtonItem(icon, -1, clickHandler));
    }

    protected void addToMarkup(ItemStack icon) {
        addMarkup(new SimpleItem(icon, -1));
    }

    public Item getItem(int slot) {
        return items.get(slot);
    }

    @Override
    public void openInventory(Player player) {
        drawPage(player);
        player.openInventory(inventory);
    }

    private void drawPage(Player player) {
        inventory.clear();
        items.clear();
        list.clear();

        drawInventory(player);
        draw(nextIcon.getSlot(), nextIcon.getIcon(), (clickEvent) -> {
            if (currentPage >= (list.size() - 1) / markupList.size()) {
                return;
            }
            currentPage++;
            updateInventory(player);
        });
        draw(prevIcon.getSlot(), prevIcon.getIcon(), (clickEvent) -> {
            if (currentPage <= 0) {
                return;
            }
            currentPage--;
            updateInventory(player);
        });

        items.forEach((slot, item) -> {
            inventory.setItem(slot, item.getIcon());
        });
        var currentIndex = currentPage * markupList.size();

        for (var index = currentIndex; index < markupList.size() * (currentPage + 1); index++) {
            var markupIndex = index - currentIndex;
            if (index >= list.size()) {
                break;
            }

            var item = list.get(index);
            inventory.setItem(markupList.get(markupIndex), item.getIcon());
            if (item instanceof ButtonItem) {
                draw(markupList.get(markupIndex), item.getIcon(), ((ButtonItem) item).getClickHandler());
            } else {
                draw(markupList.get(markupIndex), item.getIcon());
            }

        }
    }

    protected void updateInventory(Player player) {
        inventory.clear();
        drawPage(player);
    }
}
