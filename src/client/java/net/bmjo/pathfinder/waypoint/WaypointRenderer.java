package net.bmjo.pathfinder.waypoint;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.PlayerSkinDrawer;
import net.minecraft.client.render.BufferBuilderStorage;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.DiffuseLighting;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.SkinTextures;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

public final class WaypointRenderer {
    private static final WaypointRenderer INSTANCE = new WaypointRenderer();
    private final MinecraftClient MC;
    @Nullable
    private TextRenderer textRenderer;
    private final DrawContext matrixStack;
    private final DrawContext matrixStackOverlay;
    @Nullable
    private Waypoint closestWaypoint;
    @Nullable
    private Waypoint previousClosest;
    private double workingClosestCos; // angle
    private final WaypointFilter filter = new WaypointFilter();

    private WaypointRenderer() {
        MC = MinecraftClient.getInstance();
        matrixStack = new DrawContext(MC, new BufferBuilderStorage().getEntityVertexConsumers());
        matrixStackOverlay = new DrawContext(MC, new BufferBuilderStorage().getEntityVertexConsumers());
    }

    public static WaypointRenderer getInstance() {
        return INSTANCE;
    }

    /**
     * Render method for waypoints in the world.
     *
     * @param waypointsProjection The Matrix4f instance representing the projection of the waypoints.
     * @param worldModelView      The Matrix4f instance for the representation of the world model view.
     * @author BMJO
     */
    public void render(Matrix4f waypointsProjection, Matrix4f worldModelView) {
        if (MC.player == null) {
            return;
        }

        this.textRenderer = MC.textRenderer;
        if (this.textRenderer == null)
            return;

        MatrixStack matrixStack = this.matrixStack.getMatrices();
        MatrixStack matrixStackOverlay = this.matrixStackOverlay.getMatrices();

        RenderSystem.disableCull();
        matrixStack.push();
        matrixStack.peek().getPositionMatrix().mul(worldModelView);
        DiffuseLighting.disableGuiDepthLighting();

        matrixStackOverlay.push();

        List<Waypoint> waypoints = new ArrayList<>(WaypointHandler.WAYPOINTS.values());
        if (!waypoints.isEmpty() && MC.world != null) {
            Entity entity = MC.getCameraEntity();
            Camera activeRender = MC.gameRenderer.getCamera();
            assert entity != null;
            Vec3d entityPos = entity.getPos();
            Vec3d cameraPos = activeRender.getPos();

            Vector3f lookVector = activeRender.getHorizontalPlane().get(new Vector3f());
            this.filter.setParams(lookVector, cameraPos, entity.getWorld().getRegistryKey());
            Stream<Waypoint> waypointStream = waypoints.stream().filter(this.filter);

            double fov = MC.options.getFov().getValue().doubleValue();
            double clampDepth = getWaypointsClampDepth(fov, MC.getWindow().getFramebufferHeight());

            VertexConsumerProvider.Immediate vertexConsumerProvider = this.matrixStackOverlay.getVertexConsumers();
            this.renderWaypoints(waypointStream.iterator(), cameraPos, entity, entityPos, lookVector, clampDepth, vertexConsumerProvider, waypointsProjection);
        }

        matrixStackOverlay.pop();
        RenderSystem.enableDepthTest();
        RenderSystem.enableCull();
        RenderSystem.depthMask(true);
        DiffuseLighting.enableGuiDepthLighting();
        matrixStack.pop();
    }

    private void renderWaypoints(Iterator<Waypoint> iter, Vec3d cameraPos, Entity entity, Vec3d entityPos, Vector3f lookVector, double clampDepth, VertexConsumerProvider.Immediate vertexConsumerProvider, Matrix4f waypointsProjection) {
        MatrixStack matrixStackOverlay = this.matrixStackOverlay.getMatrices();
        matrixStackOverlay.translate(0.0F, 0.0F, -2980.0F);

        int count = 0;
        this.closestWaypoint = null;
        boolean showAllInfo = entity.isSneaking();

        while (iter.hasNext()) {
            Waypoint waypoint = iter.next();
            this.renderWaypoint(waypoint, lookVector, clampDepth, cameraPos, entityPos, vertexConsumerProvider, waypointsProjection, false, showAllInfo);
            ++count;
            if (count < 19500) {
                matrixStackOverlay.translate(0.0F, 0.0F, 0.1F);
            }
        }

        if (!showAllInfo && this.previousClosest != null) {
            this.renderWaypoint(this.previousClosest, lookVector, clampDepth, cameraPos, entityPos, vertexConsumerProvider, waypointsProjection, true, false);
        }

        this.previousClosest = this.closestWaypoint;
        vertexConsumerProvider.draw();
        RenderSystem.clear(256, MinecraftClient.IS_SYSTEM_MAC);
    }

