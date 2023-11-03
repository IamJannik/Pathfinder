package net.bmjo.pathfinder.mixin.client;

import net.bmjo.pathfinder.PathfinderClient;
import net.bmjo.pathfinder.gang.GangHandler;
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
    private static final ButtonTextures SINGLE_PERSON_TEXTURE = new ButtonTextures(PathfinderClient.identifier("single_person"), PathfinderClient.identifier("single_person_highlighted"));
    @Unique
    private static final ButtonTextures GANG_MEMBER_TEXTURE = new ButtonTextures(PathfinderClient.identifier("gang_member"), PathfinderClient.identifier("gang_member_highlighted"));

    @Shadow @Final private List<ClickableWidget> buttons;
    @Unique @Nullable
    private ButtonWidget addPlayerButton, removePlayerButton;
    @Unique private static final Text addPlayerText, removePlayerText;

    @Inject(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/multiplayer/SocialInteractionsPlayerListEntry;setShowButtonVisible(Z)V"))
    public void addGangButton(MinecraftClient client, SocialInteractionsScreen parent, UUID uuid, String name, Supplier<SkinTextures> skinTexture, boolean reportable, CallbackInfo ci) {
        this.addPlayerButton = new TexturedButtonWidget(0, 0, 20, 20, SINGLE_PERSON_TEXTURE, (button) -> {
            GangHandler.addMember(uuid);
            this.setGangVisible(true);
        }, addPlayerText);
        this.addPlayerButton.setTooltip(Tooltip.of(addPlayerText));
        this.addPlayerButton.setTooltipDelay(10);
        this.buttons.add(this.addPlayerButton);
        this.removePlayerButton = new TexturedButtonWidget(0, 0, 20, 20, GANG_MEMBER_TEXTURE, (button) -> {
            GangHandler.removeMember(uuid);
            this.setGangVisible(false);
        }, removePlayerText);
        this.removePlayerButton.setTooltip(Tooltip.of(removePlayerText));
        this.removePlayerButton.setTooltipDelay(10);
        this.buttons.add(this.removePlayerButton);

        this.setGangVisible(GangHandler.isMember(uuid));
    }

    @Inject(method = "render", at = @At(value = "TAIL"))
    public void renderGangButton(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta, CallbackInfo ci) {
        if (this.addPlayerButton != null && this.removePlayerButton != null) {
            this.addPlayerButton.setX(x + (entryWidth - this.addPlayerButton.getWidth() - 8) - 40 - 4);
            this.addPlayerButton.setY(y + (entryHeight - this.addPlayerButton.getHeight()) / 2);
            this.addPlayerButton.render(context, mouseX, mouseY, tickDelta);
            this.removePlayerButton.setX(x + (entryWidth - this.removePlayerButton.getWidth() - 8) - 40 - 4);
            this.removePlayerButton.setY(y + (entryHeight - this.removePlayerButton.getHeight()) / 2);
            this.removePlayerButton.render(context, mouseX, mouseY, tickDelta);
        }
    }

    @Unique
    private void setGangVisible(boolean isInGang) {
        if (this.addPlayerButton != null)
            this.addPlayerButton.visible = !isInGang;
        if (this.removePlayerButton != null)
            this.removePlayerButton.visible = isInGang;
        this.buttons.set(3, isInGang ? this.removePlayerButton : this.addPlayerButton);
    }

    static {
        addPlayerText = Text.translatable("pathfinder.gang.add");
        removePlayerText = Text.translatable("pathfinder.gang.remove");
    }
}
