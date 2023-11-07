package net.bmjo.pathfinder.networking;

import net.bmjo.pathfinder.PathfinderClient;
import net.bmjo.pathfinder.waypoint.WaypointHandler;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

import java.util.UUID;

public class ClientNetworking {
    public static final Identifier CREATE_WAYPOINT = PathfinderClient.identifier("create_waypoint");
    public static final Identifier CREATE_GANG_WAYPOINT = PathfinderClient.identifier("create_gang_waypoint");
    public static final Identifier CREATE_TEAM_WAYPOINT = PathfinderClient.identifier("create_team_waypoint");
    public static final Identifier REMOVE_WAYPOINT = PathfinderClient.identifier("remove_waypoint");
    public static final Identifier REMOVE_GANG_WAYPOINT = PathfinderClient.identifier("remove_gang_waypoint");
    public static final Identifier REMOVE_TEAM_WAYPOINT = PathfinderClient.identifier("remove_team_waypoint");
    public static final Identifier IS_LOADED = PathfinderClient.identifier("is_loaded");

    public static void register() {
        ClientPlayNetworking.registerGlobalReceiver(CREATE_WAYPOINT, (client, handler, buf, responseSender) -> {
            UUID uuid = buf.readUuid();
            BlockPos blockPos = buf.readBlockPos();
            client.execute(() -> WaypointHandler.tryAddWaypoint(uuid, blockPos));
        });
        ClientPlayNetworking.registerGlobalReceiver(REMOVE_WAYPOINT, (client, handler, buf, responseSender) -> {
            UUID uuid = buf.readUuid();
            client.execute(() -> WaypointHandler.tryRemoveWaypoint(uuid));
        });
        ClientPlayNetworking.registerGlobalReceiver(IS_LOADED, (client, handler, buf, responseSender) -> PathfinderClient.is_loaded = true);
    }
}
