package net.bmjo.pathfinder.mixin.client;

import net.bmjo.pathfinder.PathfinderClient;
import net.bmjo.pathfinder.gang.GangHandler;
import net.bmjo.pathfinder.waypoint.WaypointHandler;
import net.minecraft.client.gui.screen.ButtonTextures;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.multiplayer.SocialInteractionsPlayerListWidget;
import net.minecraft.client.gui.screen.multiplayer.SocialInteractionsScreen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TexturedButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Collection;
import java.util.UUID;

@Mixin(SocialInteractionsScreen.class)
public abstract class SocialScreenMixin extends Screen {
    @Shadow SocialInteractionsPlayerListWidget playerList;

    @Shadow protected abstract void setCurrentTab(SocialInteractionsScreen.Tab currentTab);
    @Unique private static final ButtonTextures USE_GANG_TEXTURE = new ButtonTextures(PathfinderClient.identifier("use_gang"), PathfinderClient.identifier("use_gang_highlighted"));
    @Unique private static final ButtonTextures USE_TEAM_TEXTURE = new ButtonTextures(PathfinderClient.identifier("use_team"), PathfinderClient.identifier("use_team_highlighted"));
    @Unique private static final Text gangTabTitle, selectedGangTabTitle, useGangText, useTeamText;
    @Unique
    private ButtonWidget gangTabButton;
    @Unique @Nullable
    private ButtonWidget useTeamButton, useGangButton;
    @Unique private boolean selected = false;

    protected SocialScreenMixin(Text title) {
        super(title);
    }

    @Inject(method = "init", at = @At(value = "TAIL"))
    public void addConfigButton(CallbackInfo ci) {
        this.useGangButton = this.addDrawableChild(new TexturedButtonWidget(this.width - 24, this.height - 22, 20, 20, USE_GANG_TEXTURE, (button) -> {
            this.setGangVisible(false);
            WaypointHandler.onlyTeam();
        }, useGangText));
        this.useGangButton.setTooltip(Tooltip.of(useTeamText));
        this.useGangButton.setTooltipDelay(10);
        this.useTeamButton = this.addDrawableChild(new TexturedButtonWidget(this.width - 24, this.height - 22, 20, 20, USE_TEAM_TEXTURE, (button) -> {
            this.setGangVisible(true);
            WaypointHandler.onlyGang();
        }, useTeamText));
        this.useTeamButton.setTooltip(Tooltip.of(useGangText));
        this.useTeamButton.setTooltipDelay(10);

        this.setGangVisible(PathfinderClient.use_gang);
    }

    @Redirect(method = "init", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/multiplayer/SocialInteractionsPlayerListWidget;getRowWidth()I"))
    public int fourButtons(SocialInteractionsPlayerListWidget widget) {
        int width = widget.getRowWidth() / 4;
        int left = widget.getRowLeft();
        this.gangTabButton = this.addDrawableChild(ButtonWidget.builder(gangTabTitle, (button) -> this.setCurrentTab(null)).dimensions(left + width, 45, width, 20).build());
        return width * 3;
    }

    @Redirect(method = "init", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/widget/ButtonWidget$Builder;dimensions(IIII)Lnet/minecraft/client/gui/widget/ButtonWidget$Builder;", ordinal = 1))
    public ButtonWidget.Builder notMiddle(ButtonWidget.Builder builder, int x, int y, int width, int height) {
        return builder.dimensions(x + width / 2, y, width, height);
    }

    @Inject(method = "setCurrentTab", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/widget/ButtonWidget;setMessage(Lnet/minecraft/text/Text;)V", ordinal = 2, shift = At.Shift.AFTER), cancellable = true)
    public void setGangTab(SocialInteractionsScreen.Tab currentTab, CallbackInfo ci) {
        this.gangTabButton.setMessage(gangTabTitle);
        if (currentTab == null) {
            this.selected ^= this.selected;
            this.gangTabButton.setMessage(selectedGangTabTitle);
            Collection<UUID> collection = GangHandler.members();
            this.playerList.update(collection, this.playerList.getScrollAmount(), false);
            ci.cancel();
        }
    }

    @Unique
    private void setGangVisible(boolean useGang) {
        PathfinderClient.use_gang = useGang;
        if (this.useGangButton != null)
            this.useGangButton.visible = useGang;
        if (this.useTeamButton != null)
            this.useTeamButton.visible = !useGang;
    }

    static {
        gangTabTitle = Text.translatable("pathfinder.team.gang_tab");
        selectedGangTabTitle = gangTabTitle.copyContentOnly().formatted(Formatting.UNDERLINE);
        useGangText = Text.translatable("pathfinder.gang.use_gang");
        useTeamText = Text.translatable("pathfinder.gang.use_team");
    }
}
