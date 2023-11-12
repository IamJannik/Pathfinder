package net.bmjo.pathfinder.mixin.client;

import net.bmjo.pathfinder.PathfinderClient;
import net.bmjo.pathfinder.gang.GangHandler;
import net.bmjo.pathfinder.util.ToggleTexturedButton;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ButtonTextures;
import net.minecraft.client.gui.screen.multiplayer.SocialInteractionsPlayerListEntry;
import net.minecraft.client.gui.screen.multiplayer.SocialInteractionsScreen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
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
public abstract class MixinSocialPlayerListEntry {
    @Unique
    private static final ButtonTextures SINGLE_PERSON_TEXTURE = new ButtonTextures(PathfinderClient.identifier("single_person"), PathfinderClient.identifier("single_person_highlighted"));
    @Unique
    private static final ButtonTextures GANG_MEMBER_TEXTURE = new ButtonTextures(PathfinderClient.identifier("gang_member"), PathfinderClient.identifier("gang_member_highlighted"));

    @Shadow @Final private List<ClickableWidget> buttons;
    @Unique @Nullable
    private ButtonWidget gangButton;
    @Unique private static final Text addPlayerText, removePlayerText;

    @Inject(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/multiplayer/SocialInteractionsPlayerListEntry;setShowButtonVisible(Z)V"))
    public void addGangButton(MinecraftClient client, SocialInteractionsScreen parent, UUID uuid, String name, Supplier<SkinTextures> skinTexture, boolean reportable, CallbackInfo ci) {
        this.gangButton = new ToggleTexturedButton(0, 0, 20, 20, GangHandler.isMember(uuid), GANG_MEMBER_TEXTURE, SINGLE_PERSON_TEXTURE, (button) -> this.changeGangVisible(uuid));
        this.gangButton.setTooltip(Tooltip.of(GangHandler.isMember(uuid) ? removePlayerText : addPlayerText));
        this.gangButton.setTooltipDelay(10);
        this.buttons.add(this.gangButton);
    }

    @Inject(method = "render", at = @At(value = "TAIL"))
    public void renderGangButton(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta, CallbackInfo ci) {
        if (this.gangButton != null) {
            this.gangButton.setX(x + (entryWidth - this.gangButton.getWidth() - 8) - 40 - 4);
            this.gangButton.setY(y + (entryHeight - this.gangButton.getHeight()) / 2);
            this.gangButton.render(context, mouseX, mouseY, tickDelta);
        }
    }

    @Unique
    private void changeGangVisible(UUID uuid) {
        assert this.gangButton != null;
        if (GangHandler.isMember(uuid)) {
            GangHandler.removeMember(uuid);
            this.gangButton.setTooltip(Tooltip.of(addPlayerText));
        } else {
            GangHandler.addMember(uuid);
            this.gangButton.setTooltip(Tooltip.of(removePlayerText));
        }
    }

    static {
        addPlayerText = Text.translatable("pathfinder.gang.add");
        removePlayerText = Text.translatable("pathfinder.gang.remove");
    }
}
