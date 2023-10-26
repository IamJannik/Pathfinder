package net.bmjo.pathfinder.networking;

import net.bmjo.pathfinder.PathfinderClient;
import net.bmjo.pathfinder.waypoint.Waypoints;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.util.Identifier;

public class ClientNetworking {
    public static final Identifier CREATE_WAYPOINT = PathfinderClient.identifier("create_waypoint");
    public static final Identifier REMOVE_WAYPOINT = PathfinderClient.identifier("remove_waypoint");

    public static void register() {
        ClientPlayNetworking.registerGlobalReceiver(CREATE_WAYPOINT, (client, handler, buf, responseSender) -> Waypoints.addWaypoint(buf.readInt(), buf.readBlockPos()));
        ClientPlayNetworking.registerGlobalReceiver(REMOVE_WAYPOINT, (client, handler, buf, responseSender) -> Waypoints.removeWaypoint(buf.readInt()));
    }
}
