package net.bmjo.pathfinder.networking;

import net.bmjo.pathfinder.waypoint.WaypointHandler;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
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
public class ClientNetworking {
    public static void registerPackets() {
        PayloadTypeRegistry.playS2C().register(ServerNetworking.CreateWaypointPayload.ID, ServerNetworking.CreateWaypointPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(ServerNetworking.RemoveWaypointPayload.ID, ServerNetworking.RemoveWaypointPayload.CODEC);

        ClientPlayNetworking.registerGlobalReceiver(ServerNetworking.CreateWaypointPayload.ID, (payload, context) -> {
            UUID uuid = payload.uuid();
            BlockPos blockPos = payload.blockPos();
            String worldIdentifier = payload.dimension();
            RegistryKey<World> world = RegistryKey.of(RegistryKeys.WORLD, Identifier.of(worldIdentifier));
            context.client().execute(() -> WaypointHandler.tryAddWaypoint(uuid, GlobalPos.create(world, blockPos)));
        });

        ClientPlayNetworking.registerGlobalReceiver(ServerNetworking.RemoveWaypointPayload.ID, (payload, context) -> {
            UUID uuid = payload.uuid();
            context.client().execute(() -> WaypointHandler.tryRemoveWaypoint(uuid));
        });
    }
}
