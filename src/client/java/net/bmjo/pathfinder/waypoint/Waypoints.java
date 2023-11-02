package net.bmjo.pathfinder.waypoint;

import net.bmjo.pathfinder.PathfinderClient;
import net.bmjo.pathfinder.networking.ClientNetworking;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;

import java.util.HashMap;
import java.util.Map;

public class Waypoints {
    static final Map<Integer, Waypoint> WAYPOINTS = new HashMap<>();

    public static boolean createWaypoint() {
        int playerId = PathfinderClient.getPlayer().getId();
        HitResult hitResult = raycastWaypoint();
        if (!(hitResult instanceof BlockHitResult blockHitResult))
            return false;
        BlockPos blockPos = blockHitResult.getBlockPos();
        if (WAYPOINTS.containsKey(playerId) && WAYPOINTS.get(playerId).pos().isWithinDistance(blockPos, 3)) // TODO or not when near
            deleteWaypoint();
        else {
            addWaypoint(playerId, blockPos, true);
            sendToOthers(playerId, blockPos);
        }
        return true;
    }

    private static void sendToOthers(int playerId, BlockPos blockPos) {
        if (false) {
            PacketByteBuf buf = PacketByteBufs.create();
            buf.writeInt(playerId);
            buf.writeBlockPos(blockPos);
            ClientPlayNetworking.send(ClientNetworking.CREATE_WAYPOINT, buf);
        } else {
            if (PathfinderClient.use_team) {
                PathfinderClient.getPlayer().networkHandler.sendChatCommand(String.format("teammsg Lets meet here: X:%d Y:%d Z:%d", blockPos.getX(), blockPos.getY(), blockPos.getZ()));
            } else {

            }
        }
    }

    public static void deleteWaypoint() {
        int playerID = PathfinderClient.getPlayer().getId();
        removeWaypoint(playerID);
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeInt(playerID);
        ClientPlayNetworking.send(ClientNetworking.REMOVE_WAYPOINT, buf);
    }

    public static void addWaypoint(int playerId, BlockPos blockPos) {
        addWaypoint(playerId, blockPos, false);
    }

    private static void addWaypoint(int playerId, BlockPos blockPos, boolean own) {
        if (own || playerId != PathfinderClient.getPlayer().getId())
            WAYPOINTS.put(playerId, Waypoint.create(blockPos, playerId));
    }

    public static void removeWaypoint(int playerId) {
        WAYPOINTS.remove(playerId);
    }

    private static HitResult raycastWaypoint() {
        Entity entity = MinecraftClient.getInstance().getCameraEntity();
        assert entity != null;
        return entity.raycast(MinecraftClient.getInstance().options.getViewDistance().getValue() * 16, 1.0F, false);
    }

    public static void update() {
        for (Waypoint waypoint : WAYPOINTS.values())
            if (waypoint.tryRemove())
                removeWaypoint(waypoint.player());
    }

    public static void render(WorldRenderContext ctx) {
        WAYPOINTS.values().forEach((waypoint -> waypoint.render(ctx.matrixStack(), ctx.consumers(), ctx.camera())));
    }
}
