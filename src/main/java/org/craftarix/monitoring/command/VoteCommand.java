package org.craftarix.monitoring.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.craftarix.monitoring.MonitoringPlugin;
import org.craftarix.monitoring.VoteMenu;

public class VoteCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        if (strings.length >= 1) {
            if (strings[0].equalsIgnoreCase("reload")) {
                if (commandSender.hasPermission("minecrafteco.reload")) {
                    MonitoringPlugin.INSTANCE.getSettings().reload();
                    commandSender.sendMessage("Плагин перезагружен!");
                }
                return true;
            }
        }
        new VoteMenu().openInventory((Player) commandSender);
        return true;
    }
}