    private void renderWaypoint(Waypoint waypoint, Vector3f lookVector, double depthClamp, Vec3d cameraPos, Vec3d entityPos, VertexConsumerProvider.Immediate vertexConsumerProvider, Matrix4f waypointsProjection, boolean isTheMain, boolean showAllInfo) {
        MatrixStack matrixStack = this.matrixStack.getMatrices();
        MatrixStack matrixStackOverlay = this.matrixStackOverlay.getMatrices();

        int wX = waypoint.posX();
        int wZ = waypoint.posZ();

        double offX = (double) wX - cameraPos.getX() + 0.5;
        double offY = (double) waypoint.posY() - cameraPos.getY() + 1.0;
        double offZ = (double) wZ - cameraPos.getZ() + 0.5;

        double distance2D = Math.sqrt(offX * offX + offZ * offZ);

        if (distance2D >= 0.0D) {
            String name = waypoint.name();
            String distanceText = "";

            double depth = offX * (double) lookVector.x() + offY * (double) lookVector.y() + offZ * (double) lookVector.z();
            double correctOffX = entityPos.getX() - (double) wX - 0.5;
            double correctOffY = entityPos.getY() - (double) waypoint.posY();

            double distance = Math.sqrt(offX * offX + offY * offY + offZ * offZ);
            double correctOffZ = entityPos.getZ() - (double) wZ - 0.5;
            double correctDistance = Math.sqrt(correctOffX * correctOffX + correctOffY * correctOffY + correctOffZ * correctOffZ);

            if (correctDistance > 10.0D) {
                boolean couldShowLabels = waypoint.getAngelToWaypoint() < 10;
                boolean showDLabels = couldShowLabels && shouldShowDistance(waypoint, isTheMain, showAllInfo, depth, distance);

                if (showDLabels) {
                    if (correctDistance >= 10000.0D) { //KM
                        distanceText = new DecimalFormat("0.0").format(correctDistance / 1000.0) + "km";
                    } else {
                        distanceText = new DecimalFormat("0.0").format(correctDistance) + "m";
                    }
                } else {
                    name = "";
                }
            }

            if (showAllInfo || this.previousClosest != waypoint || isTheMain) {
                matrixStack.push();
                matrixStackOverlay.push();
                if (distance > 250000.0) {
                    double cos = 250000.0 / distance;
                    offX *= cos;
                    offY *= cos;
                    offZ *= cos;
                }

                matrixStack.translate(offX, offY, offZ);
                this.drawAsOverlay(waypoint, name, distanceText, vertexConsumerProvider, waypointsProjection, depthClamp, depth);
                RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
                matrixStack.pop();
                matrixStackOverlay.pop();
            }
        }
    }

    private boolean shouldShowDistance(Waypoint waypoint, boolean isTheMain, boolean showAllInfo, double depth, double distance) {
        if (isTheMain) {
            return true;
        } else {
            double cos = depth / distance;
            if (this.closestWaypoint == null || cos > this.workingClosestCos) {
                this.closestWaypoint = waypoint;
                this.workingClosestCos = cos;
            }
            return showAllInfo;
        }
    }

