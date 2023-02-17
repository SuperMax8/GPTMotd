package fr.supermax_8.gptmotd;

import com.tchristofferson.configupdater.ConfigUpdater;
import lombok.Getter;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ServerPing;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.api.scheduler.ScheduledTask;
import net.md_5.bungee.event.EventHandler;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.concurrent.TimeUnit;

public final class GPTMotdBungee extends Plugin implements Listener {

    public MotdManager motdManager;

    private static ScheduledTask task;

    @Getter
    private static GPTMotdBungee instance;

    @Override
    public void onEnable() {
        instance = this;
        getDataFolder().mkdirs();
        this.getProxy().getPluginManager().registerCommand(this, new Command());
        File f = new File(getDataFolder(), "config.yml");
        if (!f.exists()) try (InputStream in = getResourceAsStream("config.yml")) {
            Files.copy(in, f.toPath());
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            ConfigUpdater.update(this::getResourceAsStream, "config.yml", new File(getDataFolder(), "config.yml"));
        } catch (Exception ignored) {
        }
        reload();
    }

    public void reload() {
        if (task != null) task.cancel();
        File f = new File(getDataFolder(), "config.yml");
        motdManager = new MotdManager(f, r -> getProxy().getScheduler().runAsync(this, r));
        getProxy().getPluginManager().registerListener(this, this);
        if (motdManager.getMode() == MotdManager.Mode.TIME) {
            task = getProxy().getScheduler().schedule(this, () -> {
                motdManager.updateMotd();
            }, 0, Math.max(5, motdManager.getTime()), TimeUnit.SECONDS);
        }
    }

    @Override
    public void onDisable() {
        if (task != null) task.cancel();
    }

    @EventHandler
    public void proxyPing(net.md_5.bungee.api.event.ProxyPingEvent e) {
        if (motdManager.getMotd() != null) {
            ServerPing ping = e.getResponse();
            ping.setDescription(motdManager.getMotd());
            e.setResponse(ping);
        }
        if (motdManager.getMode() == MotdManager.Mode.FOREACH) motdManager.updateMotd();
    }


    private static class Command extends net.md_5.bungee.api.plugin.Command {

        public Command() {
            super("gptmotd", "gptmotd.command", "gptm", "motd");
        }

        @Override
        public void execute(CommandSender sender, String[] args) {
            if (!sender.hasPermission("gptmotd.command")) return;
            try {
                switch (args[0].toLowerCase()) {
                    case "reload":
                        sender.sendMessage("§e[GPTMotd] §7Reload...");
                        instance.reload();
                        sender.sendMessage("§e[GPTMotd] §aReloaded !");
                        break;
                }
            } catch (Exception e) {
                sender.sendMessages(
                        "§e[GPTMotd]",
                        "§8- §7/gptmotd reload §fReload the plugin"
                );
            }
        }
    }

}