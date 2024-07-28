package net.bmjo.pathfinder.waypoint.type;

import net.bmjo.pathfinder.waypoint.Waypoint;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.ItemEntity;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.UUID;

public class ItemWaypoint extends Waypoint {
    private final ItemEntity item;

    private ItemWaypoint(UUID owner, ItemEntity item) {
        super(owner);
        this.item = item;
    }

    public static ItemWaypoint create(UUID owner, ItemEntity item) {
        return new ItemWaypoint(owner, item);
    }

    @Override
    public Vec3d pos() {
        return item.getPos();
    }

    @Override
    public RegistryKey<World> dimension() {
        return item.getWorld().getRegistryKey();
    }

    @Override
    public void drawIcon(DrawContext drawContext) {
        MinecraftClient mc = MinecraftClient.getInstance();
        World world = mc.world;
        assert world != null;
        MatrixStack matrices = drawContext.getMatrices();
        matrices.push();
        float scale = 10.0F;
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(180.0F));
        matrices.translate(0.0F, scale / 2 + 0.5F, 0.0F);
        matrices.scale(scale, scale, scale);
        mc.getItemRenderer().renderItem(this.item.getStack(), ModelTransformationMode.GUI, 14680272, OverlayTexture.DEFAULT_UV, matrices, drawContext.getVertexConsumers(), world, 1);
        matrices.pop();
    }

    @Override
    public float getYOffset() {
        return this.item.getHeight() + 0.5F;
    }

    @Override
    public boolean tryRemove() {
        return super.tryRemove() && item.isRemoved();
    }
}
