package net.bmjo.pathfinder.mixin.client;

import net.bmjo.pathfinder.PathfinderClient;
import net.bmjo.pathfinder.waypoint.WaypointHandler;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.GameOptions;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public abstract class CreateWaypointMixin {
	@Shadow @Final public GameOptions options;

    @Inject(at = @At("HEAD"), method = "handleInputEvents") //TODO
	private void run(CallbackInfo ci) {
        PlayerEntity player = PathfinderClient.getPlayer();
        if (player != null && player.isSneaking()) {
            while (this.options.pickItemKey.wasPressed()) {
                WaypointHandler.createWaypoint();
			}
		}
	}
}