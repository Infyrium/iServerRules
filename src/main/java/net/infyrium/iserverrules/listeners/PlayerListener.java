package net.infyrium.iserverrules.listeners;

import java.util.Locale;
import java.util.Set;

import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import io.papermc.paper.event.player.AsyncChatEvent;
import net.infyrium.iserverrules.iServerRulesMain;
import net.infyrium.iserverrules.models.WorldSettings;
import net.kyori.adventure.text.Component;

public class PlayerListener implements Listener {

    private final iServerRulesMain plugin;

    public PlayerListener(iServerRulesMain plugin) {
        this.plugin = plugin;
    }

    private WorldSettings settings(Player player) {
        return plugin.getSettings().get(player.getWorld());
    }

    private boolean bypass(Player player) {
        return player.hasPermission(iServerRulesMain.BYPASS_PERMISSION);
    }

    private void setGameMode(Player player) {
        GameMode gameMode = settings(player).gameMode();
        if (gameMode != null) {
            player.setGameMode(gameMode);
        }
    }

    // HIGH so it runs after other plugins that set gamemode on join
    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (settings(player).noMessage()) {
            event.joinMessage(null);
        }
        if (plugin.getSettings().noPlayer()) {
            for (Player online : plugin.getServer().getOnlinePlayers()) {
                if (!online.equals(player)) {
                    player.hidePlayer(plugin, online);
                    online.hidePlayer(plugin, player);
                }
            }
        }
        setGameMode(player);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerChangedWorld(PlayerChangedWorldEvent event) {
        setGameMode(event.getPlayer());
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        if (settings(event.getPlayer()).noMessage()) {
            event.quitMessage(null);
        }
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        if (settings(event.getPlayer()).noMessage()) {
            event.deathMessage(null);
        }
    }

    @EventHandler
    public void onPlayerKick(PlayerKickEvent event) {
        if (settings(event.getPlayer()).noMessage()) {
            // Null is not allowed here
            event.leaveMessage(Component.empty());
        }
    }

    // Only players: armor stands and frames are protected by noBreak
    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player player && settings(player).noDamage()) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onFoodLevelChange(FoodLevelChangeEvent event) {
        if (event.getEntity() instanceof Player player && settings(player).noHunger()) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerChat(AsyncChatEvent event) {
        if (settings(event.getPlayer()).noChat() && !bypass(event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
        Set<String> allowed = settings(event.getPlayer()).allowedCommands();
        if (allowed == null || bypass(event.getPlayer())) return;

        String command = event.getMessage().split(" ", 2)[0].substring(1).toLowerCase(Locale.ROOT);
        // "minecraft:help" counts as "help"
        command = command.substring(command.indexOf(':') + 1);
        if (!allowed.contains(command)) {
            event.setCancelled(true);
        }
    }
}
