package me.rhys.anticheat;

import lombok.Getter;
import me.rhys.anticheat.base.check.impl.CachedCheckManager;
import me.rhys.anticheat.base.command.CommandManager;
import me.rhys.anticheat.base.connection.KeepaliveHandler;
import me.rhys.anticheat.base.listener.BukkitListener;
import me.rhys.anticheat.base.user.UserManager;
import me.rhys.anticheat.config.ConfigLoader;
import me.rhys.anticheat.config.ConfigValues;
import me.rhys.anticheat.tinyprotocol.api.ProtocolVersion;
import me.rhys.anticheat.tinyprotocol.api.TinyProtocolHandler;
import me.rhys.anticheat.util.TPSUtil;
import me.rhys.anticheat.util.UpdateChecker;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

@Getter
public class Anticheat extends JavaPlugin {
    @Getter private static Anticheat instance;

    private UserManager userManager;
    private CommandManager commandManager;

    private String longLine =
            "-----------------------------------------------------------------------------------------------";

    private final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
    private KeepaliveHandler keepaliveHandler;
    private TinyProtocolHandler tinyProtocolHandler;
    public String bukkitVersion;
    private final ConfigValues configValues = new ConfigValues();
    private final ConfigLoader configLoader = new ConfigLoader();
    private final CachedCheckManager checkManager = new CachedCheckManager();

    @Override
    public void onEnable() {
        instance = this;
        this.tinyProtocolHandler = new TinyProtocolHandler();
        this.checkManager.setup();

        if (ProtocolVersion.getGameVersion().isAbove(ProtocolVersion.v1_16_5)) {
            getServer().getPluginManager().disablePlugin(this);
            getLogger().warning("The anticheat is only compatible with 1.7.* to 1.16.5 spigot's (1.8 RECOMMENDED)");
            return;
        }

        this.configLoader.load();
        this.bukkitVersion = Bukkit.getServer().getClass().getPackage().getName().substring(23);
        this.keepaliveHandler = new KeepaliveHandler();
        this.userManager = new UserManager();
        this.commandManager = new CommandManager();

        getServer().getPluginManager().registerEvents(new BukkitListener(), this);

        getServer().getOnlinePlayers().forEach(player -> TinyProtocolHandler.getInstance().addChannel(player));

        //Resets violations after 1 minute
        this.executorService.scheduleAtFixedRate(() -> this.getUserManager().getUserMap().forEach((uuid, user) ->
                user.getCheckManager().getCheckList().forEach(check -> check.setViolation(0))),
                1L, 1L, TimeUnit.MINUTES);

        Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(this, new TPSUtil(), 100L, 1L);

        new UpdateChecker(this, 93504).getVersion(version -> {
            if (getDescription().getVersion().equalsIgnoreCase(version)) {
                getServer().getConsoleSender().sendMessage("\n\n" + ChatColor.GREEN
                        + "You are currently running the latest version of the anticheat! " +
                        "[v"+getDescription().getVersion()+"]\n\n");
            } else {
                getServer().getConsoleSender().sendMessage("\n\n" + ChatColor.DARK_RED
                        + "Your current update is outdated!" + ChatColor.RESET
                        + " Version: [v" + getDescription().getVersion() + "]," + " Latest: [v" + version + "]\n\n"
                        + longLine + "\n"
                        + "| Please download the latest update here: https://www.spigotmc.org/resources/anticheat.93504/ |"
                        + "\n" + longLine + "\n\n");
            }
        });

    }

    @Override
    public void onDisable() {
        this.userManager.getUserMap().forEach((uuid, user) -> {
            TinyProtocolHandler.getInstance().removeChannel(user.getPlayer());
            user.getExecutorService().shutdownNow();
        });
        this.executorService.shutdownNow();

        commandManager.removeCommand();
    }
}
