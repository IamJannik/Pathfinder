package net.bmjo.pathfinder.waypoint;

import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.util.math.BlockPos;

import java.util.HashMap;
import java.util.Map;

public class Waypoints {
    static final Map<Integer, Waypoint> WAYPOINTS = new HashMap<>();

    public static void addWaypoint(int playerId, BlockPos blockPos) {
        WAYPOINTS.put(playerId, Waypoint.create(blockPos, playerId));
    }

    public static void removeWaypoint(int playerId) {
        WAYPOINTS.remove(playerId);
    }

    public static void update() {
        for (Waypoint waypoint : WAYPOINTS.values())
            if (System.currentTimeMillis() - waypoint.created() >= 10 * 60 * 1000)
                removeWaypoint(waypoint.player());
    }

    public static void render(WorldRenderContext ctx) {
        for (Waypoint waypoint : WAYPOINTS.values())
            WaypointRenderer.render(waypoint, ctx);
    }
}
