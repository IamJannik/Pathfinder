package net.bmjo.pathfinder.networking;

import net.bmjo.pathfinder.Pathfinder;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.scoreboard.AbstractTeam;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.Uuids;
import net.minecraft.util.math.BlockPos;

import java.util.List;
import java.util.UUID;

/**
 * Server Networking of Pathfinder.
 */
public class ServerNetworking {
    public static final Identifier CREATE_WAYPOINT = Pathfinder.identifier("create_waypoint");
    public static final Identifier CREATE_GANG_WAYPOINT = Pathfinder.identifier("create_gang_waypoint");
    public static final Identifier CREATE_TEAM_WAYPOINT = Pathfinder.identifier("create_team_waypoint");
    public static final Identifier REMOVE_WAYPOINT = Pathfinder.identifier("remove_waypoint");
    public static final Identifier REMOVE_GANG_WAYPOINT = Pathfinder.identifier("remove_gang_waypoint");
    public static final Identifier REMOVE_TEAM_WAYPOINT = Pathfinder.identifier("remove_team_waypoint");
    public static final Identifier IS_LOADED = Pathfinder.identifier("is_loaded");

    public record CreateWaypointPayload(UUID uuid, BlockPos blockPos, String dimension) implements CustomPayload {
        public static final CustomPayload.Id<CreateWaypointPayload> ID = new CustomPayload.Id<>(CREATE_WAYPOINT);
        public static final PacketCodec<RegistryByteBuf, CreateWaypointPayload> CODEC = PacketCodec.tuple(
                Uuids.PACKET_CODEC, CreateWaypointPayload::uuid,
                BlockPos.PACKET_CODEC, CreateWaypointPayload::blockPos,
                PacketCodecs.STRING, CreateWaypointPayload::dimension,
                CreateWaypointPayload::new);

        @Override
        public CustomPayload.Id<? extends CustomPayload> getId() {
            return ID;
        }
    }

    public record RemoveWaypointPayload(UUID uuid) implements CustomPayload {
        public static final CustomPayload.Id<RemoveWaypointPayload> ID = new CustomPayload.Id<>(REMOVE_WAYPOINT);
        public static final PacketCodec<RegistryByteBuf, RemoveWaypointPayload> CODEC = PacketCodec.tuple(
                Uuids.PACKET_CODEC, RemoveWaypointPayload::uuid,
                RemoveWaypointPayload::new);

        @Override
        public CustomPayload.Id<? extends CustomPayload> getId() {
            return ID;
        }
    }

    public record CreateGangWaypointPayload(UUID uuid, BlockPos blockPos, String dimension) implements CustomPayload {
        public static final CustomPayload.Id<CreateGangWaypointPayload> ID = new CustomPayload.Id<>(CREATE_GANG_WAYPOINT);
        public static final PacketCodec<RegistryByteBuf, CreateGangWaypointPayload> CODEC = PacketCodec.tuple(
                Uuids.PACKET_CODEC, CreateGangWaypointPayload::uuid,
                BlockPos.PACKET_CODEC, CreateGangWaypointPayload::blockPos,
                PacketCodecs.STRING, CreateGangWaypointPayload::dimension,
                CreateGangWaypointPayload::new);

        @Override
        public CustomPayload.Id<? extends CustomPayload> getId() {
            return ID;
        }
    }

    public record RemoveGangWaypointPayload(UUID uuid) implements CustomPayload {
        public static final CustomPayload.Id<RemoveGangWaypointPayload> ID = new CustomPayload.Id<>(REMOVE_GANG_WAYPOINT);
        public static final PacketCodec<RegistryByteBuf, RemoveGangWaypointPayload> CODEC = PacketCodec.tuple(
                Uuids.PACKET_CODEC, RemoveGangWaypointPayload::uuid,
                RemoveGangWaypointPayload::new);

        @Override
        public CustomPayload.Id<? extends CustomPayload> getId() {
            return ID;
        }
    }

    public record CreateTeamWaypointPayload(BlockPos blockPos, String dimension) implements CustomPayload {
        public static final CustomPayload.Id<CreateTeamWaypointPayload> ID = new CustomPayload.Id<>(CREATE_TEAM_WAYPOINT);
        public static final PacketCodec<RegistryByteBuf, CreateTeamWaypointPayload> CODEC = PacketCodec.tuple(
                BlockPos.PACKET_CODEC, CreateTeamWaypointPayload::blockPos,
                PacketCodecs.STRING, CreateTeamWaypointPayload::dimension,
                CreateTeamWaypointPayload::new);

        @Override
        public CustomPayload.Id<? extends CustomPayload> getId() {
            return ID;
        }
    }

