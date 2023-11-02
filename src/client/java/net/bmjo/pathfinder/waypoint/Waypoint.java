package net.bmjo.pathfinder.waypoint;

import net.bmjo.pathfinder.PathfinderClient;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;

import java.util.UUID;

public class Waypoint {
    private final BlockPos pos;
    private final UUID player;
    private final long created;
    private final int color;
    private final boolean farAway;
    private final WaypointModel model;

    private Waypoint(BlockPos pos, UUID player, long created, int color) {
        this.pos = pos;
        this.player = player;
        this.created = created;
        this.color = color;
        this.farAway = !isClientInRange(this.pos, 10);
        this.model = WaypointModel.create(380, 0.25F);
    }

    public static Waypoint create(BlockPos pos, UUID player) {
        return new Waypoint(pos, player, System.currentTimeMillis(), Math.abs(player.hashCode()) % 0xFFFFFF + 0xFF000000);
    }

    public BlockPos pos() {
        return this.pos;
    }

    public UUID player() {
        return this.player;
    }

    public boolean tryRemove() {
        return System.currentTimeMillis() - this.created >= 10 * 60 * 1000 || (this.farAway && isClientInRange(this.pos, 3)); // 10 min
    }

    private static boolean isClientInRange(BlockPos pos, int distance) {
        return PathfinderClient.getPlayer().getBlockPos().isWithinDistance(pos, distance);
    }

    public void render(MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, Camera camera) {
        if (vertexConsumerProvider == null)
            return;
        long time = System.currentTimeMillis() - this.created;
        //matrixStack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(time * 2.25F - 45.0F));
        this.renderBeam(matrixStack, vertexConsumerProvider.getBuffer(RenderLayer.getLines()), camera);
        this.renderBlockOutline(matrixStack, vertexConsumerProvider.getBuffer(RenderLayer.getLines()), camera);
    }

    private void renderBeam(MatrixStack matrixStack, VertexConsumer vertexConsumer, Camera camera) {

    }

    private void renderBlockOutline(MatrixStack matrixStack, VertexConsumer vertexConsumer, Camera camera) {
        Vec3d transformedPosition = new Vec3d(this.pos.getX(), this.pos.getY(), this.pos.getZ()).subtract(camera.getPos());
        ClientWorld world = MinecraftClient.getInstance().world;
        assert world != null;
        BlockState blockState = world.getBlockState(this.pos);
        VoxelShape shape =  blockState.getOutlineShape(world, this.pos, ShapeContext.of(camera.getFocusedEntity()));
        renderShapeOutline(matrixStack, vertexConsumer, shape, transformedPosition.getX(), transformedPosition.getY(), transformedPosition.getZ(), this.color);
    }

    private static void renderShapeOutline(MatrixStack matrixStack, VertexConsumer vertexConsumer, VoxelShape shape, double xOffset, double yOffset, double zOffset, int color) {
        matrixStack.push();
        MatrixStack.Entry entry = matrixStack.peek();
        matrixStack.translate(xOffset, yOffset, zOffset);
        shape.forEachEdge((minX, minY, minZ, maxX, maxY, maxZ) -> {
            Vec3d normVec = new Vec3d(maxX - minX, maxY - minY, maxZ - minZ).normalize();
            vertexConsumer
                    .vertex(entry.getPositionMatrix(), (float)minX, (float)minY, (float)minZ)
                    .color(color)
                    .normal(entry.getNormalMatrix(), (float)normVec.x, (float)normVec.y, (float)normVec.z).next();
            vertexConsumer
                    .vertex(entry.getPositionMatrix(), (float)maxX, (float)maxY, (float)maxZ)
                    .color(color)
                    .normal(entry.getNormalMatrix(), (float)normVec.x, (float)normVec.y, (float)normVec.z).next();
        });
        matrixStack.pop();
    }
}
