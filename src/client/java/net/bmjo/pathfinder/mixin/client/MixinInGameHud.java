package net.bmjo.pathfinder.mixin.client;

import net.bmjo.pathfinder.waypoint.WaypointModel;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.RenderTickCounter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin class for modifying the behavior of the Minecraft InGameHud.
 * Hooks into the rendering process to initiate waypoint rendering.
 */
@Mixin(InGameHud.class)
public class MixinInGameHud {
    @Inject(method = "render", at = @At("HEAD"))
    public void onRenderStart(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        WaypointModel.onRenderStart();
    }
}
