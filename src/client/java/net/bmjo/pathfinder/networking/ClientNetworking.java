package net.bmjo.pathfinder.networking;

import net.bmjo.pathfinder.PathfinderClient;
import net.bmjo.pathfinder.gang.GangHandler;
import net.bmjo.pathfinder.waypoint.WaypointHandler;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

import java.util.UUID;

public class ClientNetworking {
    public static final Identifier CREATE_WAYPOINT = PathfinderClient.identifier("create_waypoint");
    public static final Identifier REMOVE_WAYPOINT = PathfinderClient.identifier("remove_waypoint");
    public static final Identifier ADD_PLAYER = PathfinderClient.identifier("add_player");
    public static final Identifier REMOVE_PLAYER = PathfinderClient.identifier("remove_player");
    public static final Identifier IS_LOADED = PathfinderClient.identifier("is_loaded");

    public static void register() {
        ClientPlayNetworking.registerGlobalReceiver(CREATE_WAYPOINT, (client, handler, buf, responseSender) -> {
            UUID uuid = buf.readUuid();
            BlockPos blockPos = buf.readBlockPos();
            client.execute(() -> WaypointHandler.addWaypoint(uuid, blockPos));
        });
        ClientPlayNetworking.registerGlobalReceiver(REMOVE_WAYPOINT, (client, handler, buf, responseSender) -> {
            UUID uuid = buf.readUuid();
            client.execute(() -> WaypointHandler.removeWaypoint(uuid));
        });
        ClientPlayNetworking.registerGlobalReceiver(ADD_PLAYER, (client, handler, buf, responseSender) -> {
            UUID uuid = buf.readUuid();
            client.execute(() -> GangHandler.addMember(uuid));
        });
        ClientPlayNetworking.registerGlobalReceiver(REMOVE_PLAYER, (client, handler, buf, responseSender) -> {
            UUID uuid = buf.readUuid();
            client.execute(() -> GangHandler.removeMember(uuid));
        });
        ClientPlayNetworking.registerGlobalReceiver(IS_LOADED, (client, handler, buf, responseSender) -> PathfinderClient.is_loaded = true);
    }
}
