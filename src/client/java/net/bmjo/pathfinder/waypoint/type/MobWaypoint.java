package net.bmjo.pathfinder.waypoint.type;

import net.bmjo.pathfinder.Pathfinder;
import net.bmjo.pathfinder.util.MobFaceDrawer;
import net.bmjo.pathfinder.waypoint.Waypoint;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.UUID;

public class MobWaypoint extends Waypoint {
    MobEntity mob;

    private MobWaypoint(UUID owner, MobEntity mob) {
        super(owner);
        this.mob = mob;
    }

    public static MobWaypoint create(UUID owner, int id, World world) {
        Entity entity = world.getEntityById(id);
        if (!(entity instanceof MobEntity mob))
            throw new IllegalArgumentException("CreateEntity with id " + id + " is not a mob"); // was, wenn nicht gleiche dim
        return create(owner, mob);
    }

    public static MobWaypoint create(UUID owner, MobEntity mob) {
        return new MobWaypoint(owner, mob);
    }

    @Override
    public Vec3d pos() {
        return mob.getPos();
    }

    @Override
    public RegistryKey<World> dimension() {
        return mob.getWorld().getRegistryKey();
    }

    @Override
    public void drawIcon(DrawContext drawContext) {
        MatrixStack matrices = drawContext.getMatrices();
        matrices.push();
        matrices.translate(0.0F, 0.5F, 0.0F);
        if (MobFaceDrawer.hasFace(this.mob.getType())) {
            MobFaceDrawer.drawFace(drawContext, this.mob.getType());
        } else {
            Identifier texture = Pathfinder.identifier("textures/entity/mob.png");
            drawContext.drawTexture(texture, -4, -8, 0, 0, 8, 8, 8, 8);
            drawContext.draw();
        }
        matrices.pop();
    }

    @Override
    public boolean tryRemove() {
        return super.tryRemove() && mob.isRemoved() || mob.isDead();
    }

    @Override
    public float getYOffset() {
        return this.mob.getHeight() + 0.5F;
    }
}
