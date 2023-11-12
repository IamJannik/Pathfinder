package net.bmjo.pathfinder.config;

import net.bmjo.pathfinder.PathfinderClient;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

/**
 * Config for Pathfinder client-side settings.
 *
 * @author BMJO
 * @version 1.0
 */
@Environment(EnvType.CLIENT)
public class PathfinderConfig {
    private static ClientConfig CONFIG;
    /**
     * Boolean flag indicating whether the "use gang" setting is enabled.
     */
    public static boolean USE_GANG;
    private static final String USE_GANG_KEY = "pathfinder.use_gang";

    /**
     * Registers the client-side config and initializes configuration values.
     */
    public static void registerConfig() {
        CONFIG = new ClientConfig(PathfinderClient.MOD_ID + "config");
        assignConfigs();
    }

    private static void assignConfigs() {
        USE_GANG = CONFIG.getOrDefault(USE_GANG_KEY, true);
    }

    /**
     * Toggles the "Use Gang" setting and updates the configuration.
     */
    public static void toggleUseGang() {
        USE_GANG ^= true;
        CONFIG.set(USE_GANG_KEY, USE_GANG);
    }
}
