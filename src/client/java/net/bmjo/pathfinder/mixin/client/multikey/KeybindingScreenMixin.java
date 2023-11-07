package net.bmjo.pathfinder.mixin.client.multikey;

import net.bmjo.pathfinder.multikey.MultiKeyBinding;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.option.ControlsListWidget;
import net.minecraft.client.gui.screen.option.GameOptionsScreen;
import net.minecraft.client.gui.screen.option.KeybindsScreen;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import net.minecraft.util.Pair;
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
import java.util.stream.Collectors;

@Mixin(KeybindsScreen.class)
public abstract class KeybindingScreenMixin extends GameOptionsScreen {
    @Shadow
    @Nullable
    public KeyBinding selectedKeyBinding;

    @Shadow
    public long lastKeyCodeUpdateTime;

    @Shadow
    private ControlsListWidget controlsList;

    @Unique
    private final Set<Pair<Integer, Integer>> pressedKeys = new HashSet<>();

    public KeybindingScreenMixin(Screen parent, GameOptions gameOptions, Text title) {
        super(parent, gameOptions, title);
    }


    @Inject(method = "keyPressed", at = @At(value = "HEAD"), cancellable = true)
    public void onlySuper(int keyCode, int scanCode, int modifiers, CallbackInfoReturnable<Boolean> cir) {
        if (this.selectedKeyBinding instanceof MultiKeyBinding) {
            if (keyCode != 256) {
                pressedKeys.add(new Pair<>(keyCode, scanCode));
                cir.setReturnValue(super.keyPressed(keyCode, scanCode, modifiers));
            }
        }
    }

    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        if (this.selectedKeyBinding instanceof MultiKeyBinding multiKeyBinding && !this.pressedKeys.isEmpty()) {
            multiKeyBinding.setBoundKeys(this.pressedKeys.stream().map(pair -> InputUtil.fromKeyCode(pair.getLeft(), pair.getRight())).collect(Collectors.toSet()));

            this.selectedKeyBinding = null;
            this.lastKeyCodeUpdateTime = Util.getMeasuringTimeMs();
            this.controlsList.update();
            MultiKeyBinding.updateBindings();
            this.pressedKeys.clear();
            return true;
        }
        return super.keyReleased(keyCode, scanCode, modifiers);
    }
}
