package net.bmjo.pathfinder.waypoint;

import net.bmjo.pathfinder.util.PathfinderClientUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.Camera;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.UUID;

/**
 * Represents a waypoint in Minecraft, storing information such as position, name, skin, creation time, and distance.
 *
 * @author BMJO
 * @version 1.2
 */
public abstract class Waypoint {
    private final String owner;
    private final long created;

    /**
     * Constructs a Waypoint object.
     *
     * @param owner   The name of the owner associated with the waypoint.
     */
    protected Waypoint(UUID owner) {
        this.owner = PathfinderClientUtil.uuidToName(owner);
        this.created = System.currentTimeMillis();
    }

    /**
     * Gets the block position of the waypoint.
     *
     * @return The block position.
     */
    public abstract Vec3d pos();

    /**
     * Gets the X-coordinate of the waypoint's block position.
     *
     * @return The X-coordinate.
     */
    public double posX() {
        return this.pos().getX();
    }

    /**
     * Gets the Y-coordinate of the waypoint's block position.
     *
     * @return The Y-coordinate.
     */
    public double posY() {
        return this.pos().getY();
    }

    /**
     * Gets the Z-coordinate of the waypoint's block position.
     *
     * @return The Z-coordinate.
     */
    public double posZ() {
        return this.pos().getZ();
    }

    /**
     * Gets the dimension key of the waypoint.
     *
     * @return The dimension key.
     */
    public abstract RegistryKey<World> dimension();

    /**
     * Gets the owners name of the owner associated with the waypoint.
     *
     * @return The owners name.
     */
    public String owner() {
        return this.owner;
    }

    public abstract void drawIcon(DrawContext drawContext);

    public abstract float getYOffset();

    /**
     * Attempts to remove the waypoint based on time and distance conditions.
     *
     * @return True if the waypoint should be removed, false otherwise.
     */
    public boolean tryRemove() {
        return System.currentTimeMillis() - this.created >= 10 * 60 * 1000;
    }

    /**
     * Calculates the angle between a given block position and the client player's camera.
     *
     * @param pos The block position to calculate the angle to.
     * @return The angle to the specified block position.
     */
    public static float getAngelToPlayer(Vec3d pos) {
        MinecraftClient mc = MinecraftClient.getInstance();
        assert mc.world != null;

        double wX = pos.getX();
        double wZ = pos.getZ();

        Camera camera = mc.gameRenderer.getCamera();
        Vec3d cameraPos = camera.getPos();
        double offX = wX - cameraPos.getX();
        double offZ = wZ - cameraPos.getZ();

        double angle = Math.toDegrees(Math.atan2(offZ, offX));
        return (float) MathHelper.wrapDegrees(angle + 90.0F);
    }

    public float getViewAngelToPlayer() {
        return getViewAngelToPlayer(this.pos());
    }


    public static float getViewAngelToPlayer(Vec3d pos) {
        float angle = getAngelToPlayer(pos);

        MinecraftClient mc = MinecraftClient.getInstance();
        Camera camera = mc.gameRenderer.getCamera();

        return MathHelper.wrapDegrees(angle - camera.getYaw() - 180.0F);
    }
}
