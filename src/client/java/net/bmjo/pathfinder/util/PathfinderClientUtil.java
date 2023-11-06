package net.bmjo.pathfinder.util;

import net.bmjo.pathfinder.PathfinderClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.scoreboard.AbstractTeam;

import java.util.Collection;
import java.util.Set;
import java.util.UUID;

public class PathfinderClientUtil {

    public static Collection<String> getTeamPlayers() {
        ClientPlayerEntity player = PathfinderClient.getPlayer();
        if (player != null) {
            AbstractTeam team = player.getScoreboardTeam();
            if (team != null) {
                return team.getPlayerList();
            }
        }
        return Set.of();
    }

    public static boolean isInTeam(UUID uuid) {
        ClientPlayerEntity clientPlayer = PathfinderClient.getPlayer();
        if (clientPlayer == null)
            return false;
        PlayerListEntry playerEntry = clientPlayer.networkHandler.getPlayerListEntry(uuid);
        return playerEntry != null && getTeamPlayers().contains(playerEntry.getProfile().getName());
    }
}
