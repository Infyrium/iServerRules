package net.infyrium.iserverrules.utils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.logging.Level;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Replaces config files left from an older plugin version, keeping the old file as a backup.
 */
public final class ConfigUpdater {

    private static final String VERSION_KEY = "configVersion";

    private ConfigUpdater() {
    }

    /**
     * Compares configVersion of the file in the plugin folder with the one in the jar.
     * An older file is renamed to "name-old.yml", so the plugin creates a fresh one.
     * A file without configVersion counts as version 1. Call this before the file is read.
     */
    public static void backupIfOutdated(JavaPlugin plugin, String fileName) {
        File file = new File(plugin.getDataFolder(), fileName);
        InputStream resource = plugin.getResource(fileName);
        if (!file.exists() || resource == null) return;

        int currentVersion = YamlConfiguration
                .loadConfiguration(new InputStreamReader(resource, StandardCharsets.UTF_8))
                .getInt(VERSION_KEY, 1);
        int fileVersion = YamlConfiguration.loadConfiguration(file).getInt(VERSION_KEY, 1);
        if (fileVersion >= currentVersion) return;

        String backupName = fileName.replace(".yml", "-old.yml");
        try {
            Files.move(file.toPath(), new File(plugin.getDataFolder(), backupName).toPath(), StandardCopyOption.REPLACE_EXISTING);
            plugin.getLogger().warning(fileName + " had an old format and was replaced with a new one. "
                    + "The old file is saved as " + backupName + ".");
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Could not back up the old " + fileName, e);
        }
    }
}
