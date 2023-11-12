package net.bmjo.pathfinder;

import net.bmjo.pathfinder.networking.ServerNetworking;
import net.fabricmc.api.ModInitializer;
import net.minecraft.util.Identifier;

public class Pathfinder implements ModInitializer {
	public static final String MOD_ID = "pathfinder";

	@Override
	public void onInitialize() {
		ServerNetworking.register();
	}

	public static Identifier identifier(String name) {
		return new Identifier(MOD_ID, name);
	}
}