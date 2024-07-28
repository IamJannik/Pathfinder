package net.bmjo.pathfinder.waypoint;

import com.mojang.blaze3d.systems.RenderSystem;
import net.bmjo.pathfinder.Pathfinder;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;

import java.util.ArrayList;

public class WaypointHUDRenderer {
    private static final WaypointHUDRenderer INSTANCE = new WaypointHUDRenderer();
    private final MinecraftClient MC;

    private WaypointHUDRenderer() {
        MC = MinecraftClient.getInstance();
    }

    public static WaypointHUDRenderer getInstance() {
        return INSTANCE;
    }

    public void render(DrawContext drawContext, RenderTickCounter tickDelta) {
        var waypoints = new ArrayList<>(WaypointHandler.WAYPOINTS.values());
        if (!waypoints.isEmpty()) {
            var waypoint = waypoints.get(0);
            Vec3d waypointPos = waypoint.pos();

            GameRenderer gameRenderer = MC.gameRenderer;
            Camera camera = gameRenderer.getCamera();
            Vec3d cameraPos = camera.getPos();

            Vec3d relativePos = waypointPos.subtract(cameraPos);
            relativePos = relativePos.rotateY((float) Math.toRadians(MathHelper.wrapDegrees(camera.getYaw())));
            relativePos = relativePos.rotateX((float) Math.toRadians(MathHelper.wrapDegrees(camera.getPitch())));

            double fov = gameRenderer.getFov(camera, tickDelta.getTickDelta(true), true);
            double screenWidth = MC.getWindow().getScaledWidth();
            double screenHeight = MC.getWindow().getScaledHeight();
            double scale = (screenHeight / 2.0) / (relativePos.z * Math.tan(Math.toRadians(fov) / 2.0));


            double screenX = MathHelper.clamp(screenWidth / 2.0F - relativePos.x * scale, 0, screenWidth);
            double screenY = MathHelper.clamp(screenHeight / 2.0F - relativePos.y * scale - scale, 0, screenHeight);

            if (relativePos.z > 0.0D) {
                MatrixStack matrices = drawContext.getMatrices();
                matrices.push();

                matrices.translate(screenX, screenY, 0);
                gameRenderer.tiltViewWhenHurt(matrices, tickDelta.getTickDelta(true));
                gameRenderer.bobView(matrices, tickDelta.getTickDelta(true));

                Tessellator tessellator = Tessellator.getInstance();
                BufferBuilder buffer = tessellator.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);

                Matrix4f positionMatrix = matrices.peek().getPositionMatrix();
                buffer.vertex(positionMatrix, -10, -10, 0).color(1f, 1f, 1f, 1f).texture(0f, 0f);
                buffer.vertex(positionMatrix, -10, 10, 0).color(1f, 0f, 0f, 1f).texture(0f, 1f);
                buffer.vertex(positionMatrix, 10, 10, 0).color(0f, 1f, 0f, 1f).texture(1f, 1f);
                buffer.vertex(positionMatrix, 10, -10, 0).color(0f, 0f, 1f, 1f).texture(1f, 0f);

                RenderSystem.setShader(GameRenderer::getPositionTexColorProgram);
                RenderSystem.setShaderTexture(0, Pathfinder.identifier("icon.png"));
                RenderSystem.setShaderColor(1f, 1f, 1f, 1f);

                BufferRenderer.drawWithGlobalProgram(buffer.end());
                matrices.pop();
            }
        }
    }

    private static void isOnScreen(Vec3d transformVec, Camera camera) {
        double yawRad = Math.toRadians(-camera.getYaw());
        double pitchRad = Math.toRadians(-camera.getPitch());

        double cosYaw = Math.cos(yawRad);
        double sinYaw = Math.sin(yawRad);
        double cosPitch = Math.cos(pitchRad);
        double sinPitch = Math.sin(pitchRad);

        double x1 = transformVec.x * cosYaw - transformVec.z * sinYaw;
        double z1 = transformVec.x * sinYaw + transformVec.z * cosYaw;

        double y1 = transformVec.y * cosPitch - z1 * sinPitch;
        double z2 = transformVec.y * sinPitch + z1 * cosPitch;

        double fov = MinecraftClient.getInstance().options.getFov().getValue();
        double aspectRatio = (double) MinecraftClient.getInstance().getWindow().getFramebufferWidth() /
                (double) MinecraftClient.getInstance().getWindow().getFramebufferHeight();

        double fovRad = Math.toRadians(fov);
        double halfHeight = Math.tan(fovRad / 2.0);
        double halfWidth = aspectRatio * halfHeight;

        double screenX = (x1 / -z2) / halfWidth;
        double screenY = (y1 / -z2) / halfHeight;

        int screenWidth = MinecraftClient.getInstance().getWindow().getFramebufferWidth();
        int screenHeight = MinecraftClient.getInstance().getWindow().getFramebufferHeight();

        int screenXPos = (int) ((screenX + 1) / 2.0 * screenWidth);
        int screenYPos = (int) ((screenY + 1) / 2.0 * screenHeight);

        System.out.println("ScreenX: " + screenXPos + " ScreenY: " + screenYPos);
    }
}
