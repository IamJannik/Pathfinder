package net.bmjo.pathfinder.waypoint;

import net.bmjo.pathfinder.PathfinderClient;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.render.Camera;
import net.minecraft.client.util.SkinTextures;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;
import java.util.function.Supplier;

public class Waypoint {
    private final GlobalPos pos;
    private final String name;
    private final Supplier<SkinTextures> skin;
    private final long created;
    private final boolean farAway;

    private Waypoint(GlobalPos pos, String name, Supplier<SkinTextures> skin, long created) {
        this.pos = pos;
        this.name = name;
        this.skin = skin;
        this.created = created;
        this.farAway = !this.isClientInRange(10);
    }

    public static Waypoint create(GlobalPos pos, UUID owner) {
        String name = "";
        Supplier<SkinTextures> skin = () -> null;
        ClientPlayerEntity clientPlayer = PathfinderClient.getPlayer();
        if (clientPlayer != null) {
            PlayerListEntry playerListEntry = clientPlayer.networkHandler.getPlayerListEntry(owner);
            if (playerListEntry != null) {
                name = playerListEntry.getProfile().getName();
                skin = playerListEntry::getSkinTextures;
            }
        }
        return new Waypoint(pos, name, skin, System.currentTimeMillis());
        //minecraft:dimension / minecraft:overworld
    }

    public BlockPos pos() {
        return this.pos.getPos();
    }

    public RegistryKey<World> dimension() {
        return this.pos.getDimension();
    }

    public String name() {
        return this.name;
    }

    @Nullable
    public SkinTextures skin() {
        return this.skin.get();
    }

    public int posX() {
        return this.pos().getX();
    }

    public int posY() {
        return this.pos().getY();
    }

    public int posZ() {
        return this.pos().getZ();
    }

    public boolean tryRemove() {
        return System.currentTimeMillis() - this.created >= 10 * 60 * 1000 || this.farAway && this.isClientInRange(3);
    }

    private boolean isClientInRange(int distance) {
        ClientPlayerEntity player = PathfinderClient.getPlayer();
        return player != null && player.getBlockPos().isWithinDistance(this.pos.getPos(), distance);
    }

    public float getAngelToWaypoint() {
        return getAngelToWaypoint(this.pos());
    }

    public static float getAngelToWaypoint(BlockPos blockPos) {
        MinecraftClient mc = MinecraftClient.getInstance();

        assert mc.world != null;
        int wX = blockPos.getX();
        int wZ = blockPos.getZ();

        Camera camera = mc.gameRenderer.getCamera();
        Vec3d cameraPos = camera.getPos();
        double offX = (double) wX - cameraPos.getX() + 0.5;
        double offZ = (double) wZ - cameraPos.getZ() + 0.5;

        float Z = (float) (offZ == 0.0D ? 0.001F : offZ);
        float angle = (float) Math.toDegrees(Math.atan(-offX / (double) Z));
        if (offZ < 0.0) {
            if (offX < 0.0) {
                angle += 180.0F;
            } else {
                angle -= 180.0F;
            }
        }

        float offset = MathHelper.wrapDegrees(angle - camera.getYaw());
        return Math.abs(offset);
    }
}
