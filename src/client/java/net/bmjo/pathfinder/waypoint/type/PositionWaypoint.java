package net.bmjo.pathfinder.waypoint.type;

import net.bmjo.pathfinder.PathfinderClient;
import net.bmjo.pathfinder.waypoint.Waypoint;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.PlayerSkinDrawer;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.util.SkinTextures;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.UUID;
import java.util.function.Supplier;

public class PositionWaypoint extends Waypoint {
    private final GlobalPos pos;
    private final Supplier<SkinTextures> skin;

    private PositionWaypoint(UUID owner, GlobalPos pos, Supplier<SkinTextures> skin) {
        super(owner);
        this.pos = pos;
        this.skin = skin;
    }

    public static PositionWaypoint create(UUID owner, GlobalPos pos) {
        Supplier<SkinTextures> skin = () -> null;
        ClientPlayerEntity clientPlayer = PathfinderClient.getPlayer();
        if (clientPlayer != null) {
            PlayerListEntry playerListEntry = clientPlayer.networkHandler.getPlayerListEntry(owner);
            if (playerListEntry != null) {
                skin = playerListEntry::getSkinTextures;
            }
        }
        return new PositionWaypoint(owner, pos, skin);
    }

    @Override
    public Vec3d pos() {
        return this.pos.pos().toCenterPos();
    }

    @Override
    public RegistryKey<World> dimension() {
        return this.pos.dimension();
    }

    @Override
    public void drawIcon(DrawContext drawContext) {
        var skin = this.skin.get();
        if (skin != null) {
            int headSize = 8;
            PlayerSkinDrawer.draw(drawContext, skin, -(headSize / 2), -(headSize + 1), headSize);
        }
    }

    @Override
    public float getYOffset() {
        return 1.0F;
    }
}
