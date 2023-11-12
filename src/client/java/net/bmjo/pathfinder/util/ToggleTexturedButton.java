package net.bmjo.pathfinder.util;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ButtonTextures;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.util.Identifier;

/**
 * A button widget with toggleable textures that switch when clicked.
 * The button can have different textures for active and inactive states.
 *
 * @author BMJO
 * @version 1.1
 */
public class ToggleTexturedButton extends ButtonWidget {
    /**
     * The textures to be used when the button is in the active state.
     */
    protected final ButtonTextures activeTextures;
    /**
     * The textures to be used when the button is in the inactive state.
     */
    protected final ButtonTextures deactivateTextures;
    /** Indicates whether the button is currently in the active state. */
    protected boolean active;

    /**
     * Creates a new ToggleTexturedButton with custom text.
     *
     * @param x                  The x-coordinate of the button.
     * @param y                  The y-coordinate of the button.
     * @param width              The width of the button.
     * @param height             The height of the button.
     * @param active             The initial state of the button (active or inactive).
     * @param activeTextures     The textures for the button when it is in the active state.
     * @param deactivateTextures The textures for the button when it is in the inactive state.
     * @param pressAction        The action to be performed when the button is pressed.
     */
    public ToggleTexturedButton(int x, int y, int width, int height, boolean active, ButtonTextures activeTextures, ButtonTextures deactivateTextures, ButtonWidget.PressAction pressAction) {
        super(x, y, width, height, ScreenTexts.EMPTY, pressAction, DEFAULT_NARRATION_SUPPLIER);
        this.activeTextures = activeTextures;
        this.deactivateTextures = deactivateTextures;
        this.active = active;
    }

    /**
     * Toggles the state of the button when pressed.
     */
    @Override
    public void onPress() {
        super.onPress();
        this.active ^= true;
    }

    /**
     * Renders the button with the appropriate textures based on its current state.
     *
     * @param context The drawing context.
     * @param mouseX  The x-coordinate of the mouse.
     * @param mouseY  The y-coordinate of the mouse.
     * @param delta   The time passed since the last frame.
     */
    public void renderButton(DrawContext context, int mouseX, int mouseY, float delta) {
        ButtonTextures textures = active ? activeTextures : deactivateTextures;
        Identifier identifier = textures.get(this.isNarratable(), this.isSelected());
        context.drawGuiTexture(identifier, this.getX(), this.getY(), this.width, this.height);
    }
}