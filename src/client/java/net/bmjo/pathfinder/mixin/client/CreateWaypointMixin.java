package net.bmjo.pathfinder.mixin.client;

import net.bmjo.pathfinder.PathfinderClient;
import net.bmjo.pathfinder.waypoint.WaypointHandler;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.GameOptions;
import net.minecraft.sound.SoundEvents;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public abstract class CreateWaypointMixin {
	@Shadow @Final public GameOptions options;

	@Inject(at = @At("HEAD"), method = "handleInputEvents", cancellable = true)
	private void run(CallbackInfo info) {
		while(this.options.useKey.wasPressed()) { //TODO
			if (WaypointHandler.createWaypoint()) {
				PathfinderClient.getPlayer().playSound(SoundEvents.BLOCK_AMETHYST_BLOCK_CHIME, 1.0F, 1.0F);
				info.cancel();
			}
		}
	}
}