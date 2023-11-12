package net.bmjo.pathfinder.util;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ButtonTextures;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class ToggleTexturedButton extends ButtonWidget {
    protected final ButtonTextures activeTextures;
    protected final ButtonTextures deactiveTextures;
    protected boolean active;

    public ToggleTexturedButton(int x, int y, int width, int height, boolean active, ButtonTextures activeTextures, ButtonTextures deactiveTextures, ButtonWidget.PressAction pressAction) {
        this(x, y, width, height, active, activeTextures, deactiveTextures, pressAction, ScreenTexts.EMPTY);
    }

    public ToggleTexturedButton(int x, int y, int width, int height, boolean active, ButtonTextures activeTextures, ButtonTextures deactiveTextures, ButtonWidget.PressAction pressAction, Text text) {
        super(x, y, width, height, text, pressAction, DEFAULT_NARRATION_SUPPLIER);
        this.activeTextures = activeTextures;
        this.deactiveTextures = deactiveTextures;
        this.active = active;
    }

    @Override
    public void onPress() {
        super.onPress();
        this.active ^= true;
    }

    public void renderButton(DrawContext context, int mouseX, int mouseY, float delta) {
        ButtonTextures textures = active ? activeTextures : deactiveTextures;
        Identifier identifier = textures.get(this.isNarratable(), this.isSelected());
        context.drawGuiTexture(identifier, this.getX(), this.getY(), this.width, this.height);
    }
}