package net.bmjo.pathfinder.event;

import net.bmjo.pathfinder.waypoint.Waypoints;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;

public class ClientEvents {
    public static void register() {
        ClientTickEvents.END_CLIENT_TICK.register((client) -> {
            if (System.currentTimeMillis() % 10000 == 0)
                Waypoints.update();
        });
        WorldRenderEvents.END.register(Waypoints::render);
    }
}
