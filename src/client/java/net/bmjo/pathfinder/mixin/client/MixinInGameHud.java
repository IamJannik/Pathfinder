package net.bmjo.pathfinder.mixin.client;

import net.bmjo.pathfinder.waypoint.WaypointModel;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public class MixinInGameHud {
    @Inject(method = "render", at = @At("HEAD"))
    public void onRenderStart(DrawContext guiGraphics, float deltaTicks, CallbackInfo info) {
        WaypointModel.onRenderStart();
    }
}
