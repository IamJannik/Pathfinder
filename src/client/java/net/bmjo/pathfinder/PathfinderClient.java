package net.bmjo.pathfinder;

import net.bmjo.pathfinder.event.ClientEvents;
import net.bmjo.pathfinder.networking.ClientNetworking;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.UUID;

public class PathfinderClient implements ClientModInitializer {
	public static final String MOD_ID = "pathfinder";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	public static final HashSet<UUID> players = new HashSet<>();
	public static boolean is_loaded = false;
	public static boolean use_team = false;

	@Override
	public void onInitializeClient() {
		LOGGER.info("Build Client for " + MOD_ID);
		ClientEvents.register();
		ClientNetworking.register();
	}

	public static Identifier identifier(String name) {
		return new Identifier(MOD_ID, name);
	}

	public static ClientPlayerEntity getPlayer() {
		return MinecraftClient.getInstance().player;
	}
}