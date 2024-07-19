package net.bmjo.pathfinder.waypoint;

import net.minecraft.registry.RegistryKey;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.function.Predicate;

/**
 * A filter for waypoints based on the specified parameters, including look vector, camera position, and dimension.
 * So the {@link WaypointRenderer} knows which one to render.
 *
 * @author BMJO
 * @version 1.0
 */
public class WaypointFilter implements Predicate<Waypoint> {
    private Vec3d lookVector;
    private Vec3d cameraPos;
    private RegistryKey<World> dimension;

    /**
     * Sets the parameters for the waypoint filter.
     *
     * @param lookVector The vector representing the player's look direction.
     * @param cameraPos  The position of the client player's camera.
     * @param dimension  The dimension key to filter waypoints by.
     */
    public void setParams(Vec3d lookVector, Vec3d cameraPos, RegistryKey<World> dimension) {
        this.lookVector = lookVector;
        this.cameraPos = cameraPos;
        this.dimension = dimension;
    }

    /**
     * Tests whether a waypoint satisfies the filtering conditions and should be rendered.
     *
     * @param waypoint The waypoint to be tested.
     * @return True if the waypoint passes the filter, false otherwise.
     */
    @Override
    public boolean test(Waypoint waypoint) {
        if (!waypoint.dimension().equals(this.dimension))
            return false;
        double offX = waypoint.posX() - this.cameraPos.getX() + 0.5;
        double offY = waypoint.posY() - this.cameraPos.getY() + 1.0;
        double offZ = waypoint.posZ() - this.cameraPos.getZ() + 0.5;

        double depth = offX * this.lookVector.getX() + offY * this.lookVector.getY() + offZ * this.lookVector.getZ();
        if (depth <= 0.1) {
            return false;
        } else {
            double unscaledDistance2D = Math.sqrt(offX * offX + offZ * offZ);
            return unscaledDistance2D >= 0;
        }
    }
}
