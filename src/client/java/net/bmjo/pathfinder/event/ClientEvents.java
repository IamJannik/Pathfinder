package net.bmjo.pathfinder.event;

import net.bmjo.multikey.MultiKeyBinding;
import net.bmjo.pathfinder.PathfinderClient;
import net.bmjo.pathfinder.waypoint.WaypointHandler;
import net.bmjo.pathfinder.waypoint.WaypointRenderer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

/**
 * Client-side events of Pathfinder.
 */
public class ClientEvents {
    private static final KeyBinding waypointKey;

    public static void registerEvents() {
        //ServerPlayConnectionEvents.JOIN.register((client, sender, server) -> ClientPlayNetworking.send(new ServerNetworking.IsLoadedPayload()));
        ServerPlayConnectionEvents.DISCONNECT.register((client, sender) -> PathfinderClient.is_loaded = false);
        //HudRenderCallback.EVENT.register((drawContext, tickDelta) -> WaypointHUDRenderer.getInstance().render(drawContext, tickDelta));
        WorldRenderEvents.END.register((context) -> WaypointRenderer.getInstance().render());

        ClientTickEvents.START_CLIENT_TICK.register((client) -> {
            while (waypointKey.wasPressed())
                WaypointHandler.createWaypoint();
        });
        ClientTickEvents.END_CLIENT_TICK.register((client) -> WaypointHandler.update());
    }

    static {
        waypointKey = KeyBindingHelper.registerKeyBinding(new MultiKeyBinding(
                "key.pathfinder.waypoint",
                "category.pathfinder",
                InputUtil.Type.MOUSE.createFromCode(GLFW.GLFW_MOUSE_BUTTON_MIDDLE)
        ));
    }
}
