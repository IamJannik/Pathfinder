package net.bmjo.pathfinder.mixin.client;

import net.bmjo.pathfinder.waypoint.Waypoints;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.option.GameOptions;
import net.minecraft.entity.Entity;
import net.minecraft.util.hit.HitResult;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public abstract class CreateWaypointMixin {
	@Shadow @Final public GameOptions options;

	@Inject(at = @At("HEAD"), method = "handleInputEvents", cancellable = true)
	private void run(CallbackInfo info) {
		while(this.options.useKey.wasPressed()) { //TODO
			if (Waypoints.createWaypoint())
				info.cancel();
		}
	}
}