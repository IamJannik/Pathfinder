package net.bmjo.pathfinder;

import net.bmjo.pathfinder.networking.ServerNetworking;
import net.fabricmc.api.ModInitializer;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Pathfinder implements ModInitializer {
	public static final String MOD_ID = "pathfinder";
    public static final Logger LOGGER = LoggerFactory.getLogger("pathfinder");

	@Override
	public void onInitialize() {
		LOGGER.info("Build " + MOD_ID);
		ServerNetworking.register();
	}

	public static Identifier identifier(String name) {
		return new Identifier(MOD_ID, name);
	}
}