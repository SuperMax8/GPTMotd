package fr.supermax_8.gptmotd;

import com.tchristofferson.configupdater.ConfigUpdater;
import lombok.Getter;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.ServerListPingEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.io.File;

public class GPTMotdSpigot extends JavaPlugin implements Listener {

    public static MotdManager motdManager;
    private BukkitTask task;

    @Getter
    private static GPTMotdSpigot instance;

    @Override
    public void onEnable() {
        instance = this;
        new Metrics(this, 17742);
        saveDefaultConfig();
        try {
            ConfigUpdater.update(this::getResource, "config.yml", new File(getDataFolder(), "config.yml"));
        } catch (Exception ignored) {
        }
        getCommand("gptmotd").setExecutor(new Command());
        getServer().getPluginManager().registerEvents(this, this);
        reload();
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }


    @EventHandler
    public void onPing(ServerListPingEvent e) {
        if (motdManager.getMotd() != null) e.setMotd(motdManager.getMotd());
        if (motdManager.getMode() == MotdManager.Mode.FOREACH) motdManager.updateMotd();
    }

    public void reload() {
        if (task != null) task.cancel();
        motdManager = new MotdManager(new File(getDataFolder(), "config.yml"), r -> getServer().getScheduler().runTaskAsynchronously(this, r));
        if (motdManager.getMode() == MotdManager.Mode.TIME) {
            task = getServer().getScheduler().runTaskTimerAsynchronously(this, () -> motdManager.updateMotd(), 0, Math.max(5 * 20, motdManager.getTime() * 20));
        }
    }

    private static class Command implements CommandExecutor {

        @Override
        public boolean onCommand(CommandSender sender, org.bukkit.command.Command command, String label, String[] args) {
            if (!sender.hasPermission("gptmotd.command")) return true;
            try {
                switch (args[0].toLowerCase()) {
                    case "reload":
                        sender.sendMessage("§e[GPTMotd] §7Reload...");
                        instance.reload();
                        sender.sendMessage("§e[GPTMotd] §aReloaded !");
                        break;
                }
            } catch (Exception e) {
                sender.sendMessage(new String[]{
                        "§e[GPTMotd]",
                        "§8- §7/gptmotd reload §fReload the plugin",
                });
            }
            return false;
        }


    }

}