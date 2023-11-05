package net.bmjo.pathfinder.mixin.client;

import net.bmjo.pathfinder.PathfinderClient;
import net.bmjo.pathfinder.waypoint.WaypointHandler;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.GameOptions;
import net.minecraft.entity.player.PlayerEntity;
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

	@Inject(at = @At("HEAD"), method = "handleInputEvents", cancellable = true) //TODO
	private void run(CallbackInfo ci) {
		while (this.options.pickItemKey.wasPressed()) {
			PlayerEntity player = PathfinderClient.getPlayer();
			if (player != null && player.isSneaking()) {
				boolean created = WaypointHandler.createWaypoint();
				if (created) {
					player.playSound(SoundEvents.BLOCK_AMETHYST_BLOCK_PLACE, 1.0F, 1.0F);
					ci.cancel();
				}
			}
		}
	}
}