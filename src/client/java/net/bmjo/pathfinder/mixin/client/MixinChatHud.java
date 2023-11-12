package net.bmjo.pathfinder.mixin.client;

import net.bmjo.pathfinder.util.RegExEr;
import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.client.gui.hud.MessageIndicator;
import net.minecraft.network.message.MessageSignatureData;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin class for modifying the behavior of the Minecraft ChatHud.
 * If a message is not recognized by the {@link MixinMessageHandler} or it is added directly to the ChatHud, this class serves as a backup.
 * Creates a waypoint if a message matches the "StringEntanglement" pattern of Pathfinder and cancels the addition to the ChatHud.
 *
 * @author BMJO
 * @version 1.0
 */
@Mixin(ChatHud.class)
public class MixinChatHud {
    @Inject(method = "addMessage(Lnet/minecraft/text/Text;Lnet/minecraft/network/message/MessageSignatureData;ILnet/minecraft/client/gui/hud/MessageIndicator;Z)V", at = @At(value = "HEAD"), cancellable = true)
    public void isPathfinderMessage(Text message, MessageSignatureData signature, int ticks, MessageIndicator indicator, boolean refresh, CallbackInfo ci) {
        String msg = message.getString();
        if (msg.contains("Lets meet at:") || msg.contains("Forget about my meeting point."))
            if (RegExEr.waypointFromMessage(msg))
                ci.cancel();
    }
}
