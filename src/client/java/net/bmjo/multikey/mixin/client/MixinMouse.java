package net.bmjo.multikey.mixin.client;

import net.bmjo.multikey.MultiKeyBinding;
import net.minecraft.client.Mouse;
import net.minecraft.client.util.InputUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Mouse.class)
public class MixinMouse {
    @Inject(method = "onMouseButton", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/option/KeyBinding;setKeyPressed(Lnet/minecraft/client/util/InputUtil$Key;Z)V"), cancellable = true)
    public void handleMultiKeyBindings(long window, int button, int action, int mods, CallbackInfo ci) {
        InputUtil.Key key = InputUtil.Type.MOUSE.createFromCode(button);
        for (MultiKeyBinding multiKeyBinding : MultiKeyBinding.getMultiKeyBinding(key)) {
            if (multiKeyBinding.allPressed()) {
                multiKeyBinding.setPressed(true);
                multiKeyBinding.onPressed();
                //multiKeyBinding.disableOtherBindings();
                ci.cancel();
            }
        }
    }

    @Inject(method = "lockCursor", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/option/KeyBinding;updatePressedStates()V"))
    public void updatePressedStates(CallbackInfo ci) {
        MultiKeyBinding.updatePressedStates();
    }
}
