package net.infyrium.iserverrules.managers;

import org.bukkit.GameRule;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Mob;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import net.infyrium.iserverrules.models.WorldSettings;

/**
 * Applies settings that are stored in the world itself: time, gamerules, mobs already in it.
 */
public class WorldManager {

    private final JavaPlugin plugin;

    private final Settings settings;

    public WorldManager(JavaPlugin plugin, Settings settings) {
        this.plugin = plugin;
        this.settings = settings;
    }

    public void apply(World world) {
        WorldSettings worldSettings = settings.get(world);

        disableGameRule(world, GameRule.DO_DAYLIGHT_CYCLE, "dodaylightcycle", worldSettings.fixedTime() != null);
        if (worldSettings.fixedTime() != null) {
            world.setTime(worldSettings.fixedTime());
        }
        disableGameRule(world, GameRule.MOB_GRIEFING, "mobgriefing", worldSettings.noMobGriefing());
        removeForbiddenMobs(world.getEntities());
    }

    /**
     * Removes mobs that could not appear now: spawned for a reason from noMobSpawn of their world.
     */
    public void removeForbiddenMobs(Iterable<Entity> entities) {
        for (Entity entity : entities) {
            if (entity instanceof Mob mob && isForbidden(mob)) {
                mob.remove();
            }
        }
    }

    public boolean isForbidden(Mob mob) {
        return settings.get(mob.getWorld()).noMobSpawn().contains(mob.getEntitySpawnReason());
    }

    /**
     * Sets the gamerule to false and remembers its previous value in the world data.
     * When the setting is turned off, restores the previous value.
     * The data key is fixed: since 1.21.11 rule names are "minecraft:..." and change between versions.
     */
    private void disableGameRule(World world, GameRule<Boolean> rule, String dataKey, boolean disable) {
        NamespacedKey key = new NamespacedKey(plugin, dataKey);
        PersistentDataContainer data = world.getPersistentDataContainer();

        if (disable) {
            if (!data.has(key)) {
                Boolean previous = world.getGameRuleValue(rule);
                data.set(key, PersistentDataType.BOOLEAN, previous == null || previous);
            }
            world.setGameRule(rule, false);
        } else if (data.has(key)) {
            world.setGameRule(rule, data.get(key, PersistentDataType.BOOLEAN));
            data.remove(key);
        }
    }
}
