package net.bmjo.pathfinder.util;

import net.bmjo.pathfinder.PathfinderClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.scoreboard.AbstractTeam;

import java.util.Collection;
import java.util.Set;
import java.util.UUID;

/**
 * Utility class for Pathfinder client-related operations.
 *
 * @author BMJO
 * @version 1.0
 */
public class PathfinderClientUtil {
    /**
     * Gets the names of players in the same minecraft team as the client player.
     *
     * @return A collection of player names in the team, or an empty set if not in a team.
     */
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

    /**
     * Checks if a player with the given UUID is in the same team as the client player.
     *
     * @param uuid The UUID of the player to check.
     * @return True if the player is in the same team, false otherwise or if the client player is null.
     */
    public static boolean isInTeam(UUID uuid) {
        ClientPlayerEntity clientPlayer = PathfinderClient.getPlayer();
        if (clientPlayer == null)
            return false;
        PlayerListEntry playerEntry = clientPlayer.networkHandler.getPlayerListEntry(uuid);
        return playerEntry != null && getTeamPlayers().contains(playerEntry.getProfile().getName());
    }
}
