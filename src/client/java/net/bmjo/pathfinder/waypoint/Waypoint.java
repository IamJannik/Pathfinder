package net.bmjo.pathfinder.waypoint;

import net.bmjo.pathfinder.PathfinderClient;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.render.Camera;
import net.minecraft.client.util.SkinTextures;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;
import java.util.function.Supplier;

/**
 * Represents a waypoint in Minecraft, storing information such as position, name, skin, creation time, and distance.
 *
 * @author BMJO
 * @version 1.2
 */
public class Waypoint {
    private final GlobalPos pos;
    private final String owner;
    private final Supplier<SkinTextures> skin;
    private final long created;
    private final boolean farAway;

    /**
     * Constructs a Waypoint object.
     *
     * @param pos     The global position of the waypoint.
     * @param owner   The name of the owner associated with the waypoint.
     * @param skin    A supplier for the skin textures of the owner associated with the waypoint.
     * @param created The timestamp when the waypoint was created.
     */
    private Waypoint(GlobalPos pos, String owner, Supplier<SkinTextures> skin, long created) {
        this.pos = pos;
        this.owner = owner;
        this.skin = skin;
        this.created = created;
        this.farAway = !this.isClientInRange(10);
    }

    /**
     * Creates a new waypoint based on the global position and owner UUID.
     *
     * @param pos   The global position of the waypoint.
     * @param owner The UUID of the owner associated with the waypoint.
     * @return A new Waypoint instance.
     */
    public static Waypoint create(UUID owner, GlobalPos pos) {
        String ownerName = "";
        Supplier<SkinTextures> skin = () -> null;
        ClientPlayerEntity clientPlayer = PathfinderClient.getPlayer();
        if (clientPlayer != null) {
            PlayerListEntry playerListEntry = clientPlayer.networkHandler.getPlayerListEntry(owner);
            if (playerListEntry != null) {
                ownerName = playerListEntry.getProfile().getName();
                skin = playerListEntry::getSkinTextures;
            }
        }
        return new Waypoint(pos, ownerName, skin, System.currentTimeMillis());
    }

    /**
     * Gets the block position of the waypoint.
     *
     * @return The block position.
     */
    public BlockPos pos() {
        return this.pos.pos();
    }

    /**
     * Gets the X-coordinate of the waypoint's block position.
     *
     * @return The X-coordinate.
     */
    public int posX() {
        return this.pos().getX();
    }

    /**
     * Gets the Y-coordinate of the waypoint's block position.
     *
     * @return The Y-coordinate.
     */
    public int posY() {
        return this.pos().getY();
    }

    /**
     * Gets the Z-coordinate of the waypoint's block position.
     *
     * @return The Z-coordinate.
     */
    public int posZ() {
        return this.pos().getZ();
    }

    /**
     * Gets the dimension key of the waypoint.
     *
     * @return The dimension key.
     */
    public RegistryKey<World> dimension() {
        return this.pos.dimension();
    }

    /**
     * Gets the owners name of the owner associated with the waypoint.
     *
     * @return The owners name.
     */
    public String owner() {
        return this.owner;
    }

    /**
     * Gets the skin textures associated with the waypoint.
     *
     * @return The skin textures, or null if not available.
     */
    @Nullable
    public SkinTextures skin() {
        return this.skin.get();
    }

    /**
     * Attempts to remove the waypoint based on time and distance conditions.
     *
     * @return True if the waypoint should be removed, false otherwise.
     */
    public boolean tryRemove() {
        return System.currentTimeMillis() - this.created >= 10 * 60 * 1000 || this.farAway && this.isClientInRange(3);
    }

    /**
     * Checks if the client is within a certain distance from the waypoint.
     *
     * @param distance The distance threshold.
     * @return True if the client is within the specified distance, false otherwise.
     */
    private boolean isClientInRange(int distance) {
        ClientPlayerEntity player = PathfinderClient.getPlayer();
        return player != null && player.getBlockPos().isWithinDistance(this.pos.pos(), distance);
    }

    public static Vec3d getOffset() {
        return new Vec3d(0.5, 1.5, 0.5);
    }

    /**
     * Calculates the angle between the player's camera and the waypoint with the block position of the waypoint.
     * {@link Waypoint#getAngelToPlayer(BlockPos)}
     *
     * @return The angle to the waypoint.
     */
    public float getAngelToPlayer() {
        return getAngelToPlayer(this.pos());
    }

    /**
     * Calculates the angle between a given block position and the client player's camera.
     *
     * @param blockPos The block position to calculate the angle to.
     * @return The angle to the specified block position.
     */
    public static float getAngelToPlayer(BlockPos blockPos) {
        MinecraftClient mc = MinecraftClient.getInstance();
        assert mc.world != null;

        int wX = blockPos.getX();
        int wZ = blockPos.getZ();

        Camera camera = mc.gameRenderer.getCamera();
        Vec3d cameraPos = camera.getPos();
        double offX = (double) wX - cameraPos.getX();
        double offZ = (double) wZ - cameraPos.getZ();

        double angle = Math.toDegrees(Math.atan2(offZ, offX));
        return (float) MathHelper.wrapDegrees(angle + 90.0F);
    }

    public float getViewAngelToPlayer() {
        return getViewAngelToPlayer(this.pos());
    }


    public static float getViewAngelToPlayer(BlockPos blockPos) {
        float angle = getAngelToPlayer(blockPos);

        MinecraftClient mc = MinecraftClient.getInstance();
        Camera camera = mc.gameRenderer.getCamera();

        return MathHelper.wrapDegrees(angle - camera.getYaw() - 180.0F);
    }
}