    public record RemoveTeamWaypointPayload() implements CustomPayload {
        public static final CustomPayload.Id<RemoveTeamWaypointPayload> ID = new CustomPayload.Id<>(REMOVE_TEAM_WAYPOINT);
        public static final PacketCodec<RegistryByteBuf, RemoveTeamWaypointPayload> CODEC = PacketCodec.unit(new RemoveTeamWaypointPayload());

        @Override
        public CustomPayload.Id<? extends CustomPayload> getId() {
            return ID;
        }
    }

    public record IsLoadedPayload() implements CustomPayload {
        public static final CustomPayload.Id<IsLoadedPayload> ID = new CustomPayload.Id<>(IS_LOADED);
        public static final PacketCodec<RegistryByteBuf, IsLoadedPayload> CODEC = PacketCodec.unit(new IsLoadedPayload());

        @Override
        public CustomPayload.Id<? extends CustomPayload> getId() {
            return ID;
        }
    }

    public static void register() {
        PayloadTypeRegistry.playS2C().register(ServerNetworking.CreateWaypointPayload.ID, ServerNetworking.CreateWaypointPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(ServerNetworking.RemoveWaypointPayload.ID, ServerNetworking.RemoveWaypointPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(ServerNetworking.CreateGangWaypointPayload.ID, ServerNetworking.CreateGangWaypointPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(ServerNetworking.CreateTeamWaypointPayload.ID, ServerNetworking.CreateTeamWaypointPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(ServerNetworking.RemoveGangWaypointPayload.ID, ServerNetworking.RemoveGangWaypointPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(ServerNetworking.RemoveTeamWaypointPayload.ID, ServerNetworking.RemoveTeamWaypointPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(IsLoadedPayload.ID, IsLoadedPayload.CODEC);

        ServerPlayNetworking.registerGlobalReceiver(CreateGangWaypointPayload.ID, (payload, context) -> {
            UUID uuid = payload.uuid();
            BlockPos blockPos = payload.blockPos();
            String dimension = payload.dimension();
            try (MinecraftServer server = context.server()) {
                server.execute(() -> {
                    ServerPlayerEntity serverPlayer = server.getPlayerManager().getPlayer(uuid);
                    if (serverPlayer != null) {
                        ServerPlayNetworking.send(serverPlayer, new CreateWaypointPayload(uuid, blockPos, dimension));
                    }
                });
            }
        });

        ServerPlayNetworking.registerGlobalReceiver(CreateTeamWaypointPayload.ID, (payload, context) -> {
            ServerPlayerEntity sender = context.player();
            BlockPos blockPos = payload.blockPos();
            String dimension = payload.dimension();
            try (MinecraftServer server = context.server()) {
                server.execute(() -> {
                    for (ServerPlayerEntity serverPlayer : getTeamPlayer(server, sender)) {
                        ServerPlayNetworking.send(serverPlayer, new CreateWaypointPayload(sender.getUuid(), blockPos, dimension));
                    }
                });
            }
        });

        ServerPlayNetworking.registerGlobalReceiver(RemoveGangWaypointPayload.ID, (payload, context) -> {
            ServerPlayerEntity sender = context.player();
            UUID uuid = payload.uuid();
            try (MinecraftServer server = context.server()) {
                server.execute(() -> {
                    ServerPlayerEntity serverPlayer = server.getPlayerManager().getPlayer(uuid);
                    if (serverPlayer != null) {
                        ServerPlayNetworking.send(serverPlayer, new RemoveWaypointPayload(sender.getUuid()));
                    }
                });
            }
        });

        ServerPlayNetworking.registerGlobalReceiver(RemoveTeamWaypointPayload.ID, (payload, context) -> {
            ServerPlayerEntity sender = context.player();
            try (MinecraftServer server = context.server()) {
                server.execute(() -> {
                    for (ServerPlayerEntity serverPlayer : getTeamPlayer(server, sender)) {
                        ServerPlayNetworking.send(serverPlayer, new RemoveWaypointPayload(sender.getUuid()));
                    }
                });
            }
        });

        ServerPlayNetworking.registerGlobalReceiver(RemoveTeamWaypointPayload.ID, (payload, context) -> {
            ServerPlayerEntity sender = context.player();
            try (MinecraftServer server = context.server()) {
                server.execute(() -> ServerPlayNetworking.send(sender, new IsLoadedPayload()));
            }
        });
    }

    private static List<ServerPlayerEntity> getTeamPlayer(MinecraftServer server, ServerPlayerEntity player) {
        AbstractTeam team = player.getScoreboardTeam();
        return server.getPlayerManager().getPlayerList().stream().filter((playerEntity) -> playerEntity == player || playerEntity.getScoreboardTeam() == team).toList();
    }
}
