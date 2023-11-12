package net.bmjo.pathfinder.waypoint;

import net.bmjo.pathfinder.PathfinderClient;
import net.bmjo.pathfinder.config.PathfinderConfig;
import net.bmjo.pathfinder.gang.GangHandler;
import net.bmjo.pathfinder.networking.ClientNetworking;
import net.bmjo.pathfinder.util.PathfinderClientUtil;
import net.bmjo.pathfinder.util.PathfinderSounds;
import net.bmjo.pathfinder.util.RegExEr;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.entity.Entity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.sound.SoundCategory;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.GlobalPos;

import java.util.*;

/**
 * Manages the waypoints of the players in Minecraft, including creation, deletion, and filtering based on various conditions.
 *
 * @author BMJO
 * @version 1.8
 */
public class WaypointHandler {
    /**
     * A map that associates player UUIDs with their respective waypoints.
     */
    public static final Map<UUID, Waypoint> WAYPOINTS = new HashMap<>();
    private static int messageCooldown;
    private static final String createMessage, deleteMessage;

    //CREATE

    /**
     * Adds a waypoint for the specified owner at the given global position to {@link WaypointHandler#WAYPOINTS}.
     *
     * @param owner     The UUID of the owner.
     * @param globalPos The global position of the waypoint.
     */
    private static void addWaypoint(UUID owner, GlobalPos globalPos) {
        ClientPlayerEntity clientPlayer = PathfinderClient.getPlayer();
        if (clientPlayer != null)
            clientPlayer.playSound(PathfinderSounds.WAYPOINT_CREATE, SoundCategory.NEUTRAL, 1.0F, 1.0F);
        WAYPOINTS.put(owner, Waypoint.create(owner, globalPos));
    }

    /**
     * Attempts to create and add a waypoint for the specified owner at the given global position.
     *
     * @param owner    The UUID of the owner.
     * @param blockPos The global position of the waypoint.
     */
    public static void tryAddWaypoint(UUID owner, GlobalPos blockPos) {
        ClientPlayerEntity clientPlayer = PathfinderClient.getPlayer();
        if (clientPlayer != null && clientPlayer.getUuid().equals(owner))
            return;
        if (PathfinderConfig.USE_GANG) {
            if (GangHandler.isMember(owner))
                addWaypoint(owner, blockPos);
        } else {
            if (PathfinderClientUtil.isInTeam(owner))
                addWaypoint(owner, blockPos);
        }
    }

    /**
     * Creates a waypoint based on the client player's raycast hit result.
     */
    public static void createWaypoint() {
        ClientPlayerEntity player = PathfinderClient.getPlayer();
        HitResult hitResult = raycastWaypoint();
        if (player == null)
            return;
        UUID uuid = player.getUuid();
        if (WAYPOINTS.containsKey(uuid) && Waypoint.getAngelToWaypoint(WAYPOINTS.get(uuid).pos()) < 10) {
            deleteWaypoint();
        }
        else {
            if (!(hitResult instanceof BlockHitResult blockHitResult))
                return;
            BlockPos hitPos = blockHitResult.getBlockPos();
            addWaypoint(uuid, GlobalPos.create(player.getWorld().getRegistryKey(), hitPos));
            if (canSend()) {
                sendCreate(hitPos);
            }
        }
    }

    /**
     * Sends a creation message for the waypoint of the client.
     *
     * @param blockPos The block position of the waypoint.
     */
    private static void sendCreate(BlockPos blockPos) {
        ClientPlayerEntity owner = PathfinderClient.getPlayer();
        if (owner == null)
            return;
        Identifier dimension = owner.getWorld().getRegistryKey().getValue();
        if (PathfinderConfig.USE_GANG) {
            if (PathfinderClient.is_loaded) {
                GangHandler.forEach(uuid -> {
                    PacketByteBuf buf = PacketByteBufs.create();
                    buf.writeUuid(uuid);
                    buf.writeBlockPos(blockPos);
                    buf.writeString(dimension.toString());
                    ClientPlayNetworking.send(ClientNetworking.CREATE_GANG_WAYPOINT, buf);
                });
            } else {
                GangHandler.forEach((uuid -> {
                    PlayerListEntry member = owner.networkHandler.getPlayerListEntry(uuid);
                    if (member != null)
                        owner.networkHandler.sendChatCommand(String.format("msg %s " + createMessage, member.getProfile().getName(), blockPos.getX(), blockPos.getY(), blockPos.getZ(), RegExEr.upperCaseFirst(dimension.getNamespace()), RegExEr.upperCaseFirst(dimension.getPath()), owner.getName().getString()));
                }));
            }
        } else {
            if (owner.getScoreboardTeam() != null) {
                if (PathfinderClient.is_loaded) {
                    ClientPlayNetworking.send(ClientNetworking.CREATE_TEAM_WAYPOINT, PacketByteBufs.create().writeBlockPos(blockPos).writeString(dimension.toString()));
                } else {
                    PathfinderClient.getPlayer().networkHandler.sendChatCommand(String.format("teammsg " + createMessage, blockPos.getX(), blockPos.getY(), blockPos.getZ(), RegExEr.upperCaseFirst(dimension.getNamespace()), RegExEr.upperCaseFirst(dimension.getPath()), owner.getName().getString()));
                }
            } else {
                owner.sendMessage(Text.literal(String.format("Not in any Team. Join Team or change in <%s> to share Waypoint.", MinecraftClient.getInstance().options.socialInteractionsKey.getBoundKeyLocalizedText().getString())).formatted(Formatting.RED), true);
            }
        }
    }

