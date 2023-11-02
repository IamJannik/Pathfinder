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

public class ServerNetworking {
    public static final Identifier CREATE_WAYPOINT = Pathfinder.identifier("create_waypoint");
    public static final Identifier REMOVE_WAYPOINT = Pathfinder.identifier("remove_waypoint");
    public static final Identifier IS_LOADED = Pathfinder.identifier("is_loaded");

    public static void register() {
        ServerPlayNetworking.registerGlobalReceiver(CREATE_WAYPOINT, (server, player, networkHandler, buf, sender) -> {
            int playerId = buf.readInt();
            BlockPos blockPos = buf.readBlockPos();
            server.execute(() -> {
                for (ServerPlayerEntity serverPlayer : getTeamPlayer(server, player)) {
                    PacketByteBuf buffer = PacketByteBufs.create();
                    buffer.writeInt(playerId);
                    buffer.writeBlockPos(blockPos);
                    ServerPlayNetworking.send(serverPlayer, CREATE_WAYPOINT, buffer);
                }
            });
        });
        ServerPlayNetworking.registerGlobalReceiver(REMOVE_WAYPOINT, (server, player, networkHandler, buf, sender) -> {
            int playerId = buf.readInt();
            server.execute(() -> {
                for (ServerPlayerEntity serverPlayer : getTeamPlayer(server, player)) {
                    PacketByteBuf buffer = PacketByteBufs.create();
                    buffer.writeInt(playerId);
                    ServerPlayNetworking.send(serverPlayer, REMOVE_WAYPOINT, buffer);
                }
            });
        });
        ServerPlayNetworking.registerGlobalReceiver(IS_LOADED, (server, player, networkHandler, buf, sender) -> server.execute(() -> ServerPlayNetworking.send(player, IS_LOADED, PacketByteBufs.create())));
    }

    private static List<ServerPlayerEntity> getTeamPlayer(MinecraftServer server, ServerPlayerEntity player) {
        AbstractTeam team = player.getScoreboardTeam();
        return server.getPlayerManager().getPlayerList().stream().filter((playerEntity) -> player == playerEntity || player.getScoreboardTeam() == team).toList();
    }
}
