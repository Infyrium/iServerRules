package net.infyrium.iserverrules.listeners;

import java.util.Arrays;

import org.bukkit.World;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Hanging;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockGrowEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.BlockSpreadEvent;
import org.bukkit.event.block.LeavesDecayEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntityPlaceEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.hanging.HangingBreakEvent;
import org.bukkit.event.hanging.HangingPlaceEvent;
import org.bukkit.event.player.PlayerArmorStandManipulateEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.vehicle.VehicleDamageEvent;
import org.bukkit.event.vehicle.VehicleDestroyEvent;
import org.bukkit.event.weather.LightningStrikeEvent;
import org.bukkit.event.weather.WeatherChangeEvent;
import org.bukkit.event.world.EntitiesLoadEvent;
import org.bukkit.event.world.StructureGrowEvent;
import org.bukkit.event.world.WorldLoadEvent;

import net.infyrium.iserverrules.iServerRulesMain;
import net.infyrium.iserverrules.models.WorldSettings;

public class WorldListener implements Listener {

    private final iServerRulesMain plugin;

    public WorldListener(iServerRulesMain plugin) {
        this.plugin = plugin;
    }

    private WorldSettings settings(World world) {
        return plugin.getSettings().get(world);
    }

    // Players with the bypass permission are not stopped by noBreak, noPlace, noInteract
    private boolean bypass(Entity entity) {
        return entity instanceof Player player && player.hasPermission(iServerRulesMain.BYPASS_PERMISSION);
    }

    // The player who did it, also through a projectile
    private Entity source(Entity entity) {
        if (entity instanceof Projectile projectile && projectile.getShooter() instanceof Entity shooter) {
            return shooter;
        }
        return entity;
    }

    @EventHandler
    public void onWorldLoad(WorldLoadEvent event) {
        plugin.getWorldManager().apply(event.getWorld());
    }

    @EventHandler
    public void onEntitiesLoad(EntitiesLoadEvent event) {
        if (!settings(event.getWorld()).noMobSpawn().isEmpty()) {
            plugin.getWorldManager().removeForbiddenMobs(event.getEntities());
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (settings(event.getBlock().getWorld()).noBreak() && !bypass(event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockDamage(BlockDamageEvent event) {
        if (settings(event.getBlock().getWorld()).noBreak() && !bypass(event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    // Item frames, paintings
    @EventHandler
    public void onHangingBreak(HangingBreakEvent event) {
        if (!settings(event.getEntity().getWorld()).noBreak()) return;
        if (event instanceof HangingBreakByEntityEvent byEntity && bypass(source(byEntity.getRemover()))) return;
        event.setCancelled(true);
    }

    // Armor stands and items in frames are broken by damage
    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        Entity entity = event.getEntity();
        if (!(entity instanceof ArmorStand || entity instanceof Hanging)) return;
        if (!settings(entity.getWorld()).noBreak()) return;
        if (event instanceof EntityDamageByEntityEvent byEntity && bypass(source(byEntity.getDamager()))) return;
        event.setCancelled(true);
    }

    @EventHandler
    public void onVehicleDamage(VehicleDamageEvent event) {
        if (settings(event.getVehicle().getWorld()).noBreak() && !bypass(source(event.getAttacker()))) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onVehicleDestroy(VehicleDestroyEvent event) {
        if (settings(event.getVehicle().getWorld()).noBreak() && !bypass(source(event.getAttacker()))) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        if (settings(event.getBlock().getWorld()).noPlace() && !bypass(event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onHangingPlace(HangingPlaceEvent event) {
        if (settings(event.getEntity().getWorld()).noPlace() && !bypass(event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    // Armor stands, boats, minecarts, end crystals
    @EventHandler
    public void onEntityPlace(EntityPlaceEvent event) {
        if (settings(event.getEntity().getWorld()).noPlace() && !bypass(event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (settings(event.getPlayer().getWorld()).noInteract() && !bypass(event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    // Item frames, villagers, animals, boats
    @EventHandler
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        if (settings(event.getPlayer().getWorld()).noInteract() && !bypass(event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerInteractAtEntity(PlayerInteractAtEntityEvent event) {
        if (settings(event.getPlayer().getWorld()).noInteract() && !bypass(event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onArmorStandManipulate(PlayerArmorStandManipulateEvent event) {
        if (settings(event.getPlayer().getWorld()).noInteract() && !bypass(event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockFromTo(BlockFromToEvent event) {
        if (settings(event.getBlock().getWorld()).noFluidFlow()) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onEntityExplode(EntityExplodeEvent event) {
        if (settings(event.getEntity().getWorld()).noExplosions()) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockExplode(BlockExplodeEvent event) {
        if (settings(event.getBlock().getWorld()).noExplosions()) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onWeatherChange(WeatherChangeEvent event) {
        if (settings(event.getWorld()).noWeather() && event.toWeatherState()) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onLightning(LightningStrikeEvent event) {
        if (settings(event.getWorld()).noWeather()) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockIgnite(BlockIgniteEvent event) {
        if (settings(event.getBlock().getWorld()).noFireSpread() && event.getCause() == BlockIgniteEvent.IgniteCause.SPREAD) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockBurn(BlockBurnEvent event) {
        if (settings(event.getBlock().getWorld()).noFireSpread()) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onLeavesDecay(LeavesDecayEvent event) {
        if (settings(event.getBlock().getWorld()).noLeafDecay()) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockGrow(BlockGrowEvent event) {
        if (settings(event.getBlock().getWorld()).noGrowth()) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockSpread(BlockSpreadEvent event) {
        if (settings(event.getBlock().getWorld()).noGrowth()) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onStructureGrow(StructureGrowEvent event) {
        if (settings(event.getWorld()).noGrowth()) {
            event.setCancelled(true);
        }
    }

    // Only mobs: armor stands and other entities of plugins are not touched
    @EventHandler
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        if (event.getEntity() instanceof Mob
                && settings(event.getEntity().getWorld()).noMobSpawn().contains(event.getSpawnReason())) {
            event.setCancelled(true);
        }
    }
}
