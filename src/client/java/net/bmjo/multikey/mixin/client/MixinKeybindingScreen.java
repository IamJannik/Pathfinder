package net.bmjo.multikey.mixin.client;

import net.bmjo.multikey.MultiKeyBinding;
import net.bmjo.multikey.annotation.Unfinished;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.option.ControlsListWidget;
import net.minecraft.client.gui.screen.option.GameOptionsScreen;
import net.minecraft.client.gui.screen.option.KeybindsScreen;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import net.minecraft.util.Util;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.HashSet;
import java.util.Set;

@Unfinished
@Mixin(KeybindsScreen.class)
public abstract class MixinKeybindingScreen extends GameOptionsScreen {
    @Shadow
    @Nullable
    public KeyBinding selectedKeyBinding;

    @Shadow
    public long lastKeyCodeUpdateTime;

    @Shadow
    private ControlsListWidget controlsList;

    @Shadow
    public abstract boolean keyPressed(int keyCode, int scanCode, int modifiers);

    @Unique
    private final Set<InputUtil.Key> pressedKeys = new HashSet<>();

    public MixinKeybindingScreen(Screen parent, GameOptions gameOptions, Text title) {
        super(parent, gameOptions, title);
    }


    @Inject(method = "keyPressed", at = @At(value = "HEAD"), cancellable = true)
    public void isMultiKeyBinding(int keyCode, int scanCode, int modifiers, CallbackInfoReturnable<Boolean> cir) {
        if (this.selectedKeyBinding instanceof MultiKeyBinding) {
            if (keyCode != 256) {
                pressedKeys.add(InputUtil.fromKeyCode(keyCode, scanCode));
                cir.setReturnValue(super.keyPressed(keyCode, scanCode, modifiers));
            }
        }
    }

    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        return createMultiKeyBinding() || super.keyReleased(keyCode, scanCode, modifiers);
    }

    @Inject(method = "mouseClicked", at = @At(value = "HEAD"), cancellable = true)
    public void isMultiKeyBinding(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> cir) {
        if (this.selectedKeyBinding instanceof MultiKeyBinding) {
            pressedKeys.add(InputUtil.Type.MOUSE.createFromCode(button));
            cir.setReturnValue(super.mouseClicked(mouseX, mouseY, button));
        }
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        return createMultiKeyBinding() || super.mouseReleased(mouseX, mouseY, button);
    }

    @Unique
    private boolean createMultiKeyBinding() {
        if (this.selectedKeyBinding != null && this.selectedKeyBinding instanceof MultiKeyBinding multiKeyBinding && !this.pressedKeys.isEmpty()) {
            multiKeyBinding.setBoundKeys(this.pressedKeys);

            this.selectedKeyBinding = null;
            this.lastKeyCodeUpdateTime = Util.getMeasuringTimeMs();
            this.controlsList.update();
            MultiKeyBinding.updateBindings();
            this.pressedKeys.clear();
            return true;
        }
        return false;
    }
}
