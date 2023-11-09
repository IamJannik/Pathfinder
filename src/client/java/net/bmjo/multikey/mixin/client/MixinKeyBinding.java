package net.bmjo.multikey.mixin.client;

import net.bmjo.multikey.MultiKeyBinding;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(KeyBinding.class)
public abstract class MixinKeyBinding {
    @Inject(method = "onKeyPressed", at = @At(value = "HEAD"), cancellable = true)
    private static void onKeyPressed(InputUtil.Key key, CallbackInfo ci) {
        for (MultiKeyBinding multiKeyBinding : MultiKeyBinding.getMultiKeyBinding(key))
            if (multiKeyBinding.allPressed()) {
                multiKeyBinding.onPressed();
                ci.cancel();
            }
    }

    @Inject(method = "setKeyPressed", at = @At(value = "HEAD"), cancellable = true)
    private static void setKeyPressed(InputUtil.Key key, boolean pressed, CallbackInfo ci) {
        for (MultiKeyBinding multiKeyBinding : MultiKeyBinding.getMultiKeyBinding(key)) {
            if (pressed) {
                if (multiKeyBinding.allPressed()) {
                    multiKeyBinding.setPressed(true);
                    ci.cancel();
                }
            } else {
                multiKeyBinding.setPressed(false);
            }
        }
    }

    @Inject(method = "updatePressedStates", at = @At(value = "HEAD"))
    private static void updatePressedStates(CallbackInfo ci) {
        MultiKeyBinding.updatePressedStates();
    }

    @Inject(method = "unpressAll", at = @At(value = "HEAD"))
    private static void unpressAll(CallbackInfo ci) {
        MultiKeyBinding.unpressAll();
    }

    @Inject(method = "updateKeysByCode", at = @At(value = "HEAD"))
    private static void updateKeysByCode(CallbackInfo ci) {
        MultiKeyBinding.updateBindings();
    }
}
