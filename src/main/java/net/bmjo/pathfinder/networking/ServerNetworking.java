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
    public static final Identifier REMOVE_WAYPOINT = Pathfinder.identifier("remove_waypoint");
    public static final Identifier ADD_PLAYER = Pathfinder.identifier("add_player");
    public static final Identifier REMOVE_PLAYER = Pathfinder.identifier("remove_player");
    public static final Identifier IS_LOADED = Pathfinder.identifier("is_loaded");

    public static void register() {
        ServerPlayNetworking.registerGlobalReceiver(CREATE_WAYPOINT, (server, player, networkHandler, buf, sender) -> {
            UUID uuid = buf.readUuid();
            BlockPos blockPos = buf.readBlockPos();
            server.execute(() -> {
                for (ServerPlayerEntity serverPlayer : getTeamPlayer(server, player)) {
                    PacketByteBuf buffer = PacketByteBufs.create();
                    buffer.writeUuid(uuid);
                    buffer.writeBlockPos(blockPos);
                    ServerPlayNetworking.send(serverPlayer, CREATE_WAYPOINT, buffer);
                }
            });
        });
        ServerPlayNetworking.registerGlobalReceiver(REMOVE_WAYPOINT, (server, player, networkHandler, buf, sender) -> {
            UUID uuid = buf.readUuid();
            server.execute(() -> {
                for (ServerPlayerEntity serverPlayer : getTeamPlayer(server, player)) {
                    PacketByteBuf buffer = PacketByteBufs.create();
                    buffer.writeUuid(uuid);
                    ServerPlayNetworking.send(serverPlayer, REMOVE_WAYPOINT, buffer);
                }
            });
        });
        ServerPlayNetworking.registerGlobalReceiver(ADD_PLAYER, (server, player, networkHandler, buf, sender) -> {
            UUID uuid = buf.readUuid();
            server.execute(() -> server.execute(() -> changeGangPlayer(ADD_PLAYER, server, player, uuid)));
        });
        ServerPlayNetworking.registerGlobalReceiver(REMOVE_PLAYER, (server, player, networkHandler, buf, sender) -> {
            UUID uuid = buf.readUuid();
            server.execute(() -> changeGangPlayer(REMOVE_PLAYER, server, player, uuid));
        });
        ServerPlayNetworking.registerGlobalReceiver(IS_LOADED, (server, player, networkHandler, buf, sender) -> server.execute(() -> ServerPlayNetworking.send(player, IS_LOADED, PacketByteBufs.create())));
    }

    private static List<ServerPlayerEntity> getTeamPlayer(MinecraftServer server, ServerPlayerEntity player) {
        AbstractTeam team = player.getScoreboardTeam();
        return server.getPlayerManager().getPlayerList().stream().filter((playerEntity) -> player == playerEntity || player.getScoreboardTeam() == team).toList();
    }

    private static void changeGangPlayer(Identifier identifier, MinecraftServer server, ServerPlayerEntity player, UUID uuid) {
        ServerPlayerEntity serverPlayer = server.getPlayerManager().getPlayer(uuid);
        if (serverPlayer != null) {
            PacketByteBuf buffer = PacketByteBufs.create();
            buffer.writeUuid(player.getUuid());
            ServerPlayNetworking.send(serverPlayer, identifier, buffer);
        }
    }
}
