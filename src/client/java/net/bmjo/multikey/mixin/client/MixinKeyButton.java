package net.bmjo.multikey.mixin.client;

import net.bmjo.multikey.MultiKeyBinding;
import net.minecraft.client.gui.screen.option.ControlsListWidget;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ControlsListWidget.KeyBindingEntry.class)
public class MixinKeyButton {
    @Shadow
    @Final
    private Text bindingName;
    @Shadow
    @Final
    private KeyBinding binding;

    @Redirect(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/widget/ButtonWidget;builder(Lnet/minecraft/text/Text;Lnet/minecraft/client/gui/widget/ButtonWidget$PressAction;)Lnet/minecraft/client/gui/widget/ButtonWidget$Builder;", ordinal = 0))
    private ButtonWidget.Builder cantEdit(Text message, ButtonWidget.PressAction onPress) {
        if (this.binding instanceof MultiKeyBinding)
            return ButtonWidget.builder(this.bindingName, (button) -> {
            }).dimensions(0, 0, 75, 20).narrationSupplier((textSupplier) -> this.binding.isUnbound() ? Text.translatable("narrator.controls.unbound", bindingName) : Text.translatable("narrator.controls.bound", bindingName, textSupplier.get()));
        else
            return ButtonWidget.builder(bindingName, onPress).dimensions(0, 0, 75, 20).narrationSupplier((textSupplier) -> binding.isUnbound() ? Text.translatable("narrator.controls.unbound", bindingName) : Text.translatable("narrator.controls.bound", bindingName, textSupplier.get()));
    }
}
