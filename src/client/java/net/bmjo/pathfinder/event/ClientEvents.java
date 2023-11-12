package net.bmjo.pathfinder.event;

import net.bmjo.multikey.MultiKeyBinding;
import net.bmjo.pathfinder.PathfinderClient;
import net.bmjo.pathfinder.networking.ClientNetworking;
import net.bmjo.pathfinder.waypoint.WaypointHandler;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public class ClientEvents {
    public static final KeyBinding waypointKey;

    public static void registerEvents() {
        ServerPlayConnectionEvents.JOIN.register((client, sender, server) -> sender.sendPacket(ClientNetworking.IS_LOADED, PacketByteBufs.create()));
        ServerPlayConnectionEvents.DISCONNECT.register((client, sender) -> PathfinderClient.is_loaded = false);
        ClientTickEvents.END_CLIENT_TICK.register((client) -> {
            WaypointHandler.update();
            while (waypointKey.wasPressed())
                WaypointHandler.createWaypoint();
        });
    }

    static {
        waypointKey = new MultiKeyBinding(
                "key.pathfinder.waypoint",
                "category.pathfinder",
                true,
                InputUtil.Type.MOUSE.createFromCode(GLFW.GLFW_MOUSE_BUTTON_MIDDLE)
        );
    }
}
