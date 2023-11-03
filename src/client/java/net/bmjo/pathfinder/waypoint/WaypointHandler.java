package net.bmjo.pathfinder.waypoint;

import net.bmjo.pathfinder.PathfinderClient;
import net.bmjo.pathfinder.gang.GangHandler;
import net.bmjo.pathfinder.networking.ClientNetworking;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.entity.Entity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.scoreboard.AbstractTeam;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class WaypointHandler {
    private static final Map<UUID, Waypoint> WAYPOINTS = new HashMap<>();

    //CREATE

    private static void addWaypoint(UUID owner, BlockPos blockPos) {
        WAYPOINTS.put(owner, Waypoint.create(blockPos, owner));
    }

    public static void tryAddWaypoint(UUID owner, BlockPos blockPos) {
        ClientPlayerEntity clientPlayer = PathfinderClient.getPlayer();
        if (clientPlayer.getUuid().equals(owner))
            return;
        if (PathfinderClient.use_gang) {
            if (GangHandler.isMember(owner))
                addWaypoint(owner, blockPos);
        } else {
            if (isInTeam(owner))
                addWaypoint(owner, blockPos);
        }
    }

    public static boolean createWaypoint() {
        UUID player = PathfinderClient.getPlayer().getUuid();
        HitResult hitResult = raycastWaypoint();
        if (!(hitResult instanceof BlockHitResult blockHitResult))
            return false;
        BlockPos hitPos = blockHitResult.getBlockPos();
        if (WAYPOINTS.containsKey(player) && WAYPOINTS.get(player).pos().isWithinDistance(hitPos, 3)) // TODO or not when near
            deleteWaypoint();
        else {
            addWaypoint(player, hitPos);
            sendCreate(hitPos);
        }
        return true;
    }

    private static void sendCreate(BlockPos blockPos) {
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
                    PlayerListEntry playerEntry = PathfinderClient.getPlayer().networkHandler.getPlayerListEntry(uuid);
                    if (playerEntry != null)
                        sendAddMessage(playerEntry.getProfile().getName(), blockPos);
                }));
            }
        } else {
            ClientPlayerEntity clientPlayer = PathfinderClient.getPlayer();
            if (clientPlayer.getScoreboardTeam() != null) {
                if (PathfinderClient.is_loaded) {
                    ClientPlayNetworking.send(ClientNetworking.CREATE_TEAM_WAYPOINT, PacketByteBufs.create().writeBlockPos(blockPos));
                } else {
                    PathfinderClient.getPlayer().networkHandler.sendChatCommand(String.format("teammsg Lets meet here: X:%d Y:%d Z:%d", blockPos.getX(), blockPos.getY(), blockPos.getZ()));
                }
            } else {
                clientPlayer.sendMessage(Text.literal(String.format("Not in any Team. Join Team or change in <%s> to share Waypoint.", MinecraftClient.getInstance().options.socialInteractionsKey.getBoundKeyLocalizedText().getString())).formatted(Formatting.RED), true);
            }
        }
    }

    private static void sendAddMessage(String player, BlockPos blockPos) {
        PathfinderClient.getPlayer().networkHandler.sendChatCommand(String.format("msg %s Lets meet here: X:%d Y:%d Z:%d", player, blockPos.getX(), blockPos.getY(), blockPos.getZ()));
    }

    // DELETE

    public static void tryRemoveWaypoint(UUID owner) {
        if (owner != PathfinderClient.getPlayer().getUuid())
            removeWaypoint(owner);
    }

    private static void removeWaypoint(UUID owner) {
        WAYPOINTS.remove(owner);
    }

    public static void deleteWaypoint() {
        UUID player = PathfinderClient.getPlayer().getUuid();
        removeWaypoint(player);
        sendDelete();
    }

    private static void sendDelete() {
        if (PathfinderClient.use_gang) {
            if (PathfinderClient.is_loaded) {
                GangHandler.forEach(uuid -> ClientPlayNetworking.send(ClientNetworking.REMOVE_GANG_WAYPOINT, PacketByteBufs.create().writeUuid(uuid)));
            } else {
                GangHandler.forEach((uuid -> {
                    PlayerListEntry playerEntry = PathfinderClient.getPlayer().networkHandler.getPlayerListEntry(uuid);
                    if (playerEntry != null)
                        sendDeleteMessage(playerEntry.getProfile().getName());
                }));
            }
        } else {
            ClientPlayerEntity clientPlayer = PathfinderClient.getPlayer();
            if (clientPlayer.getScoreboardTeam() != null) {
                if (PathfinderClient.is_loaded) {
                    ClientPlayNetworking.send(ClientNetworking.REMOVE_TEAM_WAYPOINT, PacketByteBufs.create());
                } else {
                    clientPlayer.networkHandler.sendChatCommand("teammsg Forget about my meeting point.");
                }
            } else {
                clientPlayer.sendMessage(Text.literal(String.format("Not in any Team. Join Team or change in <%s> to share Waypoint.", MinecraftClient.getInstance().options.socialInteractionsKey.getBoundKeyLocalizedText().getString())).formatted(Formatting.RED), true);
            }
        }
    }

    private static void sendDeleteMessage(String member) {
        PathfinderClient.getPlayer().networkHandler.sendChatCommand(String.format("msg %s Forget about my meeting point.", member));
    }

    // STUFF

    public static void onlyGang() {
        for (UUID uuid : WAYPOINTS.keySet())
            if (!(GangHandler.isMember(uuid) || PathfinderClient.getPlayer().getUuid().equals(uuid)))
                removeWaypoint(uuid);
    }

    public static void onlyTeam() {
        for (UUID uuid : WAYPOINTS.keySet())
            if (!(isInTeam(uuid) || PathfinderClient.getPlayer().getUuid().equals(uuid)))
                removeWaypoint(uuid);
    }

    private static boolean isInTeam(UUID uuid) {
        AbstractTeam team = PathfinderClient.getPlayer().getScoreboardTeam();
        PlayerListEntry playerEntry = PathfinderClient.getPlayer().networkHandler.getPlayerListEntry(uuid);
        return team != null && playerEntry != null && team.getPlayerList().contains(playerEntry.getProfile().getName());
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
        WAYPOINTS.values().forEach((waypoint -> waypoint.render(ctx.camera())));
    }
}
