package net.bmjo.pathfinder.gang;

import net.bmjo.pathfinder.waypoint.WaypointHandler;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;

public class GangHandler {
    private static final HashSet<UUID> MEMBERS = new HashSet<>();

    public static Collection<UUID> members() {
        return Set.copyOf(MEMBERS);
    }

    public static void addMember(UUID member) {
        MEMBERS.add(member);
    }

    public static void removeMember(UUID member) {
        MEMBERS.remove(member);
        WaypointHandler.tryRemoveWaypoint(member);
    }

    public static boolean isMember(UUID member) {
        return MEMBERS.contains(member);
    }

    public static void forEach(Consumer<? super UUID> action) {
        MEMBERS.forEach(action);
    }

}
