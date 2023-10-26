package net.bmjo.pathfinder.waypoint;

import net.minecraft.util.math.BlockPos;

public class Waypoint {
    private final BlockPos pos;
    private final int playerID;
    private final long created;
    private final int color;

    private Waypoint(BlockPos pos, int playerID, long created, int color) {
        this.pos = pos;
        this.playerID = playerID;
        this.created = created;
        this.color = color;
    }

    public static Waypoint create(BlockPos pos, int playerId) {
        return new Waypoint(pos, playerId, System.currentTimeMillis(), 0);
    }

    public BlockPos pos() {
        return this.pos;
    }

    public int player() {
        return this.playerID;
    }

    public long created() {
        return this.created;
    }

    public int color() {
        return this.color;
    }
}
