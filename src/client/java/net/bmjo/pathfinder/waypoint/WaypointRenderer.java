package net.bmjo.pathfinder.waypoint;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.PlayerSkinDrawer;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.BufferBuilderStorage;
import net.minecraft.client.render.Camera;
import net.minecraft.client.util.SkinTextures;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

/**
 * Manages the rendering of waypoints in the Minecraft world, including player heads, labels, and distances.
 *
 * @author BMJO
 * @version 2.0
 */
public final class WaypointRenderer {
    /**
     * The singleton instance of the WaypointRenderer.
     */
    private static final WaypointRenderer INSTANCE = new WaypointRenderer();
    private final MinecraftClient MC;
    @Nullable
    private TextRenderer textRenderer;
    private final DrawContext drawContext;
    private final WaypointFilter filter = new WaypointFilter();
    private static final float SIZE = 0.6F;

    private WaypointRenderer() {
        MC = MinecraftClient.getInstance();
        drawContext = new DrawContext(MC, new BufferBuilderStorage(256).getEntityVertexConsumers());
    }

    /**
     * Gets the singleton instance of the WaypointRenderer.
     *
     * @return The singleton instance of the WaypointRenderer.
     */
    public static WaypointRenderer getInstance() {
        return INSTANCE;
    }

    public void render() {
        if (MC.player == null || MC.world == null)
            return;

        this.textRenderer = MC.textRenderer;
        if (this.textRenderer == null)
            return;

        List<Waypoint> waypoints = new ArrayList<>(WaypointHandler.WAYPOINTS.values());
        if (!waypoints.isEmpty()) {
            ClientPlayerEntity player = MC.player;
            boolean showAllInfo = player.isSneaking();
            Camera camera = MC.gameRenderer.getCamera();
            Vec3d cameraPos = camera.getPos();

            Vec3d lookVector = Vec3d.fromPolar(camera.getPitch(), camera.getYaw());
            this.filter.setParams(lookVector, cameraPos, player.getWorld().getRegistryKey());
            Stream<Waypoint> waypointStream = waypoints.stream().filter(this.filter);

            this.renderWaypoints(waypointStream.iterator(), camera, showAllInfo);
        }
    }

    private void renderWaypoints(Iterator<Waypoint> waypoints, Camera camera, boolean showAllInfo) {
        MatrixStack matrixStack = this.drawContext.getMatrices();
        matrixStack.push();

        while (waypoints.hasNext()) {
            Waypoint waypoint = waypoints.next();
            this.renderWaypoint(waypoint, camera, showAllInfo);
            matrixStack.translate(0.0F, 0.0F, 0.1F);
        }

        matrixStack.pop();
    }

    private void renderWaypoint(Waypoint waypoint, Camera camera, boolean showAllInfo) {
        MatrixStack matrixStack = drawContext.getMatrices();
        matrixStack.push();

        Vec3d waypointPos = new Vec3d(waypoint.posX(), waypoint.posY(), waypoint.posZ());
        Vec3d transformVec = waypointPos.subtract(camera.getPos());
        float distance = (float) transformVec.length();

        if (distance >= 0.0D) {
            // ORIGIN
            matrixStack.multiply(camera.getRotation().invert());
            Vec3d waypointOffset = Waypoint.getOffset();
            matrixStack.translate(waypointOffset.getX(), waypointOffset.getY(), waypointOffset.getZ()); // over block offset

            //POSITION
            matrixStack.translate(transformVec.x, transformVec.y, transformVec.z); // waypoint position offset

            // ANGLE
            Quaternionf quaternion = new Quaternionf().rotateXYZ((float) Math.toRadians(camera.getPitch()), (float) -Math.toRadians(camera.getYaw()), (float) Math.toRadians(180.0F));
            matrixStack.multiply(quaternion.invert()); // rotate to player view

            // SIZE
            // TODO GUI SCALE
            float scale = SIZE / 16.0F;
            float distanceScale = Math.max(scale * (distance / 4.0F), scale);
            matrixStack.scale(distanceScale, distanceScale, distanceScale);


            String name = waypoint.owner();
            String distanceText = "";

            if (distance > 10.0D) {
                boolean couldShowLabels = Math.abs(waypoint.getViewAngelToPlayer()) < 10.0F;

                if (showAllInfo || couldShowLabels) {
                    if (distance >= 10000.0D) { //KM
                        distanceText = new DecimalFormat("0.0").format(distance / 1000.0) + "km";
                    } else {
                        distanceText = new DecimalFormat("0.0").format(distance) + "m";
                    }
                } else {
                    name = "";
                }
            }

            this.drawAsOverlay(waypoint, name, distanceText);
        }
        matrixStack.pop();
    }

    private void drawAsOverlay(Waypoint waypoint, String name, String distance) {
        MatrixStack matrixStack = this.drawContext.getMatrices();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        matrixStack.push();
        //MC.getEntityRenderDispatcher().getRenderer(null).getTexture(null);

        this.drawPlayerHead(waypoint);

        matrixStack.scale(0.5F, 0.5F, 0.5F);
        if (!name.isEmpty()) {
            this.renderWaypointLabel(name);
        }

        if (!distance.isEmpty()) {
            this.renderWaypointLabel(distance);
        }
        matrixStack.pop();
    }

    private void drawPlayerHead(Waypoint waypoint) {
        SkinTextures skin = waypoint.skin();
        if (skin != null) {
            int headSize = 8;
            PlayerSkinDrawer.draw(this.drawContext, skin, -(headSize / 2), -(headSize + 1), headSize);
        }
    }

    private void renderWaypointLabel(String label) {
        assert this.textRenderer != null;
        MatrixStack matrixStack = this.drawContext.getMatrices();

        int nameW = this.textRenderer.getWidth(label);
        int bgW = nameW + 3;
        int halfBgW = bgW / 2;
        float bgOpacity = MC.options.getTextBackgroundOpacity(0.25F);
        int bgColor = (int) (bgOpacity * 255.0F) << 24;

        this.textRenderer.draw(label, -halfBgW + 2, 0, -1, false, matrixStack.peek().getPositionMatrix(), this.drawContext.getVertexConsumers(), TextRenderer.TextLayerType.SEE_THROUGH, bgColor, -1);
        this.drawContext.draw();
        matrixStack.translate(0.0, this.textRenderer.fontHeight, 0.0);
    }
}
