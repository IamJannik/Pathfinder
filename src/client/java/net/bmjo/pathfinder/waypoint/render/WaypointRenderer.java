package net.bmjo.pathfinder.waypoint.render;

import com.mojang.blaze3d.systems.RenderSystem;
import net.bmjo.pathfinder.waypoint.Waypoint;
import net.bmjo.pathfinder.waypoint.WaypointFilter;
import net.bmjo.pathfinder.waypoint.WaypointHandler;
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
import net.minecraft.util.math.MathHelper;
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

        Entity entity = MC.getCameraEntity();
        Camera activeRender = MC.gameRenderer.getCamera();
        assert entity != null;
        double actualEntityX = entity.getX();
        double actualEntityY = entity.getY();
        double actualEntityZ = entity.getZ();
        Vec3d cameraPos = activeRender.getPos();
        double cameraX = cameraPos.getX();
        double cameraY = cameraPos.getY();
        double cameraZ = cameraPos.getZ();
        RenderSystem.disableCull();
        matrixStack.push();
        matrixStack.peek().getPositionMatrix().mul(worldModelView);
        DiffuseLighting.disableGuiDepthLighting();
        double fov = MC.options.getFov().getValue().doubleValue();
        float cameraAngleYaw = activeRender.getYaw();
        Vector3f lookVector = activeRender.getHorizontalPlane().get(new Vector3f());
        double clampDepth = getWaypointsClampDepth(fov, MC.getWindow().getFramebufferHeight());
        assert MC.world != null;
        double dimDiv = MC.world.getDimension().coordinateScale();
        List<Waypoint> waypoints = new ArrayList<>(WaypointHandler.WAYPOINTS.values());

        matrixStackOverlay.push();
        matrixStackOverlay.translate(0.0F, 0.0F, -2980.0F);
        if (!waypoints.isEmpty()) {
            this.filter.setParams(lookVector, cameraX, cameraY, cameraZ, dimDiv);
            Stream<Waypoint> waypointStream = waypoints.stream().filter(this.filter);

            VertexConsumerProvider.Immediate vertexConsumerProvider = this.matrixStackOverlay.getVertexConsumers();
            this.renderWaypoints(waypointStream.iterator(), cameraX, cameraY, cameraZ, entity, dimDiv, actualEntityX, actualEntityY, actualEntityZ, cameraAngleYaw, lookVector, clampDepth, vertexConsumerProvider, waypointsProjection);
        }

        matrixStackOverlay.pop();
        RenderSystem.enableDepthTest();
        RenderSystem.enableCull();
        RenderSystem.depthMask(true);
        DiffuseLighting.enableGuiDepthLighting();
        matrixStack.pop();
    }

    private void renderWaypoints(Iterator<Waypoint> iter, double cameraX, double cameraY, double cameraZ, Entity entity, double dimDiv, double actualEntityX, double actualEntityY, double actualEntityZ, float cameraAngleYaw, Vector3f lookVector, double clampDepth, VertexConsumerProvider.Immediate vertexConsumerProvider, Matrix4f waypointsProjection) {
        MatrixStack matrixStackOverlay = this.matrixStackOverlay.getMatrices();

        int count = 0;
        this.closestWaypoint = null;
        boolean showAllInfo = entity.isSneaking();

        while (iter.hasNext()) {
            Waypoint waypoint = iter.next();
            this.renderWaypoint(waypoint, cameraAngleYaw, lookVector, clampDepth, cameraX, cameraY, cameraZ, dimDiv, actualEntityX, actualEntityY, actualEntityZ, vertexConsumerProvider, waypointsProjection, false, showAllInfo);
            ++count;
            if (count < 19500) {
                matrixStackOverlay.translate(0.0F, 0.0F, 0.1F);
            }
        }

        if (!showAllInfo && this.previousClosest != null) {
            this.renderWaypoint(this.previousClosest, cameraAngleYaw, lookVector, clampDepth, cameraX, cameraY, cameraZ, dimDiv, actualEntityX, actualEntityY, actualEntityZ, vertexConsumerProvider, waypointsProjection, true, false);
        }

        this.previousClosest = this.closestWaypoint;
        vertexConsumerProvider.draw();
        RenderSystem.clear(256, MinecraftClient.IS_SYSTEM_MAC);
    }

    private void renderWaypoint(Waypoint waypoint, float cameraAngleYaw, Vector3f lookVector, double depthClamp, double cameraX, double cameraY, double cameraZ, double dimDiv, double actualEntityX, double actualEntityY, double actualEntityZ, VertexConsumerProvider.Immediate vertexConsumerProvider, Matrix4f waypointsProjection, boolean isTheMain, boolean showAllInfo) {
        MatrixStack matrixStack = this.matrixStack.getMatrices();
        MatrixStack matrixStackOverlay = this.matrixStackOverlay.getMatrices();

        int wX = waypoint.posX(dimDiv);
        int wZ = waypoint.posZ(dimDiv);

        double offX = (double) wX - cameraX + 0.5;
        double offY = (double) waypoint.posY(1) - cameraY + 1.0;
        double offZ = (double) wZ - cameraZ + 0.5;

        double depth = offX * (double) lookVector.x() + offY * (double) lookVector.y() + offZ * (double) lookVector.z();
        double correctOffX = actualEntityX - (double) wX - 0.5;
        double correctOffY = actualEntityY - (double) waypoint.posY(1);

        double correctOffZ = actualEntityZ - (double) wZ - 0.5;
        double correctDistance = Math.sqrt(correctOffX * correctOffX + correctOffY * correctOffY + correctOffZ * correctOffZ);
        double distance2D = Math.sqrt(offX * offX + offZ * offZ);
        double distance = Math.sqrt(offX * offX + offY * offY + offZ * offZ);

        if (distance2D >= 0.0D) {
            String name = waypoint.name();
            String distanceText = "";
            if (correctDistance > 10.0D) {
                boolean couldShowDistance = couldShowDistance(cameraAngleYaw, offZ, offX);
                boolean showDistance = couldShowDistance && shouldShowDistance(waypoint, isTheMain, showAllInfo, depth, distance);

                if (showDistance) {
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

    private static boolean couldShowDistance(float cameraAngleYaw, double offZ, double offX) {
        float Z = (float) (offZ == 0.0D ? 0.001F : offZ);
        float angle = (float) Math.toDegrees(Math.atan(-offX / (double) Z));
        if (offZ < 0.0) {
            if (offX < 0.0) {
                angle += 180.0F;
            } else {
                angle -= 180.0F;
            }
        }

        float offset = MathHelper.wrapDegrees(angle - cameraAngleYaw);
        return Math.abs(offset) < 10;
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
        this.drawIconInWorld(waypoint, name, distance, vertexConsumerProvider);
    }

    private void drawIconInWorld(Waypoint waypoint, String name, String distance, VertexConsumerProvider.Immediate vertexConsumerProvider) {
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
        drawNormalText(matrixStackOverlay, label, (float) (-halfBgW + 2), 1.0F, -1, false, vertexConsumerProvider);
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

    public static void drawNormalText(MatrixStack matrices, String name, float x, float y, int color, boolean shadow, VertexConsumerProvider.Immediate vertexConsumerProvider) {
        MinecraftClient.getInstance().textRenderer.draw(name, x, y, color, shadow, matrices.peek().getPositionMatrix(), vertexConsumerProvider, TextRenderer.TextLayerType.NORMAL, 0, 15728880);
    }
}
