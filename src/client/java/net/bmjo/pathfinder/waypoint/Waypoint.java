package net.bmjo.pathfinder.waypoint;

import net.bmjo.pathfinder.PathfinderClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.util.SkinTextures;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;
import java.util.function.Supplier;

public class Waypoint {
    private final BlockPos pos;
    private final Text name;
    private final Supplier<SkinTextures> skin;
    private final long created;
    private final boolean farAway;

    private Waypoint(BlockPos pos, Text name, Supplier<SkinTextures> skin, long created) {
        this.pos = pos;
        this.name = name;
        this.skin = skin;
        this.created = created;
        this.farAway = !isClientInRange(this.pos, 10);
    }

    public static Waypoint create(BlockPos pos, UUID owner) { //TODO FIX!!!
        PlayerListEntry playerListEntry = PathfinderClient.getPlayer().networkHandler.getPlayerListEntry(owner);
        String name = playerListEntry.getProfile().getName();
        Text displayName = playerListEntry.getDisplayName();
        Supplier<SkinTextures> skin = playerListEntry::getSkinTextures;
        return new Waypoint(pos, displayName != null ? displayName : Text.of(name), skin, System.currentTimeMillis());
    }

    public BlockPos pos() {
        return this.pos;
    }

    public Text name() {
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
