package net.bmjo.pathfinder.gang;

import net.bmjo.pathfinder.waypoint.WaypointHandler;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * Manages the in-house team system from Pathfinder "Gang".
 *
 * @author BMJO
 * @version 1.2
 */
public class GangHandler {
    private static final HashSet<UUID> MEMBERS = new HashSet<>();

    /**
     * Gets a collection of UUIDs representing the members of the gang.
     *
     * @return A collection of UUIDs representing the gang members.
     */
    public static Collection<UUID> members() {
        return Set.copyOf(MEMBERS);
    }

    /**
     * Adds a member to the gang.
     *
     * @param member The UUID of the member to be added.
     */
    public static void addMember(UUID member) {
        MEMBERS.add(member);
    }

    /**
     * Removes a member from the gang and attempts to remove their waypoint.
     *
     * @param member The UUID of the member to be removed.
     */
    public static void removeMember(UUID member) {
        MEMBERS.remove(member);
        WaypointHandler.tryRemoveWaypoint(member);
    }

    /**
     * Checks if a player with the given UUID is a member of the gang.
     *
     * @param member The UUID of the player to check.
     * @return True if the player is a member of the gang, false otherwise.
     */
    public static boolean isMember(UUID member) {
        return MEMBERS.contains(member);
    }

    /**
     * Performs the given action for each member of the gang.
     *
     * @param action The action to be performed for each member.
     */
    public static void forEach(Consumer<? super UUID> action) {
        MEMBERS.forEach(action);
    }
}
