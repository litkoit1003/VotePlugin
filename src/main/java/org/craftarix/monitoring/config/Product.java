package org.craftarix.monitoring.config;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.logging.Level;
import java.util.regex.Pattern;

@AllArgsConstructor
@Getter
public class Product {

    // FIX: Command Injection - whitelist разрешённых символов в имени игрока
    private static final Pattern SAFE_PLAYER_NAME = Pattern.compile("^[a-zA-Z0-9_]{1,16}$");

    private final ItemStack icon;
    private final Integer price;
    private final List<String> onBuyCommands;

    public void executeCommands(Player player) {
        String playerName = player.getName();

        // Отклоняем выполнение если имя содержит посторонние символы
        if (!SAFE_PLAYER_NAME.matcher(playerName).matches()) {
            Bukkit.getLogger().log(Level.WARNING,
                    "[MinecraftEcoVote] Blocked command execution: unsafe player name '" + playerName + "'");
            return;
        }

        onBuyCommands.forEach(command -> {
            if (command.contains("{player}")) {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(),
                        command.replace("{player}", playerName));
            } else {
                player.chat(command);
            }
        });
    }
}
