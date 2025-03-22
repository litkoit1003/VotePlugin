package org.craftarix.monitoring;

import com.tcoded.folialib.FoliaLib;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.craftarix.monitoring.api.McEcoServiceAsync;
import org.craftarix.monitoring.api.VoteService;
import org.craftarix.monitoring.command.VoteCommand;
import org.craftarix.monitoring.config.Settings;
import org.craftarix.monitoring.menu.listener.MenuListener;
import org.craftarix.monitoring.util.BukkitTasks;

@Getter
public final class MonitoringPlugin extends JavaPlugin {
    public static MonitoringPlugin INSTANCE;

    {
        INSTANCE = this;
    }

    private FoliaLib foliaLib;
    private VoteService voteService;
    private final Settings settings = new Settings();

    @Override
    public void onEnable() {
        saveDefaultConfig();

        foliaLib = new FoliaLib(this);

        var apiKey = getConfig().getString("apiKey");
        voteService = new McEcoServiceAsync(apiKey);
        settings.load(getConfig());

        BukkitTasks.setPlugin(this);
        BukkitTasks.setFoliaLib(foliaLib);

        Bukkit.getPluginManager().registerEvents(new MenuListener(), this);
        getCommand("vote").setExecutor(new VoteCommand());
    }
}
