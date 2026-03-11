package org.craftarix.monitoring;

import com.google.common.collect.Lists;
import org.bukkit.entity.Player;
import org.craftarix.monitoring.api.McEcoServiceAsync;
import org.craftarix.monitoring.api.VoteService;
import org.craftarix.monitoring.api.model.GetVotesModel;
import org.craftarix.monitoring.config.Settings;
import org.craftarix.monitoring.menu.impl.PaginatedMenu;
import org.craftarix.monitoring.util.BukkitTasks;
import org.craftarix.monitoring.util.JsonUtil;
import org.craftarix.monitoring.util.ItemUtil;

import java.util.Collections;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class VoteMenu extends PaginatedMenu {

    private static final Settings settings = MonitoringPlugin.INSTANCE.getSettings();

    // FIX: Race Condition - множество игроков, у которых голос уже обрабатывается
    private static final Set<UUID> purchaseInProgress =
            Collections.newSetFromMap(new ConcurrentHashMap<>());

    private final VoteService voteService;
    private int currentVotes;

    public VoteMenu() {
        super(settings.getInventoryTitle(), 54);
        voteService = MonitoringPlugin.INSTANCE.getVoteService();
        setMarkupList(Lists.newArrayList(settings.getProductSlots()));
        setNextIcon(settings.getNextPage());
        setPrevIcon(settings.getPrevPage());
    }

    @Override
    public void openInventory(Player player) {
        if (voteService instanceof McEcoServiceAsync serviceAsync) {
            BukkitTasks.runTaskAsync(() -> {
                serviceAsync.getVotesAsync(player.getName())
                        .whenComplete(((response, throwable) -> {
                            if (response == null) return;
                            if (response.statusCode() != 200) return;

                            currentVotes = JsonUtil.unparseJson(response.body(), GetVotesModel.class).getBalance();
                            BukkitTasks.runTask(() -> super.openInventory(player));
                        }));
            });
        } else {
            currentVotes = voteService.getVotes(player.getName());
            super.openInventory(player);
        }
    }

    @Override
    protected void drawInventory(Player player) {
        settings.getDesign().forEach(item -> {
            var newIcon = ItemUtil.replace(item.getIcon(), "{votes}", String.valueOf(currentVotes));
            var newItem = item.clone();
            newItem.setIcon(newIcon);
            draw(newItem);
        });

        settings.getProducts().forEach(product -> {
            var newIcon = ItemUtil.replace(product.getIcon(), "{votes}", String.valueOf(currentVotes));
            addToMarkup(newIcon, (event) -> {
                UUID playerId = player.getUniqueId();

                if (currentVotes < product.getPrice()) {
                    replaceItem(event.getSlot(), settings.getNotEnoughVotesIcon(), 40);
                    settings.getNotEnoughVotesMessages().forEach(message ->
                            player.sendMessage(message
                                    .replace("{player}", player.getName())
                                    .replace("{votes}", String.valueOf(currentVotes))));
                    return;
                }

                // Блокируем повторный клик пока обработка не завершена
                if (!purchaseInProgress.add(playerId)) {
                    return;
                }

                try {
                    product.executeCommands(player);
                    voteService.takeVote(player.getName(), product.getPrice());
                    currentVotes -= product.getPrice();
                    updateInventory(player);
                    replaceItem(event.getSlot(), settings.getSuccessBuyIcon(), 40);
                } finally {
                    // Снимаем блокировку в любом случае даже при исключении
                    purchaseInProgress.remove(playerId);
                }
            });
        });
    }
}
