package net.bmjo.pathfinder.waypoint;

import net.bmjo.pathfinder.PathfinderClient;
import net.bmjo.pathfinder.config.PathfinderConfig;
import net.bmjo.pathfinder.gang.GangHandler;
import net.bmjo.pathfinder.networking.payload.GangWaypointPayload;
import net.bmjo.pathfinder.networking.payload.TeamWaypointPayload;
import net.bmjo.pathfinder.util.PathfinderClientUtil;
import net.bmjo.pathfinder.util.PathfinderSounds;
import net.bmjo.pathfinder.util.RegExEr;
import net.bmjo.pathfinder.waypoint.type.ItemWaypoint;
import net.bmjo.pathfinder.waypoint.type.MobWaypoint;
import net.bmjo.pathfinder.waypoint.type.PositionWaypoint;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

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
    private static final String createPosMessage, createEntityMessage, deleteMessage;

    //CREATE

    /**
     * Creates a waypoint based on the client player's raycast hit result.
     */
    public static void createWaypoint() {
        ClientPlayerEntity player = PathfinderClient.getPlayer();
        HitResult hitResult = raycastWaypoint();
        if (player == null)
            return;
        UUID uuid = player.getUuid();
        if (WAYPOINTS.containsKey(uuid) && Waypoint.getViewAngelToPlayer(WAYPOINTS.get(uuid).pos()) < 10) {
            removeWaypoint();
        } else {
            if (hitResult instanceof BlockHitResult blockHitResult) {
                World world = player.getWorld();
                BlockPos hitPos = blockHitResult.getBlockPos();
                if (world.getBlockState(hitPos).isAir())
                    return;
                List<ItemEntity> items = world.getEntitiesByClass(ItemEntity.class, new Box(hitPos).expand(0.2D), Entity::isAlive);
                if (!items.isEmpty()) {
                    ItemEntity item = items.get(0);
                    addItemWaypoint(uuid, item);
                    sendEntityWaypoint(item);
                    return;
                }
                addPosWaypoint(uuid, GlobalPos.create(world.getRegistryKey(), hitPos));
                sendPosWaypoint(hitPos);
            } else if (hitResult instanceof EntityHitResult entityHitResult) {
                Entity entity = entityHitResult.getEntity();
                if (entity instanceof MobEntity mob) {
                    addMobWaypoint(uuid, mob);
                    sendEntityWaypoint(entity);
                } else if (entity instanceof ItemEntity item) {
                    addItemWaypoint(uuid, item);
                }
            }
        }
    }

    /**
     * Adds a waypoint for the specified owner at the given global position to {@link WaypointHandler#WAYPOINTS}.
     *
     * @param owner     The UUID of the owner.
     * @param globalPos The global position of the waypoint.
     */
    private static void addPosWaypoint(UUID owner, GlobalPos globalPos) {
        ClientPlayerEntity clientPlayer = PathfinderClient.getPlayer();
        if (clientPlayer != null)
            clientPlayer.playSound(PathfinderSounds.WAYPOINT_CREATE, 1.0F, 1.0F);
        WAYPOINTS.put(owner, PositionWaypoint.create(owner, globalPos));
    }

    private static void addMobWaypoint(UUID owner, MobEntity mob) {
        ClientPlayerEntity clientPlayer = PathfinderClient.getPlayer();
        if (clientPlayer != null)
            clientPlayer.playSound(PathfinderSounds.WAYPOINT_CREATE, 1.0F, 1.0F);
        WAYPOINTS.put(owner, MobWaypoint.create(owner, mob));
    }

    private static void addItemWaypoint(UUID owner, ItemEntity item) {
        ClientPlayerEntity clientPlayer = PathfinderClient.getPlayer();
        if (clientPlayer != null)
            clientPlayer.playSound(PathfinderSounds.WAYPOINT_CREATE, 1.0F, 1.0F);
        WAYPOINTS.put(owner, ItemWaypoint.create(owner, item));
    }

    /**
     * Attempts to create and add a waypoint for the specified owner at the given global position.
     *
     * @param owner    The UUID of the owner.
     * @param globalPos The global position of the waypoint.
     */
    public static void tryAddPosWaypoint(UUID owner, GlobalPos globalPos) {
        ClientPlayerEntity clientPlayer = PathfinderClient.getPlayer();
        if (clientPlayer != null && clientPlayer.getUuid().equals(owner))
            return;
        if (PathfinderConfig.USE_GANG) {
            if (GangHandler.isMember(owner))
                addPosWaypoint(owner, globalPos);
        } else {
            if (PathfinderClientUtil.isInTeam(owner))
                addPosWaypoint(owner, globalPos);
        }
    }

    public static void tryAddEntityWaypoint(UUID owner, int entityId) {
        ClientPlayerEntity clientPlayer = PathfinderClient.getPlayer();
        if (clientPlayer == null || clientPlayer.getUuid().equals(owner))
            return;
        World world = clientPlayer.getWorld();
        Entity entity = world.getEntityById(entityId);
        if (entity == null) {
            return;
        }
        if (entity instanceof MobEntity mob) {
            if (PathfinderConfig.USE_GANG) {
                if (GangHandler.isMember(owner))
                    addMobWaypoint(owner, mob);
            } else {
                if (PathfinderClientUtil.isInTeam(owner))
                    addMobWaypoint(owner, mob);
            }
        } else if (entity instanceof ItemEntity item) {
            if (PathfinderConfig.USE_GANG) {
                if (GangHandler.isMember(owner))
                    addItemWaypoint(owner, item);
            } else {
                if (PathfinderClientUtil.isInTeam(owner))
                    addItemWaypoint(owner, item);
            }
        }
    }

    /**
     * Sends a creation message for the waypoint of the client.
     *
     * @param blockPos The block position of the waypoint.
     */
    private static void sendPosWaypoint(BlockPos blockPos) {
        if (!canSend())
            return;
        ClientPlayerEntity owner = PathfinderClient.getPlayer();
        if (owner == null)
            return;
        Identifier dimension = owner.getWorld().getRegistryKey().getValue();
        if (PathfinderConfig.USE_GANG) {
            if (PathfinderClient.is_loaded) {
                GangHandler.forEach(uuid -> ClientPlayNetworking.send(new GangWaypointPayload.CreatePos(uuid, blockPos, dimension.toString())));
            } else {
                GangHandler.forEach((uuid -> {
                    PlayerListEntry member = owner.networkHandler.getPlayerListEntry(uuid);
                    if (member != null)
                        owner.networkHandler.sendChatCommand(String.format("msg %s " + createPosMessage, member.getProfile().getName(), blockPos.getX(), blockPos.getY(), blockPos.getZ(), RegExEr.upperCaseFirst(dimension.getNamespace()), RegExEr.upperCaseFirst(dimension.getPath()), owner.getName().getString()));
                }));
            }
        } else {
            if (owner.getScoreboardTeam() != null) {
                if (PathfinderClient.is_loaded) {
                    ClientPlayNetworking.send(new TeamWaypointPayload.CreatePos(blockPos, dimension.toString()));
                } else {
                    PathfinderClient.getPlayer().networkHandler.sendChatCommand(String.format("teammsg " + createPosMessage, blockPos.getX(), blockPos.getY(), blockPos.getZ(), RegExEr.upperCaseFirst(dimension.getNamespace()), RegExEr.upperCaseFirst(dimension.getPath()), owner.getName().getString()));
                }
            } else {
                owner.sendMessage(Text.literal(String.format("Not in any Team. Join Team or change in <%s> to share Waypoint.", MinecraftClient.getInstance().options.socialInteractionsKey.getBoundKeyLocalizedText().getString())).formatted(Formatting.RED), true);
            }
        }
    }

    private static void sendEntityWaypoint(Entity entity) {
        if (!canSend())
            return;
        ClientPlayerEntity owner = PathfinderClient.getPlayer();
        if (owner == null)
            return;
        int entityId = entity.getId();
        if (PathfinderConfig.USE_GANG) {
            if (PathfinderClient.is_loaded) {
                GangHandler.forEach(toPlayer -> ClientPlayNetworking.send(new GangWaypointPayload.CreateEntity(toPlayer, entityId)));
            } else {
                GangHandler.forEach((uuid -> {
                    PlayerListEntry member = owner.networkHandler.getPlayerListEntry(uuid);
                    if (member != null)
                        owner.networkHandler.sendChatCommand(String.format("msg %s " + createEntityMessage, member.getProfile().getName(), entity.getName().getString(), entityId, owner.getName().getString()));
                }));
            }
        } else {
            if (owner.getScoreboardTeam() != null) {
                if (PathfinderClient.is_loaded) {
                    ClientPlayNetworking.send(new TeamWaypointPayload.CreateEntity(entityId));
                } else {
                    PathfinderClient.getPlayer().networkHandler.sendChatCommand(String.format("teammsg " + createEntityMessage, entity.getName().getString(), entityId, owner.getName().getString()));
                }
            } else {
                owner.sendMessage(Text.literal(String.format("Not in any Team. Join Team or change in <%s> to share Waypoint.", MinecraftClient.getInstance().options.socialInteractionsKey.getBoundKeyLocalizedText().getString())).formatted(Formatting.RED), true);
            }
        }
    }

    // REMOVE

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
    public static void removeWaypoint() {
        ClientPlayerEntity player = PathfinderClient.getPlayer();
        if (player != null) {
            UUID uuid = player.getUuid();
            removeWaypoint(uuid);
            sendRemove();
        }
    }

    /**
     * Sends a delete message for the client player's waypoint.
     */
    private static void sendRemove() {
        ClientPlayerEntity owner = PathfinderClient.getPlayer();
        if (owner == null)
            return;
        if (PathfinderConfig.USE_GANG) {
            if (PathfinderClient.is_loaded) {
                GangHandler.forEach(uuid -> ClientPlayNetworking.send(new GangWaypointPayload.Remove(uuid)));
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
                    ClientPlayNetworking.send(new TeamWaypointPayload.Remove());
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
        MinecraftClient mc = MinecraftClient.getInstance();
        Entity camera = mc.getCameraEntity();
        assert camera != null;
        float tickDelta = 1.0F; // MinecraftClient.getInstance().gameRenderer.getCamera().getLastTickDelta();
        double maxDistance = mc.options.getViewDistance().getValue() * 16;
        Vec3d start = camera.getCameraPosVec(tickDelta);
        HitResult blockHitResult = camera.raycast(maxDistance, tickDelta, false); // get Block raycast
        double blockHitDistance = blockHitResult.getPos().squaredDistanceTo(start);
        if (blockHitResult.getType() != HitResult.Type.MISS) // reduce maxDistance to the block hit distance
            maxDistance = blockHitDistance;
        maxDistance = Math.min(maxDistance, mc.options.getSimulationDistance().getValue() * 16);
        Vec3d cameraView = camera.getRotationVec(tickDelta);
        Vec3d end = start.add(cameraView.getX() * maxDistance, cameraView.getY() * maxDistance, cameraView.getZ() * maxDistance);
        Box box = camera.getBoundingBox().stretch(end).expand(1.0, 1.0, 1.0);
        HitResult entityHitResult = ProjectileUtil.raycast(camera, start, end, box, (entity) -> !entity.isSpectator(), maxDistance); // get CreateEntity raycast
        return entityHitResult != null && entityHitResult.getPos().squaredDistanceTo(start) < blockHitDistance ? entityHitResult : blockHitResult; // return closest
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
        createPosMessage = "Lets meet at: X:%d Y:%d Z:%d in the %s %s. ~%s"; // Lets meet at: X:420 Y:69 Z:-13 in the Minecraft Overworld. ~BaumeisterJO
        createEntityMessage = "Lets meet by the %s (%s). ~%s"; // Lets meet at: X:420 Y:69 Z:-13 in the Minecraft Overworld. ~BaumeisterJO
        deleteMessage = "Forget about my meeting point. ~%s";
    }
}
