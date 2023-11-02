package net.bmjo.pathfinder.networking;

import net.bmjo.pathfinder.PathfinderClient;
import net.bmjo.pathfinder.waypoint.Waypoints;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

public class ClientNetworking {
    public static final Identifier CREATE_WAYPOINT = PathfinderClient.identifier("create_waypoint");
    public static final Identifier REMOVE_WAYPOINT = PathfinderClient.identifier("remove_waypoint");
    public static final Identifier IS_LOADED = PathfinderClient.identifier("is_loaded");

    public static void register() {
        ClientPlayNetworking.registerGlobalReceiver(CREATE_WAYPOINT, (client, handler, buf, responseSender) -> {
            int playerID = buf.readInt();
            BlockPos blockPos = buf.readBlockPos();
            client.execute(() -> Waypoints.addWaypoint(playerID, blockPos));
        });
        ClientPlayNetworking.registerGlobalReceiver(REMOVE_WAYPOINT, (client, handler, buf, responseSender) -> {
            int playerID = buf.readInt();
            client.execute(() -> Waypoints.removeWaypoint(playerID));
        });
        ClientPlayNetworking.registerGlobalReceiver(IS_LOADED, (client, handler, buf, responseSender) -> PathfinderClient.is_loaded = true);
    }
}
