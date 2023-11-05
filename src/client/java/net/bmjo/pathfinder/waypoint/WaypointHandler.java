package net.bmjo.pathfinder.waypoint;

import net.bmjo.pathfinder.PathfinderClient;
import net.bmjo.pathfinder.gang.GangHandler;
import net.bmjo.pathfinder.networking.ClientNetworking;
import net.bmjo.pathfinder.uil.PathfinderClientUtil;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.entity.Entity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;

import java.util.*;

public class WaypointHandler {
    private static final Map<UUID, Waypoint> WAYPOINTS = new HashMap<>();

    //CREATE

    private static void addWaypoint(UUID owner, BlockPos blockPos) {
        WAYPOINTS.put(owner, Waypoint.create(blockPos, owner));
    }

    public static void tryAddWaypoint(UUID owner, BlockPos blockPos) {
        ClientPlayerEntity clientPlayer = PathfinderClient.getPlayer();
        if (clientPlayer != null && clientPlayer.getUuid().equals(owner))
            return;
        if (PathfinderClient.use_gang) {
            if (GangHandler.isMember(owner))
                addWaypoint(owner, blockPos);
        } else {
            if (PathfinderClientUtil.isInTeam(owner))
                addWaypoint(owner, blockPos);
        }
    }

    public static boolean createWaypoint() {
        ClientPlayerEntity player = PathfinderClient.getPlayer();
        HitResult hitResult = raycastWaypoint();
        if (player == null || !(hitResult instanceof BlockHitResult blockHitResult))
            return false;
        UUID uuid = player.getUuid();
        BlockPos hitPos = blockHitResult.getBlockPos();
        if (WAYPOINTS.containsKey(uuid) && WAYPOINTS.get(uuid).pos().isWithinDistance(hitPos, 3)) // TODO or not when near
            deleteWaypoint();
        else {
            addWaypoint(uuid, hitPos);
            sendCreate(hitPos);
        }
        return true;
    }

    private static void sendCreate(BlockPos blockPos) {
        ClientPlayerEntity player = PathfinderClient.getPlayer();
        if (player == null)
            return;
        if (PathfinderClient.use_gang) {
            if (PathfinderClient.is_loaded) {
                GangHandler.forEach(uuid -> {
                    PacketByteBuf buf = PacketByteBufs.create();
                    buf.writeUuid(uuid);
                    buf.writeBlockPos(blockPos);
                    ClientPlayNetworking.send(ClientNetworking.CREATE_GANG_WAYPOINT, buf);
                });
            } else {
                GangHandler.forEach((uuid -> {
                    PlayerListEntry member = player.networkHandler.getPlayerListEntry(uuid);
                    if (member != null)
                        player.networkHandler.sendChatCommand(String.format("msg %s Lets meet here: X:%d Y:%d Z:%d", member.getProfile().getName(), blockPos.getX(), blockPos.getY(), blockPos.getZ()));
                }));
            }
        } else {
            if (player.getScoreboardTeam() != null) {
                if (PathfinderClient.is_loaded) {
                    ClientPlayNetworking.send(ClientNetworking.CREATE_TEAM_WAYPOINT, PacketByteBufs.create().writeBlockPos(blockPos));
                } else {
                    PathfinderClient.getPlayer().networkHandler.sendChatCommand(String.format("teammsg Lets meet here: X:%d Y:%d Z:%d", blockPos.getX(), blockPos.getY(), blockPos.getZ()));
                }
            } else {
                player.sendMessage(Text.literal(String.format("Not in any Team. Join Team or change in <%s> to share Waypoint.", MinecraftClient.getInstance().options.socialInteractionsKey.getBoundKeyLocalizedText().getString())).formatted(Formatting.RED), true);
            }
        }
    }

    // DELETE

    public static void tryRemoveWaypoint(UUID owner) {
        ClientPlayerEntity clientPlayer = PathfinderClient.getPlayer();
        if (clientPlayer != null && owner != clientPlayer.getUuid())
            removeWaypoint(owner);
    }

    private static void removeWaypoint(UUID owner) {
        WAYPOINTS.remove(owner);
    }

    public static void deleteWaypoint() {
        ClientPlayerEntity player = PathfinderClient.getPlayer();
        if (player != null) {
            UUID uuid = player.getUuid();
            removeWaypoint(uuid);
            sendDelete();
        }
    }

    private static void sendDelete() {
        ClientPlayerEntity player = PathfinderClient.getPlayer();
        if (player == null)
            return;
        if (PathfinderClient.use_gang) {
            if (PathfinderClient.is_loaded) {
                GangHandler.forEach(uuid -> ClientPlayNetworking.send(ClientNetworking.REMOVE_GANG_WAYPOINT, PacketByteBufs.create().writeUuid(uuid)));
            } else {
                GangHandler.forEach((uuid -> {
                    PlayerListEntry member = player.networkHandler.getPlayerListEntry(uuid);
                    if (member != null)
                        player.networkHandler.sendChatCommand(String.format("msg %s Forget about my meeting point.", member.getProfile().getName()));
                }));
            }
        } else {
            if (player.getScoreboardTeam() != null) {
                if (PathfinderClient.is_loaded) {
                    ClientPlayNetworking.send(ClientNetworking.REMOVE_TEAM_WAYPOINT, PacketByteBufs.create());
                } else {
                    player.networkHandler.sendChatCommand("teammsg Forget about my meeting point.");
                }
            } else {
                player.sendMessage(Text.literal(String.format("Not in any Team. Join Team or change in <%s> to share Waypoint.", MinecraftClient.getInstance().options.socialInteractionsKey.getBoundKeyLocalizedText().getString())).formatted(Formatting.RED), true);
            }
        }
    }

    // STUFF

    public static void onlyGang() {
        ClientPlayerEntity clientPlayer = PathfinderClient.getPlayer();
        if (clientPlayer == null)
            return;
        for (UUID uuid : WAYPOINTS.keySet())
            if (!(GangHandler.isMember(uuid) || clientPlayer.getUuid().equals(uuid)))
                removeWaypoint(uuid);
    }

    public static void onlyTeam() {
        ClientPlayerEntity clientPlayer = PathfinderClient.getPlayer();
        if (clientPlayer == null)
            return;
        for (UUID uuid : WAYPOINTS.keySet())
            if (!(PathfinderClientUtil.isInTeam(uuid) || clientPlayer.getUuid().equals(uuid)))
                removeWaypoint(uuid);
    }

    private static HitResult raycastWaypoint() {
        Entity entity = MinecraftClient.getInstance().getCameraEntity();
        assert entity != null;
        return entity.raycast(MinecraftClient.getInstance().options.getViewDistance().getValue() * 16, 1.0F, false);
    }

    public static void update() {
        Set<UUID> remove = new HashSet<>(WAYPOINTS.size());
        for (Map.Entry<UUID, Waypoint> waypoint : WAYPOINTS.entrySet())
            if (waypoint.getValue().tryRemove())
                remove.add(waypoint.getKey());
        for (UUID uuid : remove)
            removeWaypoint(uuid);
    }

    public static void render(WorldRenderContext ctx) {
        WAYPOINTS.values().forEach((waypoint -> waypoint.render(ctx.camera())));
    }
}
