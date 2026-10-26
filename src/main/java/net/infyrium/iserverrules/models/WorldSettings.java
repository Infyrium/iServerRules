package net.infyrium.iserverrules.models;

import java.util.Set;

import org.bukkit.GameMode;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;

/**
 * Settings of one world: values from worlds.{world} with fallback to default.
 *
 * @param gameMode null - don't change
 * @param allowedCommands null - all commands are allowed
 * @param fixedTime null - time goes as usual
 */
public record WorldSettings(
        boolean noDamage,
        boolean noHunger,
        boolean noMessage,
        boolean noChat,
        GameMode gameMode,
        Set<String> allowedCommands,
        boolean noBreak,
        boolean noPlace,
        boolean noInteract,
        boolean noFluidFlow,
        boolean noExplosions,
        boolean noWeather,
        boolean noFireSpread,
        boolean noLeafDecay,
        boolean noGrowth,
        boolean noMobGriefing,
        Set<SpawnReason> noMobSpawn,
        Long fixedTime) {
}
