package org.craftarix.monitoring.config;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.craftarix.monitoring.MonitoringPlugin;
import org.craftarix.monitoring.menu.item.impl.ButtonItem;
import org.craftarix.monitoring.menu.item.Item;
import org.craftarix.monitoring.menu.item.impl.SimpleItem;
import org.craftarix.monitoring.util.ItemUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

@Getter
public class Settings {

    private String inventoryTitle;

    private ItemStack successBuyIcon;
    private ItemStack notEnoughVotesIcon;
    private List<String> notEnoughVotesMessages;
    private SimpleItem nextPage;
    private SimpleItem prevPage;
    private final List<Product> products = new ArrayList<>();
    private final List<Item> design = new ArrayList<>();
    private List<Integer> productSlots;

    public void load(FileConfiguration fileConfiguration) {
        // load design
        loadDesign(fileConfiguration);

        // load products info
        var productsSection = fileConfiguration.getConfigurationSection("products");
        if (productsSection == null) {
            Bukkit.getLogger().log(Level.WARNING, "[Monitoring]: products not configured");
        } else {
            for (var key : productsSection.getKeys(false)) {
                var productSection = productsSection.getConfigurationSection(key);
                var icon = ItemUtil.loadItemFromConfig(productSection.getConfigurationSection("icon"));
                var price = productSection.getInt("price", 0);
                var onBuyCommands = productSection.getStringList("onBuyCommands");
                if (onBuyCommands.isEmpty()) {
                    onBuyCommands.add(productSection.getString("onBuyCommands"));
                }
                var product = new Product(icon, price, onBuyCommands);
                products.add(product);
            }
        }
    }

    private void loadDesign(FileConfiguration fileConfiguration) {
        inventoryTitle = fileConfiguration.getString("design.title", "VoteShop");

        successBuyIcon = ItemUtil.loadItemFromConfig(fileConfiguration.getConfigurationSection("design.successBuyIcon"));
        notEnoughVotesIcon = ItemUtil.loadItemFromConfig(fileConfiguration.getConfigurationSection("design.notEnoughVotesIcon"));
        notEnoughVotesMessages = fileConfiguration.getStringList("design.notEnoughVotesMessages");
        productSlots = fileConfiguration.getIntegerList("design.productSlots");
        var otherDesignSection = fileConfiguration.getConfigurationSection("design.inventory");
        if (otherDesignSection != null) {
            for (var key : otherDesignSection.getKeys(false)) {
                var otherSection = otherDesignSection.getConfigurationSection(key);
                var slot = otherSection.getInt("slot", 0);
                var icon = ItemUtil.loadItemFromConfig(otherSection.getConfigurationSection("icon"));
                var clickCommands = otherSection.getStringList("clickCommand");
                Item item;
                if (!clickCommands.isEmpty()) {
                    item = new ButtonItem(icon, slot, (event -> {
                        var player = (Player) event.getWhoClicked();
                        clickCommands.forEach(command -> {
                            if (command.startsWith("tell:")) {
                                player.sendMessage(command.replace("tell:", "").replace("&", "§"));
                            } else if (!command.contains("{player}")) {
                                player.chat(command);
                            } else {
                                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command.replace("{player}", player.getName()));
                            }
                        });

                    }));
                } else {
                    item = new SimpleItem(icon, slot);
                }
                if (key.equalsIgnoreCase("nextPage")) {
                    nextPage = new SimpleItem(icon, slot);
                } else if (key.equalsIgnoreCase("prevPage")) {
                    prevPage = new SimpleItem(icon, slot);
                } else {
                    design.add(item);
                }

            }
        }

    }

    public void reload() {
        products.clear();
        design.clear();
        MonitoringPlugin.INSTANCE.reloadConfig();
        load(MonitoringPlugin.INSTANCE.getConfig());
    }
}
