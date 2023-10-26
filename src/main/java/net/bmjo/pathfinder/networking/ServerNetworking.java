package net.bmjo.pathfinder.networking;

import net.bmjo.pathfinder.Pathfinder;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.scoreboard.AbstractTeam;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

import java.util.List;

public class ServerNetworking {
    public static final Identifier CREATE_WAYPOINT = Pathfinder.identifier("create_waypoint");
    public static final Identifier REMOVE_WAYPOINT = Pathfinder.identifier("remove_waypoint");

    public static void register() {
        ServerPlayNetworking.registerGlobalReceiver(CREATE_WAYPOINT, (server, player, networkHandler, buf, sender) -> {
            PacketByteBuf buffer = PacketByteBufs.create();
            buffer.writeBlockPos(buf.readBlockPos());
            buffer.writeInt(buf.readInt());
            sendToTeam(server, player, buffer);
        });
        ServerPlayNetworking.registerGlobalReceiver(REMOVE_WAYPOINT, (server, player, networkHandler, buf, sender) -> {
            PacketByteBuf buffer = PacketByteBufs.create();
            buffer.writeInt(buf.readInt());
            sendToTeam(server, player, buffer);
        });
    }

    private static void sendToTeam(MinecraftServer server, ServerPlayerEntity player, PacketByteBuf buf) {
        AbstractTeam team = player.getScoreboardTeam();
        List<ServerPlayerEntity> playerList = server.getPlayerManager().getPlayerList().stream().filter((playerEntity) -> player == playerEntity || player.getScoreboardTeam() == team).toList();
        playerList.forEach((serverPlayer) -> ServerPlayNetworking.send(serverPlayer, REMOVE_WAYPOINT, buf));
    }
}
