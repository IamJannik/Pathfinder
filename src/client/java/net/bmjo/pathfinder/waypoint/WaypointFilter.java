package net.bmjo.pathfinder.waypoint;

import net.minecraft.util.math.Vec3d;
import org.joml.Vector3f;

import java.util.function.Predicate;

public class WaypointFilter implements Predicate<Waypoint> {
    private Vector3f lookVector;
    private Vec3d cameraPos;

    public void setParams(Vector3f lookVector, Vec3d cameraPos) {
        this.lookVector = lookVector;
        this.cameraPos = cameraPos;
    }

    @Override
    public boolean test(Waypoint waypoint) {
        double offX = waypoint.posX() - this.cameraPos.getX() + 0.5;
        double offY = waypoint.posY() - this.cameraPos.getY() + 1.0;
        double offZ = waypoint.posZ() - this.cameraPos.getZ() + 0.5;

        double depth = offX * (double) this.lookVector.x() + offY * (double) this.lookVector.y() + offZ * (double) this.lookVector.z();
        if (depth <= 0.1) {
            return false;
        } else {
            double unscaledDistance2D = Math.sqrt(offX * offX + offZ * offZ);
            return unscaledDistance2D >= 0;
        }
    }
}
