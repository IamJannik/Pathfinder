package net.bmjo.pathfinder.mixin.client.multikey;

import net.bmjo.pathfinder.multikey.MultiKeyBinding;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public class MixinClient {
    @Inject(method = "setScreen", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/option/KeyBinding;unpressAll()V"), cancellable = true)
    public void handleMultiKeyBindings(Screen screen, CallbackInfo ci) {
        MultiKeyBinding.unpressAll();
    }
}
