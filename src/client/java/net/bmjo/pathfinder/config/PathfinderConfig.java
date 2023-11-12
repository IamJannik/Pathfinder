package net.bmjo.pathfinder.config;

import net.bmjo.pathfinder.PathfinderClient;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class PathfinderConfig {
    public static ClientConfig CONFIG;
    public static boolean USE_GANG;
    private static final String USE_GANG_KEY = "pathfinder.use_gang";

    public static void registerConfig() {
        CONFIG = new ClientConfig(PathfinderClient.MOD_ID + "config");
        assignConfigs();
    }

    private static void assignConfigs() {
        USE_GANG = CONFIG.getOrDefault(USE_GANG_KEY, true);
    }

    public static void toggleUseGang() {
        USE_GANG ^= true;
        CONFIG.set(USE_GANG_KEY, USE_GANG);
    }
}