    // DELETE

    /**
     * Attempts to remove a waypoint for the specified owner.
     *
     * @param owner The UUID of the owner.
     */
    public static void tryRemoveWaypoint(UUID owner) {
        ClientPlayerEntity clientPlayer = PathfinderClient.getPlayer();
        if (clientPlayer != null && owner != clientPlayer.getUuid())
            removeWaypoint(owner);
    }

    /**
     * Removes the waypoint for the player.
     *
     * @param owner The UUID of the owner.
     */
    private static void removeWaypoint(UUID owner) {
        WAYPOINTS.remove(owner);
    }

    /**
     * Deletes the waypoint for the client player.
     */
    public static void deleteWaypoint() {
        ClientPlayerEntity player = PathfinderClient.getPlayer();
        if (player != null) {
            UUID uuid = player.getUuid();
            removeWaypoint(uuid);
            sendDelete();
        }
    }

    /**
     * Sends a delete message for the client player's waypoint.
     */
    private static void sendDelete() {
        ClientPlayerEntity owner = PathfinderClient.getPlayer();
        if (owner == null)
            return;
        if (PathfinderConfig.USE_GANG) {
            if (PathfinderClient.is_loaded) {
                GangHandler.forEach(uuid -> ClientPlayNetworking.send(ClientNetworking.REMOVE_GANG_WAYPOINT, PacketByteBufs.create().writeUuid(uuid)));
            } else {
                GangHandler.forEach((uuid -> {
                    PlayerListEntry member = owner.networkHandler.getPlayerListEntry(uuid);
                    if (member != null)
                        owner.networkHandler.sendChatCommand(String.format("msg %s " + deleteMessage, member.getProfile().getName(), owner.getName().getString()));
                }));
            }
        } else {
            if (owner.getScoreboardTeam() != null) {
                if (PathfinderClient.is_loaded) {
                    ClientPlayNetworking.send(ClientNetworking.REMOVE_TEAM_WAYPOINT, PacketByteBufs.create());
                } else {
                    owner.networkHandler.sendChatCommand(String.format("teammsg " + deleteMessage, owner.getName().getString()));
                }
            } else {
                owner.sendMessage(Text.literal(String.format("Not in any Team. Join Team or change in <%s> to share Waypoint.", MinecraftClient.getInstance().options.socialInteractionsKey.getBoundKeyLocalizedText().getString())).formatted(Formatting.RED), true);
            }
        }
    }

    // STUFF

    /**
     * Filters waypoints to only include those belonging to gang members.
     */
    public static void onlyGang() {
        ClientPlayerEntity clientPlayer = PathfinderClient.getPlayer();
        if (clientPlayer == null)
            return;
        for (UUID uuid : WAYPOINTS.keySet())
            if (!(GangHandler.isMember(uuid) || clientPlayer.getUuid().equals(uuid)))
                removeWaypoint(uuid);
    }

    /**
     * Filters waypoints to only include those belonging to minecraft team members.
     */
    public static void onlyTeam() {
        ClientPlayerEntity clientPlayer = PathfinderClient.getPlayer();
        if (clientPlayer == null)
            return;
        for (UUID uuid : WAYPOINTS.keySet())
            if (!(PathfinderClientUtil.isInTeam(uuid) || clientPlayer.getUuid().equals(uuid)))
                removeWaypoint(uuid);
    }

    /**
     * Performs a raycast to determine the hit result for waypoints.
     *
     * @return The hit result.
     */
    private static HitResult raycastWaypoint() {
        Entity entity = MinecraftClient.getInstance().getCameraEntity();
        assert entity != null;
        return entity.raycast(MinecraftClient.getInstance().options.getViewDistance().getValue() * 16, 1.0F, false);
    }

    /**
     * Updates the waypoint handler, removing waypoints that exceed the time limit.
     */
    public static void update() {
        if (messageCooldown > 0)
            --messageCooldown;

        if (System.currentTimeMillis() % 10 * 1000 == 0) {
            Set<UUID> remove = new HashSet<>(WAYPOINTS.size());
            for (Map.Entry<UUID, Waypoint> waypoint : WAYPOINTS.entrySet())
                if (waypoint.getValue().tryRemove())
                    remove.add(waypoint.getKey());
            for (UUID uuid : remove)
                removeWaypoint(uuid);
        }
    }

    /**
     * Checks if a message can be sent based on a cooldown, to not trigger the Minecraft spam detection.
     *
     * @return True if a message can be sent, false otherwise.
     */
    private static boolean canSend() {
        messageCooldown += 20;
        return messageCooldown < 100;
    }

    static {
        createMessage = "Lets meet at: X:%d Y:%d Z:%d in the %s %s. ~%s"; // Lets meet at: X:420 Y:69 Z:-13 in the Minecraft Overworld. ~BaumeisterJO
        deleteMessage = "Forget about my meeting point. ~%s";
    }
}
