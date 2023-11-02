package net.bmjo.pathfinder.team;

import net.minecraft.entity.player.PlayerEntity;

import java.util.HashSet;

public class Team {
    private static final HashSet<PlayerEntity> players = new HashSet<>();
    public static HashSet<PlayerEntity> players() {
        return players;
    }

    public static void addPlayer(PlayerEntity player) {
        players.add(player);
    }
}
