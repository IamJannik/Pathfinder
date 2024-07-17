package net.bmjo.pathfinder;

import net.bmjo.pathfinder.networking.ServerNetworking;
import net.fabricmc.api.ModInitializer;
import net.minecraft.util.Identifier;

/**
 * The main entry point for the Pathfinder mod on the server side.
 * Initializes server-specific components and registers networking.
 */
public class Pathfinder implements ModInitializer {
	public static final String MOD_ID = "pathfinder";

	@Override
	public void onInitialize() {
		ServerNetworking.register();
	}

	public static Identifier identifier(String name) {
		return Identifier.of(MOD_ID, name);
	}
}