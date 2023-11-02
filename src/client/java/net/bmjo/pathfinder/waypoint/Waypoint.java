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
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.shape.VoxelShape;

public class Waypoint {
    private final BlockPos pos;
    private final int playerID;
    private final long created;
    private final int color;
    private final boolean farAway;
    private final WaypointModel model;

    private Waypoint(BlockPos pos, int playerID, long created, int color) {
        this.pos = pos;
        this.playerID = playerID;
        this.created = created;
        this.color = color;
        this.farAway = !isClientInRange(this.pos, 10);
        this.model = WaypointModel.create(380, 0.25F);
    }

    public static Waypoint create(BlockPos pos, int playerId) {
        return new Waypoint(pos, playerId, System.currentTimeMillis(), 15728880);
    }

    public BlockPos pos() {
        return this.pos;
    }

    public int player() {
        return this.playerID;
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
        ClientWorld world = MinecraftClient.getInstance().world;
        assert world != null;
        BlockState blockState = world.getBlockState(this.pos);
        VoxelShape shape =  blockState.getOutlineShape(world, this.pos, ShapeContext.of(camera.getFocusedEntity()));
        double cameraX = (double)this.pos.getX() - camera.getPos().getX();
        double cameraY = (double)this.pos.getY() - camera.getPos().getY();
        double cameraZ = (double)this.pos.getZ() - camera.getPos().getZ();
        renderShapeOutline(matrixStack, vertexConsumer, shape, cameraX, cameraY, cameraZ, camera.getYaw(), 0x6495EDFF);
    }

    private static void renderShapeOutline(MatrixStack matrixStack, VertexConsumer vertexConsumer, VoxelShape shape, double xOffset, double yOffset, double zOffset, float yaw, int color) {
        matrixStack.push();
        MatrixStack.Entry entry = matrixStack.peek();
        matrixStack.translate(xOffset, yOffset, zOffset);
        shape.forEachEdge((minX, minY, minZ, maxX, maxY, maxZ) -> {
            float k = (float)(maxX - minX);
            float l = (float)(maxY - minY);
            float m = (float)(maxZ - minZ);
            float n = MathHelper.sqrt(k * k + l * l + m * m);
            k /= n;
            l /= n;
            m /= n;
            vertexConsumer.vertex(entry.getPositionMatrix(), (float)(minX), (float)(minY), (float)(minZ)).color(color).normal(entry.getNormalMatrix(), k, l, m).next();
            vertexConsumer.vertex(entry.getPositionMatrix(), (float)(maxX), (float)(maxY), (float)(maxZ)).color(color).normal(entry.getNormalMatrix(), k, l, m).next();
        });
        matrixStack.pop();
    }
}
