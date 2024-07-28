package net.bmjo.pathfinder.networking.payload;

import net.bmjo.pathfinder.Pathfinder;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import net.minecraft.util.Uuids;
import net.minecraft.util.math.BlockPos;

import java.util.UUID;

public class WaypointPayload {

    public static final Identifier CREATE_POS = Pathfinder.identifier("create_pos_waypoint");
    public static final Identifier CREATE_ENTITY = Pathfinder.identifier("create_entity_waypoint");
    public static final Identifier REMOVE = Pathfinder.identifier("remove_waypoint");

    public record CreatePos(UUID uuid, BlockPos blockPos, String dimension) implements CustomPayload {
        public static final CustomPayload.Id<CreatePos> ID = new CustomPayload.Id<>(CREATE_POS);
        public static final PacketCodec<RegistryByteBuf, CreatePos> CODEC = PacketCodec.tuple(
                Uuids.PACKET_CODEC, CreatePos::uuid,
                BlockPos.PACKET_CODEC, CreatePos::blockPos,
                PacketCodecs.STRING, CreatePos::dimension,
                CreatePos::new);

        @Override
        public CustomPayload.Id<? extends CustomPayload> getId() {
            return ID;
        }
    }

    public record CreateEntity(UUID uuid, int entityId) implements CustomPayload {
        public static final CustomPayload.Id<CreateEntity> ID = new CustomPayload.Id<>(CREATE_ENTITY);
        public static final PacketCodec<RegistryByteBuf, CreateEntity> CODEC = PacketCodec.tuple(
                Uuids.PACKET_CODEC, CreateEntity::uuid,
                PacketCodecs.INTEGER, CreateEntity::entityId,
                CreateEntity::new);

        @Override
        public CustomPayload.Id<? extends CustomPayload> getId() {
            return ID;
        }
    }

    public record Remove(UUID uuid) implements CustomPayload {
        public static final CustomPayload.Id<Remove> ID = new CustomPayload.Id<>(REMOVE);
        public static final PacketCodec<RegistryByteBuf, Remove> CODEC = PacketCodec.tuple(
                Uuids.PACKET_CODEC, Remove::uuid,
                Remove::new);

        @Override
        public CustomPayload.Id<? extends CustomPayload> getId() {
            return ID;
        }
    }

    public static void register() {
        PayloadTypeRegistry.playS2C().register(CreatePos.ID, CreatePos.CODEC);
        PayloadTypeRegistry.playS2C().register(CreateEntity.ID, CreateEntity.CODEC);
        PayloadTypeRegistry.playS2C().register(Remove.ID, Remove.CODEC);
    }
}
