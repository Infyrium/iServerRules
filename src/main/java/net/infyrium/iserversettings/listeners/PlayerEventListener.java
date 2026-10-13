package net.infyrium.iserversettings.listeners;

import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import io.papermc.paper.event.player.AsyncChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.event.PacketListenerAbstract;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerPlayerInfoUpdate;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerPlayerInfoUpdate.PlayerInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import net.infyrium.iserversettings.iServerSettingsMain;
import net.kyori.adventure.text.Component;

public class PlayerEventListener implements Listener {

    private final iServerSettingsMain plugin;

    private final boolean noPlayer;
    private final boolean noMessage;
    private final boolean noDamage;
    private final boolean noHunger;
    private final boolean noChat;
    private final boolean noCommands;
    private final String gameModeStr;
    private final List<String> allowedCommands;

    public PlayerEventListener(iServerSettingsMain plugin) {
        this.plugin = plugin;

        var config = plugin.getConfig();
        this.noPlayer = config.getBoolean("settings.player.noPlayer");
        this.noMessage = config.getBoolean("settings.player.noMessage");
        this.noDamage = config.getBoolean("settings.player.noDamage");
        this.noHunger = config.getBoolean("settings.player.noHunger");
        this.noChat = config.getBoolean("settings.player.noChat");
        this.noCommands = config.getBoolean("settings.player.noCommands.enabled");
        this.allowedCommands = config.getStringList("settings.player.noCommands.allowedCommands");
        this.gameModeStr = config.getString("settings.player.gameMode");

        if (noPlayer) {
            PacketEvents.getAPI().getEventManager().registerListener(new PacketListenerAbstract() {
                @Override
                public void onPacketSend(PacketSendEvent event) {
                    if (event.getPacketType() != PacketType.Play.Server.PLAYER_INFO_UPDATE) return;

                    UUID receiverUUID = event.getUser().getUUID();
                    WrapperPlayServerPlayerInfoUpdate wrapper = new WrapperPlayServerPlayerInfoUpdate(event);

                    List<PlayerInfo> filtered = new ArrayList<>();
                    for (PlayerInfo entry : wrapper.getEntries()) {
                        if (!entry.getGameProfile().getUUID().equals(receiverUUID)) continue;

                        if (wrapper.getActions().contains(WrapperPlayServerPlayerInfoUpdate.Action.UPDATE_LISTED)) {
                            filtered.add(new PlayerInfo(
                                entry.getGameProfile(),
                                false,
                                entry.getLatency(),
                                entry.getGameMode(),
                                entry.getDisplayName(),
                                entry.getChatSession()
                            ));
                        } else {
                            filtered.add(entry);
                        }
                    }

                    if (filtered.isEmpty()) {
                        event.setCancelled(true);
                    } else {
                        wrapper.setEntries(filtered);
                    }
                }
            });
        }
    }

    private void setPlayerGameMode(Player player) {
        if (gameModeStr == null) return;
        try {
            GameMode gameMode = GameMode.valueOf(gameModeStr.toUpperCase());
            player.setGameMode(gameMode);
        } catch (IllegalArgumentException e) {
            plugin.getLogger().warning("Incorrect gamemode in the config: " + gameModeStr);
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (noMessage) {
            event.joinMessage(null);
        }
        if (noPlayer) {
            for (Player online : plugin.getServer().getOnlinePlayers()) {
                if (!online.equals(player)) {
                    player.hidePlayer(plugin, online);
                    online.hidePlayer(plugin, player);
                }
            }
        }
        setPlayerGameMode(player);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        if (noMessage) {
            event.quitMessage(null);
        }
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        if (noMessage) {
            event.deathMessage(null);
        }
    }

    @EventHandler
    public void onPlayerKick(PlayerKickEvent event) {
        if (noMessage) {
            // Null is not allowed here
            event.leaveMessage(Component.empty());
        }
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if (noDamage) {
            event.setCancelled(true);
        }
    }
    @EventHandler
    public void onFoodLevelChange(FoodLevelChangeEvent event) {
        if (noHunger) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerChat(AsyncChatEvent event) {
        if (noChat) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
        if (noCommands) {
            String command = event.getMessage().split(" ")[0].substring(1).toLowerCase();
            if (!allowedCommands.contains(command)) {
                event.setCancelled(true);
            }
        }
    }
}
