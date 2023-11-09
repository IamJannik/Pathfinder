package net.bmjo.multikey.mixin.client;

import net.bmjo.multikey.MultiKeyBinding;
import net.minecraft.client.gui.screen.option.ControlsListWidget;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
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

    @Redirect(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/widget/ButtonWidget$Builder;build()Lnet/minecraft/client/gui/widget/ButtonWidget;", ordinal = 0))
    private ButtonWidget cantEdit(ButtonWidget.Builder builder) {
        if (this.binding instanceof MultiKeyBinding)
            return ButtonWidget.builder(this.bindingName, (button) -> {
            }).tooltip(Tooltip.of(Text.literal("Not yet possible to edit.").formatted(Formatting.ITALIC))).dimensions(0, 0, 75, 20).narrationSupplier((textSupplier) -> this.binding.isUnbound() ? Text.translatable("narrator.controls.unbound", bindingName) : Text.translatable("narrator.controls.bound", bindingName, textSupplier.get())).build();
        else
            return builder.build();
    }

    @Redirect(method = "update", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/widget/ButtonWidget;setTooltip(Lnet/minecraft/client/gui/tooltip/Tooltip;)V", ordinal = 1))
    private void setTooltip(ButtonWidget buttonWidget, Tooltip tooltip) {
        if (this.binding instanceof MultiKeyBinding)
            buttonWidget.setTooltip(Tooltip.of(Text.literal("Not yet possible to edit.").formatted(Formatting.ITALIC)));
    }
}
