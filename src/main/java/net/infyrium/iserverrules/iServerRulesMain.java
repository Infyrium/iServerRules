package net.infyrium.iserverrules;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.plugin.java.JavaPlugin;

import com.github.retrooper.packetevents.PacketEvents;

import net.infyrium.iserverrules.listeners.PlayerInfoPacketListener;
import net.infyrium.iserverrules.listeners.PlayerListener;
import net.infyrium.iserverrules.listeners.WorldListener;
import net.infyrium.iserverrules.managers.Settings;
import net.infyrium.iserverrules.managers.WorldManager;
import net.infyrium.iserverrules.utils.ConfigUpdater;

public class iServerRulesMain extends JavaPlugin {

    public static final String BYPASS_PERMISSION = "iserverrules.bypass";

    private Settings settings;
    private WorldManager worldManager;
    private PlayerInfoPacketListener packetListener;

    @Override
    public void onEnable() {
        ConfigUpdater.backupIfOutdated(this, "config.yml");
        saveDefaultConfig();

        settings = new Settings(this);
        settings.load();
        worldManager = new WorldManager(this, settings);

        Bukkit.getPluginManager().registerEvents(new PlayerListener(this), this);
        Bukkit.getPluginManager().registerEvents(new WorldListener(this), this);
        if (settings.noPlayer()) {
            packetListener = new PlayerInfoPacketListener();
            PacketEvents.getAPI().getEventManager().registerListener(packetListener);
        }

        for (World world : Bukkit.getWorlds()) {
            worldManager.apply(world);
        }

        getLogger().info("Plugin has been enabled!");
        getLogger().info("Plugin developed by: " + String.join(", ", getPluginMeta().getAuthors()));
        getLogger().info("Website: " + getPluginMeta().getWebsite());
    }

    @Override
    public void onDisable() {
        // PacketEvents keeps its listeners after the plugin is disabled
        if (packetListener != null) {
            PacketEvents.getAPI().getEventManager().unregisterListener(packetListener);
        }
        getLogger().info("Plugin has been disabled!");
    }

    public Settings getSettings() {
        return settings;
    }

    public WorldManager getWorldManager() {
        return worldManager;
    }
}
