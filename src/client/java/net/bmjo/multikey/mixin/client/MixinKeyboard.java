package net.bmjo.multikey.mixin.client;

import net.bmjo.multikey.MultiKeyBinding;
import net.minecraft.client.Keyboard;
import net.minecraft.client.util.InputUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Keyboard.class)
public class MixinKeyboard {
    @Inject(method = "onKey", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/util/InputUtil;fromKeyCode(II)Lnet/minecraft/client/util/InputUtil$Key;"), cancellable = true)
    public void handleMultiKeyBindings(long window, int k, int scancode, int action, int modifiers, CallbackInfo ci) {
        InputUtil.Key key = InputUtil.fromKeyCode(k, scancode);
        for (MultiKeyBinding multiKeyBinding : MultiKeyBinding.getMultiKeyBinding(key)) {
            if (multiKeyBinding.allPressed()) {
                multiKeyBinding.setPressed(true);
                multiKeyBinding.onPressed();
                //multiKeyBinding.disableOtherBindings();
                ci.cancel(); // only first
            }
        }
    }

}




