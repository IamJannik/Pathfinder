package net.bmjo.pathfinder.event;

import net.bmjo.pathfinder.PathfinderClient;
import net.bmjo.pathfinder.networking.ClientNetworking;
import net.bmjo.pathfinder.waypoint.WaypointHandler;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;

public class ClientEvents {
    public static void register() {
        ServerPlayConnectionEvents.JOIN.register((client, sender, server) -> sender.sendPacket(ClientNetworking.IS_LOADED, PacketByteBufs.create()));
        ServerPlayConnectionEvents.DISCONNECT.register((client, sender) -> PathfinderClient.is_loaded = false);
        ClientTickEvents.END_CLIENT_TICK.register((client) -> {
            if (System.currentTimeMillis() % 10 * 1000 == 0)
                WaypointHandler.update();
        });
        WorldRenderEvents.BEFORE_DEBUG_RENDER.register(WaypointHandler::render);
    }
}
