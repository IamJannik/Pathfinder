package net.bmjo.pathfinder.networking;

import net.bmjo.pathfinder.Pathfinder;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.scoreboard.AbstractTeam;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

import java.util.List;
import java.util.UUID;

public class ServerNetworking {
    public static final Identifier CREATE_WAYPOINT = Pathfinder.identifier("create_waypoint");
    public static final Identifier CREATE_GANG_WAYPOINT = Pathfinder.identifier("create_gang_waypoint");
    public static final Identifier CREATE_TEAM_WAYPOINT = Pathfinder.identifier("create_team_waypoint");
    public static final Identifier REMOVE_WAYPOINT = Pathfinder.identifier("remove_waypoint");
    public static final Identifier REMOVE_GANG_WAYPOINT = Pathfinder.identifier("remove_gang_waypoint");
    public static final Identifier REMOVE_TEAM_WAYPOINT = Pathfinder.identifier("remove_team_waypoint");
    public static final Identifier IS_LOADED = Pathfinder.identifier("is_loaded");

    public static void register() {
        ServerPlayNetworking.registerGlobalReceiver(CREATE_GANG_WAYPOINT, (server, player, networkHandler, buf, sender) -> {
            UUID uuid = buf.readUuid();
            BlockPos blockPos = buf.readBlockPos();
            server.execute(() -> {
                ServerPlayerEntity serverPlayer = server.getPlayerManager().getPlayer(uuid);
                if (serverPlayer != null) {
                    PacketByteBuf buffer = PacketByteBufs.create();
                    buffer.writeUuid(player.getUuid());
                    buffer.writeBlockPos(blockPos);
                    ServerPlayNetworking.send(serverPlayer, CREATE_WAYPOINT, buffer);
                }
            });
        });
        ServerPlayNetworking.registerGlobalReceiver(CREATE_TEAM_WAYPOINT, (server, player, networkHandler, buf, sender) -> {
            BlockPos blockPos = buf.readBlockPos();
            server.execute(() -> {
                for (ServerPlayerEntity serverPlayer : getTeamPlayer(server, player)) {
                    PacketByteBuf buffer = PacketByteBufs.create();
                    buffer.writeUuid(player.getUuid());
                    buffer.writeBlockPos(blockPos);
                    ServerPlayNetworking.send(serverPlayer, CREATE_WAYPOINT, buffer);
                }
            });
        });
        ServerPlayNetworking.registerGlobalReceiver(REMOVE_GANG_WAYPOINT, (server, player, networkHandler, buf, sender) -> {
            UUID uuid = buf.readUuid();
            server.execute(() -> {
                ServerPlayerEntity serverPlayer = server.getPlayerManager().getPlayer(uuid);
                if (serverPlayer != null) {
                    PacketByteBuf buffer = PacketByteBufs.create();
                    buffer.writeUuid(player.getUuid());
                    ServerPlayNetworking.send(serverPlayer, REMOVE_WAYPOINT, buffer);
                }
            });
        });
        ServerPlayNetworking.registerGlobalReceiver(REMOVE_TEAM_WAYPOINT, (server, player, networkHandler, buf, sender) -> {
            server.execute(() -> {
                for (ServerPlayerEntity serverPlayer : getTeamPlayer(server, player)) {
                    PacketByteBuf buffer = PacketByteBufs.create();
                    buffer.writeUuid(player.getUuid());
                    ServerPlayNetworking.send(serverPlayer, REMOVE_WAYPOINT, buffer);
                }
            });
        });
        ServerPlayNetworking.registerGlobalReceiver(IS_LOADED, (server, player, networkHandler, buf, sender) -> server.execute(() -> ServerPlayNetworking.send(player, IS_LOADED, PacketByteBufs.create())));
    }

    private static List<ServerPlayerEntity> getTeamPlayer(MinecraftServer server, ServerPlayerEntity player) {
        AbstractTeam team = player.getScoreboardTeam();
        return server.getPlayerManager().getPlayerList().stream().filter((playerEntity) -> playerEntity == player || playerEntity.getScoreboardTeam() == team).toList();
    }
}
