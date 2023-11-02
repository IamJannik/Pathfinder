package net.bmjo.pathfinder.mixin.client;

import net.bmjo.pathfinder.PathfinderClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.multiplayer.SocialInteractionsPlayerListWidget;
import net.minecraft.client.gui.screen.multiplayer.SocialInteractionsScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
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
    @Unique private static final Text TAB_TEAM_TITLE, SELECTED_TAB_TEAM_TITLE;
    @Unique
    private ButtonWidget teamTabButton;

    @Unique private boolean selected = false;

    protected SocialScreenMixin(Text title) {
        super(title);
    }

    @Redirect(method = "init", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/multiplayer/SocialInteractionsPlayerListWidget;getRowWidth()I"))
    public int fourButtons(SocialInteractionsPlayerListWidget widget) {
        int width = widget.getRowWidth() / 4;
        int left = widget.getRowLeft();
        this.teamTabButton = this.addDrawableChild(ButtonWidget.builder(TAB_TEAM_TITLE, (button) -> this.setCurrentTab(null)).dimensions(left + width, 45, width, 20).build());
        return width * 3;
    }

    @Redirect(method = "init", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/widget/ButtonWidget$Builder;dimensions(IIII)Lnet/minecraft/client/gui/widget/ButtonWidget$Builder;", ordinal = 1))
    public ButtonWidget.Builder notMiddle(ButtonWidget.Builder builder, int x, int y, int width, int height) {
        return builder.dimensions(x + width / 2, y, width, height);
    }

    @Inject(method = "setCurrentTab", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/widget/ButtonWidget;setMessage(Lnet/minecraft/text/Text;)V", ordinal = 2, shift = At.Shift.AFTER), cancellable = true)
    public void setTeamTab(SocialInteractionsScreen.Tab currentTab, CallbackInfo ci) {
        if (currentTab == null) {
            this.selected ^= this.selected;
            this.teamTabButton.setMessage(SELECTED_TAB_TEAM_TITLE);
            Collection<UUID> collection = PathfinderClient.players;
            this.playerList.update(collection, this.playerList.getScrollAmount(), false);
            ci.cancel();
        }
    }

    static {
        TAB_TEAM_TITLE = Text.translatable("gui.socialInteractions.tab_team");
        SELECTED_TAB_TEAM_TITLE = TAB_TEAM_TITLE.copyContentOnly().formatted(Formatting.UNDERLINE);
    }
}
