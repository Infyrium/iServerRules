package net.infyrium.iserverrules.listeners;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.github.retrooper.packetevents.event.PacketListenerAbstract;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerPlayerInfoUpdate;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerPlayerInfoUpdate.PlayerInfo;

/**
 * With noPlayer: leaves only the receiver himself in the tab list.
 */
public class PlayerInfoPacketListener extends PacketListenerAbstract {

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
}
