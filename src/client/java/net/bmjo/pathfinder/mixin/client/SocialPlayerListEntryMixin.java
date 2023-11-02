package net.bmjo.pathfinder.mixin.client;

import net.bmjo.pathfinder.PathfinderClient;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ButtonTextures;
import net.minecraft.client.gui.screen.multiplayer.SocialInteractionsPlayerListEntry;
import net.minecraft.client.gui.screen.multiplayer.SocialInteractionsScreen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.TexturedButtonWidget;
import net.minecraft.client.util.SkinTextures;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

@Mixin(SocialInteractionsPlayerListEntry.class)
public abstract class SocialPlayerListEntryMixin {
    @Unique
    private static final ButtonTextures ADD_TEAM_TEXTURE = new ButtonTextures(new Identifier("social_interactions/mute_button"), new Identifier("social_interactions/mute_button_highlighted"));
    @Unique
    private static final ButtonTextures REMOVE_TEAM_TEXTURE = new ButtonTextures(new Identifier("social_interactions/mute_button"), new Identifier("social_interactions/mute_button_highlighted"));

    @Shadow @Final private List<ClickableWidget> buttons;
    @Unique @Nullable
    private ButtonWidget addTeamButton, removeTeamButton;
    @Unique private static final Text addTeamText, removeTeamText;

    @Inject(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/multiplayer/SocialInteractionsPlayerListEntry;setShowButtonVisible(Z)V"))
    public void addTeamButton(MinecraftClient client, SocialInteractionsScreen parent, UUID uuid, String name, Supplier<SkinTextures> skinTexture, boolean reportable, CallbackInfo ci) {
        this.addTeamButton = new TexturedButtonWidget(0, 0, 20, 20, ADD_TEAM_TEXTURE, (button) -> PathfinderClient.players.add(uuid),addTeamText);
        this.addTeamButton.setTooltip(Tooltip.of(addTeamText));
        this.addTeamButton.setTooltipDelay(10);
        this.buttons.add(this.addTeamButton);
        this.removeTeamButton = new TexturedButtonWidget(0, 0, 20, 20, REMOVE_TEAM_TEXTURE, (button) -> PathfinderClient.players.remove(uuid),removeTeamText);
        this.removeTeamButton.setTooltip(Tooltip.of(removeTeamText));
        this.removeTeamButton.setTooltipDelay(10);
        this.buttons.add(this.removeTeamButton);


        this.setTeamVisible(PathfinderClient.players.contains(uuid));
    }

    @Inject(method = "render", at = @At(value = "TAIL"))
    public void renderTeamButton(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta, CallbackInfo ci) {
        if (this.addTeamButton != null && this.removeTeamButton != null) {
            this.addTeamButton.setX(x + (entryWidth - this.addTeamButton.getWidth() - 8) - 40 - 4);
            this.addTeamButton.setY(y + (entryHeight - this.addTeamButton.getHeight()) / 2);
            this.addTeamButton.render(context, mouseX, mouseY, tickDelta);
            this.removeTeamButton.setX(x + (entryWidth - this.removeTeamButton.getWidth() - 8) - 40 - 4);
            this.removeTeamButton.setY(y + (entryHeight - this.removeTeamButton.getHeight()) / 2);
            this.removeTeamButton.render(context, mouseX, mouseY, tickDelta);
        }
    }

    @Unique
    private void setTeamVisible(boolean isInTeam) {
        if (this.addTeamButton != null)
            this.addTeamButton.visible = !isInTeam;
        if (this.removeTeamButton != null)
            this.removeTeamButton.visible = isInTeam;
        this.buttons.set(3, isInTeam ? this.removeTeamButton : this.addTeamButton);
    }

    static {
        addTeamText = Text.translatable("pathfinder.team.add");
        removeTeamText = Text.translatable("pathfinder.team.remove");
    }
}
