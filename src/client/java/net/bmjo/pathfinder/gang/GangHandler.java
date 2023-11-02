package net.bmjo.pathfinder.gang;

import net.bmjo.pathfinder.PathfinderClient;
import net.bmjo.pathfinder.networking.ClientNetworking;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.network.PacketByteBuf;

import java.util.HashSet;
import java.util.UUID;

public class GangHandler {
    public static final HashSet<UUID> members = new HashSet<>();

    public static void addMember(UUID member) {
        members.add(member);
    }

    public static void removeMember(UUID member) {
        members.remove(member);
    }

    public static boolean isMember(UUID member) {
        return members.contains(member);
    }

    public static void joinGang(UUID member) {
        addMember(member);
        sendJoin(member);
    }

    private static void sendJoin(UUID member) {
        if (PathfinderClient.is_loaded) {
            PacketByteBuf buffer = PacketByteBufs.create();
            buffer.writeUuid(member);
            ClientPlayNetworking.send(ClientNetworking.ADD_PLAYER, buffer);
        } else {
            PlayerListEntry playerEntry = PathfinderClient.getPlayer().networkHandler.getPlayerListEntry(member);
            if (playerEntry != null)
                sendJoinMessage(playerEntry.getProfile().getName());
        }
    }

    private static void sendJoinMessage(String player) {
        PathfinderClient.getPlayer().networkHandler.sendChatCommand(String.format("msg %s I added you to my gang.", player));
    }

    public static void leaveGang(UUID member) {
        removeMember(member);
        sendLeave(member);
    }

    private static void sendLeave(UUID member) {
        if (PathfinderClient.is_loaded) {
            PacketByteBuf buffer = PacketByteBufs.create();
            buffer.writeUuid(member);
            ClientPlayNetworking.send(ClientNetworking.REMOVE_PLAYER, buffer);
        } else {
            PlayerListEntry playerEntry = PathfinderClient.getPlayer().networkHandler.getPlayerListEntry(member);
            if (playerEntry != null)
                sendKickMessage(playerEntry.getProfile().getName());
        }
    }

    private static void sendKickMessage(String player) {
        PathfinderClient.getPlayer().networkHandler.sendChatCommand(String.format("msg %s I kicked you out of my gang.", player));
    }
}
