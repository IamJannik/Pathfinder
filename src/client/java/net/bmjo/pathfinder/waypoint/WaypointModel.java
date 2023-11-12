package net.bmjo.pathfinder.waypoint;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.systems.VertexSorter;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.Window;
import net.minecraft.client.util.math.MatrixStack;
import org.joml.Matrix4f;

/**
 * Manages the rendering aspects of waypoints in the Minecraft world.
 */
public class WaypointModel {
    private static boolean renderingWorld = false;
    /**
     * The projection matrix for waypoints.
     */
    private static final Matrix4f waypointsProjection = new Matrix4f();
    /**
     * The model-view matrix for waypoints.
     */
    private static final Matrix4f waypointModelView = new Matrix4f();

    /**
     * Marks the beginning of the world rendering process.
     */
    public static void beforeRenderWorld() {
        renderingWorld = true;
    }

    /**
     * Handles the reset of the projection matrix during rendering.
     *
     * @param matrixIn The input matrix to be reset.
     */
    public static void onResetProjectionMatrix(Matrix4f matrixIn) {
        if (renderingWorld) {
            waypointsProjection.identity();
            waypointsProjection.mul(matrixIn);
            renderingWorld = false;
        }
    }

    /**
     * Handles the application of the world model-view matrix.
     *
     * @param matrixStack The matrix stack containing the world model-view matrix.
     */
    public static void onWorldModelViewMatrix(MatrixStack matrixStack) {
        waypointModelView.identity();
        waypointModelView.mul(matrixStack.peek().getPositionMatrix());
    }

    /**
     * Starts the rendering process for waypoints.
     */
    public static void onRenderStart() {
        Window mainwindow = MinecraftClient.getInstance().getWindow();
        Matrix4f projectionMatrixBU = RenderSystem.getProjectionMatrix();
        VertexSorter vertexSortingBU = RenderSystem.getVertexSorting();
        Matrix4f ortho = (new Matrix4f()).setOrtho(0.0F, (float) mainwindow.getFramebufferWidth(), (float) mainwindow.getFramebufferHeight(), 0.0F, 1000.0F, 3000.0F);
        RenderSystem.setProjectionMatrix(ortho, VertexSorter.BY_Z);
        RenderSystem.getModelViewStack().push();
        RenderSystem.getModelViewStack().loadIdentity();
        RenderSystem.applyModelViewMatrix();
        WaypointRenderer.getInstance().render(waypointsProjection, waypointModelView);
        RenderSystem.getModelViewStack().pop();
        RenderSystem.applyModelViewMatrix();
        RenderSystem.setProjectionMatrix(projectionMatrixBU, vertexSortingBU);
    }
}