    private void drawAsOverlay(Waypoint waypoint, String name, String distance, VertexConsumerProvider.Immediate vertexConsumerProvider, Matrix4f waypointsProjection, double depthClamp, double depth) {
        MatrixStack matrixStack = this.matrixStack.getMatrices();
        MatrixStack matrixStackOverlay = this.matrixStackOverlay.getMatrices();

        Vector4f origin4f = new Vector4f(0.0F, 0.0F, 0.0F, 1.0F);
        origin4f.mul(matrixStack.peek().getPositionMatrix());
        origin4f.mul(waypointsProjection);
        int overlayPosX = (int) ((1.0F + origin4f.x() / origin4f.w()) / 2.0F * (float) MC.getWindow().getFramebufferWidth());
        int overlayPosY = (int) ((1.0F - origin4f.y() / origin4f.w()) / 2.0F * (float) MC.getWindow().getFramebufferHeight());
        matrixStackOverlay.translate((float) overlayPosX, (float) overlayPosY, 0.0F);
        if (depth < depthClamp) {
            float scale = (float) (depthClamp / depth);
            matrixStackOverlay.scale(scale, scale, scale);
        }
        this.drawPlayerHead(waypoint, name, distance, vertexConsumerProvider);
    }

    private void drawPlayerHead(Waypoint waypoint, String name, String distance, VertexConsumerProvider.Immediate vertexConsumerProvider) {
        MatrixStack matrixStackOverlay = this.matrixStackOverlay.getMatrices();

        int iconScale = 2;
        double nameScale = 1;
        double distanceScale = 1;

        int halfIconPixel = iconScale / 2;
        matrixStackOverlay.translate((float) halfIconPixel, 0.0F, 0.0F);
        matrixStackOverlay.scale((float) iconScale, (float) iconScale, 1.0F);

        SkinTextures skin = waypoint.skin();
        if (skin != null) {
            PlayerSkinDrawer.draw(this.matrixStackOverlay, skin, -5, -9, 8);
        }

        RenderSystem.enableBlend();
        RenderSystem.blendFuncSeparate(770, 771, 1, 0);
        boolean showName = !name.isEmpty();
        matrixStackOverlay.scale((float) (1.0 / iconScale), (float) (1.0 / iconScale), 1.0F);
        matrixStackOverlay.translate((float) (-halfIconPixel), 0.0F, 0.0F);
        matrixStackOverlay.translate(0.0F, 2.0F, 0.0F);
        if (showName) {
            this.renderWaypointLabel(name, nameScale, vertexConsumerProvider);
        }

        matrixStackOverlay.translate(0.0F, 2.0F, 0.0F);
        if (!distance.isEmpty()) {
            this.renderWaypointLabel(distance, distanceScale, vertexConsumerProvider);
        }
    }

    private void renderWaypointLabel(String label, double labelScale, VertexConsumerProvider.Immediate vertexConsumerProvider) {
        MatrixStack matrixStackOverlay = this.matrixStackOverlay.getMatrices();
        assert this.textRenderer != null;
        int nameW = this.textRenderer.getWidth(label);
        int bgW = nameW + 3;
        int halfBgW = bgW / 2;
        int halfNamePixel = 0;
        if ((bgW & 1) != 0) {
            halfNamePixel = (int) labelScale - (int) labelScale / 2;
            matrixStackOverlay.translate((float) (-halfNamePixel), 0.0F, 0.0F);
        }

        matrixStackOverlay.scale((float) labelScale, (float) labelScale, 1.0F);
        MinecraftClient.getInstance().textRenderer.draw(label, -halfBgW + 2, 1.0F, -1, false, matrixStackOverlay.peek().getPositionMatrix(), vertexConsumerProvider, TextRenderer.TextLayerType.NORMAL, 0, 15728880);
        matrixStackOverlay.translate(0.0F, 9.0F, 0.0F);
        matrixStackOverlay.scale((float) (1.0 / labelScale), (float) (1.0 / labelScale), 1.0F);
        if ((bgW & 1) != 0) {
            matrixStackOverlay.translate((float) halfNamePixel, 0.0F, 0.0F);
        }

        RenderSystem.enableBlend();
    }

    private static double getWaypointsClampDepth(double fov, int height) {
        int baseIconHeight = 8;
        double worldSizeAtClampDepth = 0.19200003147125244 * (double) height / (double) baseIconHeight;
        double fovMultiplier = 2.0 * Math.tan(Math.toRadians(fov / 2.0));
        return worldSizeAtClampDepth / fovMultiplier;
    }
}
