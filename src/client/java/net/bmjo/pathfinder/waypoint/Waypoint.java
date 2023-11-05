package net.bmjo.pathfinder.waypoint;

import com.mojang.blaze3d.systems.RenderSystem;
import net.bmjo.pathfinder.PathfinderClient;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import org.lwjgl.opengl.GL11;

import java.util.UUID;

public class Waypoint {
    private final BlockPos pos;
    private final UUID player;
    private final long created;
    private final int color;
    private final boolean farAway;

    private Waypoint(BlockPos pos, UUID player, long created, int color) {
        this.pos = pos;
        this.player = player;
        this.created = created;
        this.color = color;
        this.farAway = !isClientInRange(this.pos, 10);
    }

    public static Waypoint create(BlockPos pos, UUID player) {
        return new Waypoint(pos, player, System.currentTimeMillis(), Math.abs(player.hashCode()) % 0xFFFFFF + 0xFF000000);
    }

    public BlockPos pos() {
        return this.pos;
    }

    public boolean tryRemove() {
        return System.currentTimeMillis() - this.created >= 10 * 60 * 1000 || (this.farAway && isClientInRange(this.pos, 3)); // 10 min
    }

    private static boolean isClientInRange(BlockPos pos, int distance) {
        ClientPlayerEntity player = PathfinderClient.getPlayer();
        return player != null && player.getBlockPos().isWithinDistance(pos, distance);
    }

    public void render(Camera camera) {
        renderBeam(this.pos, this.color, System.currentTimeMillis() - this.created, camera);
        renderBlockOutline(this.pos, this.color, camera);
    }

    private static void renderBeam(BlockPos blockPos, int color, long time, Camera camera) {
        Vec3d transformedPosition = new Vec3d(blockPos.getX(), blockPos.getY(), blockPos.getZ()).subtract(camera.getPos());

        MatrixStack matrixStack = new MatrixStack();
        matrixStack.multiply(RotationAxis.POSITIVE_X.rotationDegrees(camera.getPitch()));
        matrixStack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(camera.getYaw() + 180.0F));
        matrixStack.translate(transformedPosition.x + 0.5F, transformedPosition.y, transformedPosition.z + 0.5F);
        matrixStack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(time * 2.25F - 45.0F));

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        MatrixStack.Entry entry = matrixStack.peek();
        buffer.begin(VertexFormat.DrawMode.LINES, VertexFormats.LINES);
        buffer
                .vertex(entry.getPositionMatrix(), 0.0F, 100.0F, 0.0F)
                .color(color)
                .normal(entry.getNormalMatrix(), 0.0F, 200.0F, 0.0F).next();
        buffer
                .vertex(entry.getPositionMatrix(), 0.0F, -100.0F, 0.0F)
                .color(color)
                .normal(entry.getNormalMatrix(), 0.0F, 200.0F, 0.0F).next();


        RenderSystem.setShader(GameRenderer::getRenderTypeLinesProgram);
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);

        RenderSystem.disableCull();
        RenderSystem.lineWidth(5.0F);
        tessellator.draw();
        RenderSystem.enableCull();
        RenderSystem.lineWidth(1.0F);
    }

    private static void renderBlockOutline(BlockPos blockPos, int color, Camera camera) {
        Vec3d transformedPosition = new Vec3d(blockPos.getX(), blockPos.getY(), blockPos.getZ()).subtract(camera.getPos());

        MatrixStack matrixStack = new MatrixStack();
        matrixStack.multiply(RotationAxis.POSITIVE_X.rotationDegrees(camera.getPitch()));
        matrixStack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(camera.getYaw() + 180.0F));
        matrixStack.translate(transformedPosition.x, transformedPosition.y, transformedPosition.z);

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();

        buffer.begin(VertexFormat.DrawMode.LINES, VertexFormats.LINES);
        ClientWorld world = MinecraftClient.getInstance().world;
        assert world != null;
        BlockState blockState = world.getBlockState(blockPos);
        VoxelShape shape = blockState.getOutlineShape(world, blockPos, ShapeContext.of(camera.getFocusedEntity()));
        if (!blockState.isAir() && world.getWorldBorder().contains(blockPos))
            renderShapeOutline(matrixStack, buffer, shape, color);


        RenderSystem.setShader(GameRenderer::getRenderTypeLinesProgram);
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);

        setupRenderSystem();
        tessellator.draw();
        resetRenderSystem();
    }

    private static void renderShapeOutline(MatrixStack matrixStack, VertexConsumer vertexConsumer, VoxelShape shape, int color) {
        MatrixStack.Entry entry = matrixStack.peek();
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
    }

    private static void setupRenderSystem() {
        RenderSystem.disableCull();
        RenderSystem.depthFunc(GL11.GL_ALWAYS);
        RenderSystem.lineWidth(5.0F);
    }

    private static void resetRenderSystem() {
        RenderSystem.enableCull();
        RenderSystem.depthFunc(GL11.GL_LEQUAL);
        RenderSystem.lineWidth(1.0F);
    }
}
