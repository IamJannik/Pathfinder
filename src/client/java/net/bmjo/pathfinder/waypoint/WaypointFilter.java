package net.bmjo.pathfinder.waypoint;

import org.joml.Vector3f;

import java.util.function.Predicate;

public class WaypointFilter implements Predicate<Waypoint> {
    public Vector3f lookVector;
    public double cameraX;
    public double cameraY;
    public double cameraZ;
    public double dimDiv;

    public void setParams(Vector3f lookVector, double cameraX, double cameraY, double cameraZ, double dimDiv) {
        this.lookVector = lookVector;
        this.cameraX = cameraX;
        this.cameraY = cameraY;
        this.cameraZ = cameraZ;
        this.dimDiv = dimDiv;
    }

    @Override
    public boolean test(Waypoint waypoint) {
        double offX = waypoint.posX(this.dimDiv) - this.cameraX + 0.5;
        double offY = waypoint.posY(1) - this.cameraY + 1.0;

        double offZ = waypoint.posZ(this.dimDiv) - this.cameraZ + 0.5;
        double depth = offX * (double) this.lookVector.x() + offY * (double) this.lookVector.y() + offZ * (double) this.lookVector.z();
        if (depth <= 0.1) {
            return false;
        } else {
            double unscaledDistance2D = Math.sqrt(offX * offX + offZ * offZ);
            return unscaledDistance2D >= 0;
        }
    }
}
