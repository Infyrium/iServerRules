package net.infyrium.iserverrules.managers;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

import org.bukkit.GameMode;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.plugin.java.JavaPlugin;

import net.infyrium.iserverrules.models.WorldSettings;

/**
 * Values of config.yml, read once on start: the settings section and settings by worlds.
 */
public class Settings {

    // ALL in noMobSpawn: everything except mobs spawned by plugins and by /summon
    private static final Set<SpawnReason> ALL_REASONS = EnumSet.complementOf(EnumSet.of(SpawnReason.CUSTOM, SpawnReason.COMMAND));

    private final JavaPlugin plugin;

    // Also read from the async chat thread
    private final Map<String, WorldSettings> worlds = new ConcurrentHashMap<>();

    private boolean noPlayer;

    public Settings(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void load() {
        noPlayer = plugin.getConfig().getBoolean("settings.noPlayer");
        worlds.clear();
    }

    public boolean noPlayer() {
        return noPlayer;
    }

    public WorldSettings get(World world) {
        return worlds.computeIfAbsent(world.getName(), this::read);
    }

    private WorldSettings read(String worldName) {
        FileConfiguration config = plugin.getConfig();
        String worldPath = "worlds." + worldName + ".";

        // Value of the world, if set, otherwise the default one
        Function<String, String> path = key ->
                config.isSet(worldPath + key) ? worldPath + key : "default." + key;

        Set<String> allowedCommands = null;
        if (config.getBoolean(path.apply("player.noCommands.enabled"))) {
            allowedCommands = new HashSet<>();
            for (String command : config.getStringList(path.apply("player.noCommands.allowedCommands"))) {
                allowedCommands.add(command.toLowerCase(Locale.ROOT));
            }
        }

        Long fixedTime = config.getBoolean(path.apply("world.fixedTime.enabled"))
                ? config.getLong(path.apply("world.fixedTime.time"))
                : null;

        return new WorldSettings(
                config.getBoolean(path.apply("player.noDamage")),
                config.getBoolean(path.apply("player.noHunger")),
                config.getBoolean(path.apply("player.noMessage")),
                config.getBoolean(path.apply("player.noChat")),
                parseGameMode(config.getString(path.apply("player.gameMode")), worldName),
                allowedCommands,
                config.getBoolean(path.apply("world.noBreak")),
                config.getBoolean(path.apply("world.noPlace")),
                config.getBoolean(path.apply("world.noInteract")),
                config.getBoolean(path.apply("world.noFluidFlow")),
                config.getBoolean(path.apply("world.noExplosions")),
                config.getBoolean(path.apply("world.noWeather")),
                config.getBoolean(path.apply("world.noFireSpread")),
                config.getBoolean(path.apply("world.noLeafDecay")),
                config.getBoolean(path.apply("world.noGrowth")),
                config.getBoolean(path.apply("world.noMobGriefing")),
                parseSpawnReasons(config, path.apply("world.noMobSpawn"), worldName),
                fixedTime);
    }

    private GameMode parseGameMode(String value, String worldName) {
        if (value == null || value.isEmpty()) return null;
        try {
            return GameMode.valueOf(value.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            plugin.getLogger().warning("Incorrect gamemode in the config for world '" + worldName + "': " + value);
            return null;
        }
    }

    private Set<SpawnReason> parseSpawnReasons(FileConfiguration config, String path, String worldName) {
        Set<SpawnReason> reasons = EnumSet.noneOf(SpawnReason.class);
        for (String value : config.getStringList(path)) {
            String name = value.toUpperCase(Locale.ROOT);
            if (name.equals("ALL")) {
                reasons.addAll(ALL_REASONS);
                continue;
            }
            try {
                reasons.add(SpawnReason.valueOf(name));
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("Incorrect spawn reason in noMobSpawn for world '" + worldName + "': " + value);
            }
        }
        return reasons;
    }
}
