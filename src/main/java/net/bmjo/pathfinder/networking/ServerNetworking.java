package net.bmjo.pathfinder.networking;

import net.bmjo.pathfinder.networking.payload.GangWaypointPayload;
import net.bmjo.pathfinder.networking.payload.TeamWaypointPayload;
import net.bmjo.pathfinder.networking.payload.WaypointPayload;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.scoreboard.AbstractTeam;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;

import java.util.List;
import java.util.UUID;

/**
 * Server Networking of Pathfinder.
 */
@SuppressWarnings("resource")
public class ServerNetworking {
    public static void register() {
        GangWaypointPayload.register();
        TeamWaypointPayload.register();

        ServerPlayNetworking.registerGlobalReceiver(GangWaypointPayload.CreatePos.ID, (payload, context) -> {
            UUID uuid = payload.uuid();
            BlockPos blockPos = payload.blockPos();
            String dimension = payload.dimension();
            MinecraftServer server = context.server();
            server.execute(() -> {
                ServerPlayerEntity serverPlayer = server.getPlayerManager().getPlayer(uuid);
                if (serverPlayer != null) {
                    ServerPlayNetworking.send(serverPlayer, new WaypointPayload.CreatePos(uuid, blockPos, dimension));
                }
            });
        });

        ServerPlayNetworking.registerGlobalReceiver(GangWaypointPayload.CreateEntity.ID, (payload, context) -> {
            UUID uuid = payload.uuid();
            int entityId = payload.entityId();
            MinecraftServer server = context.server();
            server.execute(() -> {
                ServerPlayerEntity serverPlayer = server.getPlayerManager().getPlayer(uuid);
                if (serverPlayer != null) {
                    ServerPlayNetworking.send(serverPlayer, new WaypointPayload.CreateEntity(uuid, entityId));
                }
            });
        });

        ServerPlayNetworking.registerGlobalReceiver(GangWaypointPayload.Remove.ID, (payload, context) -> {
            ServerPlayerEntity sender = context.player();
            UUID uuid = payload.uuid();
            MinecraftServer server = context.server();
            server.execute(() -> {
                ServerPlayerEntity serverPlayer = server.getPlayerManager().getPlayer(uuid);
                if (serverPlayer != null) {
                    ServerPlayNetworking.send(serverPlayer, new WaypointPayload.Remove(sender.getUuid()));
                }
            });
        });

        ServerPlayNetworking.registerGlobalReceiver(TeamWaypointPayload.CreatePos.ID, (payload, context) -> {
            ServerPlayerEntity sender = context.player();
            BlockPos blockPos = payload.blockPos();
            String dimension = payload.dimension();
            MinecraftServer server = context.server();
            server.execute(() -> {
                for (ServerPlayerEntity serverPlayer : getTeamPlayer(server, sender)) {
                    ServerPlayNetworking.send(serverPlayer, new WaypointPayload.CreatePos(sender.getUuid(), blockPos, dimension));
                }
            });
        });

        ServerPlayNetworking.registerGlobalReceiver(TeamWaypointPayload.CreateEntity.ID, (payload, context) -> {
            ServerPlayerEntity sender = context.player();
            int entityId = payload.entityId();
            MinecraftServer server = context.server();
            server.execute(() -> {
                for (ServerPlayerEntity serverPlayer : getTeamPlayer(server, sender)) {
                    ServerPlayNetworking.send(serverPlayer, new WaypointPayload.CreateEntity(sender.getUuid(), entityId));
                }
            });
        });

        ServerPlayNetworking.registerGlobalReceiver(TeamWaypointPayload.Remove.ID, (payload, context) -> {
            ServerPlayerEntity sender = context.player();
            MinecraftServer server = context.server();
            server.execute(() -> {
                for (ServerPlayerEntity serverPlayer : getTeamPlayer(server, sender)) {
                    ServerPlayNetworking.send(serverPlayer, new WaypointPayload.Remove(sender.getUuid()));
                }
            });
        });
    }

    private static List<ServerPlayerEntity> getTeamPlayer(MinecraftServer server, ServerPlayerEntity player) {
        AbstractTeam team = player.getScoreboardTeam();
        return server.getPlayerManager().getPlayerList().stream().filter((playerEntity) -> playerEntity == player || playerEntity.getScoreboardTeam() == team).toList();
    }
}
