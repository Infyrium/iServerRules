package net.infyrium.iserversettings.listeners;

import org.bukkit.Bukkit;
import org.bukkit.GameRule;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockGrowEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.BlockSpreadEvent;
import org.bukkit.event.block.LeavesDecayEvent;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.weather.LightningStrikeEvent;
import org.bukkit.event.weather.WeatherChangeEvent;
import org.bukkit.event.world.StructureGrowEvent;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import net.infyrium.iserversettings.iServerSettingsMain;


public class BlockEventListener implements Listener {

    private final iServerSettingsMain plugin;

    private final boolean noBreak;
    private final boolean noPlace;
    private final boolean noInteract;
    private final boolean noFluidFlow;
    private final boolean noExplosions;
    private final boolean noWeather;
    private final boolean noFireSpread;
    private final boolean noLeafDecay;
    private final boolean noGrowth;
    private final boolean noMobGriefing;
    private final boolean noMobSpawn;
    private final boolean fixedTime;
    private final long fixedTimeMeaning;

    public BlockEventListener(iServerSettingsMain plugin) {
        this.plugin = plugin;

        var config = plugin.getConfig();
        this.noBreak = config.getBoolean("settings.world.noBreak");
        this.noPlace = config.getBoolean("settings.world.noPlace");
        this.noInteract = config.getBoolean("settings.world.noInteract");
        this.noFluidFlow = config.getBoolean("settings.world.noFluidFlow");
        this.noExplosions = config.getBoolean("settings.world.noExplosions");
        this.noWeather = config.getBoolean("settings.world.noWeather");
        this.noFireSpread = config.getBoolean("settings.world.noFireSpread");
        this.noLeafDecay = config.getBoolean("settings.world.noLeafDecay");
        this.noGrowth = config.getBoolean("settings.world.noGrowth");
        this.noMobGriefing = config.getBoolean("settings.world.noMobGriefing");
        this.noMobSpawn = config.getBoolean("settings.world.noMobSpawn");
        this.fixedTime = config.getBoolean("settings.world.fixedTime.enabled");
        this.fixedTimeMeaning = config.getLong("settings.world.fixedTime.time");
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (noBreak) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockDamage(BlockDamageEvent event) {
        if (noBreak) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        if (noPlace) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (noInteract) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockFromTo(BlockFromToEvent event) {
        if (noFluidFlow) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onEntityExplode(EntityExplodeEvent event) {
        if (noExplosions) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockExplode(BlockExplodeEvent event) {
        if (noExplosions) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onWeatherChange(WeatherChangeEvent event) {
        if (noWeather && event.toWeatherState()) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onLightning(LightningStrikeEvent event) {
        if (noWeather) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockIgnite(BlockIgniteEvent event) {
        if (noFireSpread && event.getCause() == BlockIgniteEvent.IgniteCause.SPREAD) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockBurn(BlockBurnEvent event) {
        if (noFireSpread) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onLeavesDecay(LeavesDecayEvent event) {
        if (noLeafDecay) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockGrow(BlockGrowEvent event) {
        if (noGrowth) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockSpread(BlockSpreadEvent event) {
        if (noGrowth) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        if (noMobSpawn) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onStructureGrow(StructureGrowEvent event) {
        if (noGrowth) {
            event.setCancelled(true);
        }
    }

    public void applyWorldSettings() {
        for (World world : Bukkit.getWorlds()) {
            disableGameRule(world, GameRule.DO_DAYLIGHT_CYCLE, "dodaylightcycle", fixedTime);
            if (fixedTime) {
                world.setTime(fixedTimeMeaning);
            }
            disableGameRule(world, GameRule.MOB_GRIEFING, "mobgriefing", noMobGriefing);
        }
        if (noMobSpawn) {
            for (World world : Bukkit.getWorlds()) {
                for (Entity entity : world.getEntities()) {
                    if (!(entity instanceof Player)) {
                        entity.remove();
                    }
                }
            }
        }
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
