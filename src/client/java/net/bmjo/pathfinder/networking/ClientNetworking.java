package net.bmjo.pathfinder.networking;

import net.bmjo.pathfinder.networking.payload.WaypointPayload;
import net.bmjo.pathfinder.waypoint.WaypointHandler;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.world.World;

import java.util.UUID;

/**
 * Client Networking of Pathfinder.
 */
@SuppressWarnings("resource")
public class ClientNetworking {
    public static void registerPackets() {
        WaypointPayload.register();

        ClientPlayNetworking.registerGlobalReceiver(WaypointPayload.CreatePos.ID, (payload, context) -> {
            UUID uuid = payload.uuid();
            BlockPos blockPos = payload.blockPos();
            String worldIdentifier = payload.dimension();
            RegistryKey<World> world = RegistryKey.of(RegistryKeys.WORLD, Identifier.of(worldIdentifier));
            context.client().execute(() -> WaypointHandler.tryAddPosWaypoint(uuid, GlobalPos.create(world, blockPos)));
        });

        ClientPlayNetworking.registerGlobalReceiver(WaypointPayload.CreateEntity.ID, (payload, context) -> {
            UUID uuid = payload.uuid();
            int entityId = payload.entityId();
            context.client().execute(() -> WaypointHandler.tryAddEntityWaypoint(uuid, entityId));
        });

        ClientPlayNetworking.registerGlobalReceiver(WaypointPayload.Remove.ID, (payload, context) -> {
            UUID uuid = payload.uuid();
            context.client().execute(() -> WaypointHandler.tryRemoveWaypoint(uuid));
        });
    }
}
