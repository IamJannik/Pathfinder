package net.bmjo.pathfinder.waypoint;

import net.bmjo.pathfinder.PathfinderClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.util.SkinTextures;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;
import java.util.function.Supplier;

public class Waypoint {
    private final BlockPos pos;
    private final String name;
    private final Supplier<SkinTextures> skin;
    private final long created;
    private final boolean farAway;

    private Waypoint(BlockPos pos, String name, Supplier<SkinTextures> skin, long created) {
        this.pos = pos;
        this.name = name;
        this.skin = skin;
        this.created = created;
        this.farAway = !isClientInRange(this.pos, 10);
    }

    public static Waypoint create(BlockPos pos, UUID owner) {
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
    }

    public BlockPos pos() {
        return this.pos;
    }

    public String name() {
        return this.name;
    }

    @Nullable
    public SkinTextures skin() {
        return this.skin.get();
    }

    public int posX(double dimDiv) {
        return dimDiv == 1.0 ? this.pos.getX() : (int) Math.floor((double) this.pos.getX() / dimDiv);
    }

    public int posY(double dimDiv) {
        return dimDiv == 1.0 ? this.pos.getY() : (int) Math.floor((double) this.pos.getY() / dimDiv);
    }

    public int posZ(double dimDiv) {
        return dimDiv == 1.0 ? this.pos.getZ() : (int) Math.floor((double) this.pos.getZ() / dimDiv);
    }

    public boolean tryRemove() {
        return System.currentTimeMillis() - this.created >= 10 * 60 * 1000 || (this.farAway && isClientInRange(this.pos, 3)); // 10 min
    }

    private static boolean isClientInRange(BlockPos pos, int distance) {
        ClientPlayerEntity player = PathfinderClient.getPlayer();
        return player != null && player.getBlockPos().isWithinDistance(pos, distance);
    }
}
