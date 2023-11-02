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
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class WaypointHandler {
    static final Map<UUID, Waypoint> WAYPOINTS = new HashMap<>();

    public static boolean createWaypoint() {
        UUID player = PathfinderClient.getPlayer().getUuid();
        HitResult hitResult = raycastWaypoint();
        if (!(hitResult instanceof BlockHitResult blockHitResult))
            return false;
        BlockPos blockPos = blockHitResult.getBlockPos();
        if (WAYPOINTS.containsKey(player) && WAYPOINTS.get(player).pos().isWithinDistance(blockPos, 3)) // TODO or not when near
            deleteWaypoint();
        else {
            addWaypoint(player, blockPos, true);
            sendCreate(player, blockPos);
        }
        return true;
    }

    private static void sendCreate(UUID player, BlockPos blockPos) {
        if (PathfinderClient.is_loaded) {
            PacketByteBuf buf = PacketByteBufs.create();
            buf.writeUuid(player);
            buf.writeBlockPos(blockPos);
            ClientPlayNetworking.send(ClientNetworking.CREATE_WAYPOINT, buf);
        } else {
            if (PathfinderClient.use_gang) {
                for (UUID uuid : GangHandler.members) {
                    PlayerListEntry playerEntry = PathfinderClient.getPlayer().networkHandler.getPlayerListEntry(uuid);
                    if (playerEntry != null)
                        sendAddMessage(playerEntry.getProfile().getName(), blockPos);
                }
            } else {
                ClientPlayerEntity clientPlayer = PathfinderClient.getPlayer();
                if (clientPlayer.getScoreboardTeam() != null) {
                    PathfinderClient.getPlayer().networkHandler.sendChatCommand(String.format("teammsg Lets meet here: X:%d Y:%d Z:%d", blockPos.getX(), blockPos.getY(), blockPos.getZ()));
                } else {
                    clientPlayer.sendMessage(Text.literal(String.format("Not in any Team. Join Team or change in <%s>.", MinecraftClient.getInstance().options.socialInteractionsKey.getBoundKeyLocalizedText().getString())).formatted(Formatting.RED), true);
                }
            }
        }
    }

    private static void sendAddMessage(String player, BlockPos blockPos) {
        PathfinderClient.getPlayer().networkHandler.sendChatCommand(String.format("msg %s Lets meet here: X:%d Y:%d Z:%d", player, blockPos.getX(), blockPos.getY(), blockPos.getZ()));
    }

    public static void deleteWaypoint() {
        UUID player = PathfinderClient.getPlayer().getUuid();
        removeWaypoint(player, true);
        sendRemove(player);
    }

    private static void sendRemove(UUID player) {
        if (PathfinderClient.is_loaded) {
            PacketByteBuf buf = PacketByteBufs.create();
            buf.writeUuid(player);
            ClientPlayNetworking.send(ClientNetworking.REMOVE_WAYPOINT, buf);
        } else {
            if (PathfinderClient.use_gang) {
                for (UUID uuid : GangHandler.members) {
                    PlayerListEntry playerEntry = PathfinderClient.getPlayer().networkHandler.getPlayerListEntry(uuid);
                    if (playerEntry != null)
                        sendRemoveMessage(playerEntry.getProfile().getName());
                }
            }
            else {
                ClientPlayerEntity clientPlayer = PathfinderClient.getPlayer();
                if (clientPlayer.getScoreboardTeam() != null) {
                    clientPlayer.networkHandler.sendChatCommand("teammsg Forget about my meeting point.");
                } else {
                    clientPlayer.sendMessage(Text.literal(String.format("Not in any Team. Join Team or change in <%s>", MinecraftClient.getInstance().options.socialInteractionsKey.getBoundKeyLocalizedText().getString())).formatted(Formatting.RED), true);
                }
            }
        }
    }

    private static void sendRemoveMessage(String player) {
        PathfinderClient.getPlayer().networkHandler.sendChatCommand(String.format("msg %s Forget about my meeting point.", player));
    }

    public static void addWaypoint(UUID player, BlockPos blockPos) {
        addWaypoint(player, blockPos, false);
    }

    private static void addWaypoint(UUID player, BlockPos blockPos, boolean own) {
        if (own || player != PathfinderClient.getPlayer().getUuid())
            WAYPOINTS.put(player, Waypoint.create(blockPos, player));
    }

    public static void removeWaypoint(UUID player) {
        removeWaypoint(player, false);
    }

    public static void removeWaypoint(UUID player, boolean own) {
        if (own || player != PathfinderClient.getPlayer().getUuid())
            WAYPOINTS.remove(player);
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
