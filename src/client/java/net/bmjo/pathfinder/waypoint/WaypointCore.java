package net.bmjo.pathfinder.waypoint;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.systems.VertexSorter;
import net.bmjo.pathfinder.waypoint.render.PathfinderShaders;
import net.bmjo.pathfinder.waypoint.render.WaypointRenderer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.Window;
import net.minecraft.client.util.math.MatrixStack;
import org.joml.Matrix4f;

public class WaypointCore {
    private static boolean renderingWorld = false;
    private static final Matrix4f waypointsProjection = new Matrix4f();
    private static final Matrix4f waypointModelView = new Matrix4f();

    public static void beforeRenderWorld() {
        renderingWorld = true;
    }

    public static void onResetProjectionMatrix(Matrix4f matrixIn) {
        if (renderingWorld) {
            waypointsProjection.identity();
            waypointsProjection.mul(matrixIn);
            renderingWorld = false;
        }
    }

    public static void onWorldModelViewMatrix(MatrixStack matrixStack) {
        waypointModelView.identity();
        waypointModelView.mul(matrixStack.peek().getPositionMatrix());
    }

    public static void onRenderStart() {
        PathfinderShaders.ensureShaders();
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
