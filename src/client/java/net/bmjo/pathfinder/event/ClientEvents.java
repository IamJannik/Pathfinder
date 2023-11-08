package net.bmjo.pathfinder.event;

import net.bmjo.pathfinder.PathfinderClient;
import net.bmjo.pathfinder.networking.ClientNetworking;
import net.bmjo.pathfinder.waypoint.WaypointHandler;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;

public class ClientEvents {
    //public static final KeyBinding keyBinding;
    public static void register() {
        ServerPlayConnectionEvents.JOIN.register((client, sender, server) -> sender.sendPacket(ClientNetworking.IS_LOADED, PacketByteBufs.create()));
        ServerPlayConnectionEvents.DISCONNECT.register((client, sender) -> PathfinderClient.is_loaded = false);
        ClientTickEvents.END_CLIENT_TICK.register((client) -> {
            WaypointHandler.update();
            //while (keyBinding.wasPressed())
            //    WaypointHandler.createWaypoint();
        });
    }

    /*
    static {
        keyBinding = KeyBindingHelper.registerKeyBinding(new MultiKeyBinding(
                "key.pathfinder.waypoint",
                "category.pathfinder",
                InputUtil.Type.KEYSYM.createFromCode(GLFW.GLFW_KEY_P),
                InputUtil.Type.KEYSYM.createFromCode(GLFW.GLFW_KEY_LEFT_SHIFT)
        ));
    }
     */
}
