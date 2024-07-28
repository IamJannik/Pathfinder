package net.bmjo.pathfinder.networking.payload;

import net.bmjo.pathfinder.Pathfinder;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

public class TeamWaypointPayload {

    public static final Identifier CREATE_POS = Pathfinder.identifier("create_team_pos_waypoint");
    public static final Identifier CREATE_ENTITY = Pathfinder.identifier("create_team_entity_waypoint");
    public static final Identifier REMOVE = Pathfinder.identifier("remove_team_waypoint");

    public record CreatePos(BlockPos blockPos, String dimension) implements CustomPayload {
        public static final CustomPayload.Id<CreatePos> ID = new CustomPayload.Id<>(CREATE_POS);
        public static final PacketCodec<RegistryByteBuf, CreatePos> CODEC = PacketCodec.tuple(
                BlockPos.PACKET_CODEC, CreatePos::blockPos,
                PacketCodecs.STRING, CreatePos::dimension,
                CreatePos::new);

        @Override
        public CustomPayload.Id<? extends CustomPayload> getId() {
            return ID;
        }
    }

    public record CreateEntity(int entityId) implements CustomPayload {
        public static final CustomPayload.Id<CreateEntity> ID = new CustomPayload.Id<>(CREATE_ENTITY);
        public static final PacketCodec<RegistryByteBuf, CreateEntity> CODEC = PacketCodec.tuple(
                PacketCodecs.INTEGER, CreateEntity::entityId,
                CreateEntity::new);

        @Override
        public CustomPayload.Id<? extends CustomPayload> getId() {
            return ID;
        }
    }

    public record Remove() implements CustomPayload {
        public static final CustomPayload.Id<Remove> ID = new CustomPayload.Id<>(REMOVE);
        public static final PacketCodec<RegistryByteBuf, Remove> CODEC = PacketCodec.unit(new Remove());

        @Override
        public CustomPayload.Id<? extends CustomPayload> getId() {
            return ID;
        }
    }

    public static void register() {
        PayloadTypeRegistry.playC2S().register(CreatePos.ID, CreatePos.CODEC);
        PayloadTypeRegistry.playC2S().register(CreateEntity.ID, CreateEntity.CODEC);
        PayloadTypeRegistry.playC2S().register(Remove.ID, Remove.CODEC);
    }
}
