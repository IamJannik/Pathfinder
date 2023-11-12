package net.bmjo.pathfinder.mixin.client;

import com.mojang.authlib.GameProfile;
import net.bmjo.pathfinder.util.RegExEr;
import net.minecraft.client.network.message.MessageHandler;
import net.minecraft.network.message.MessageType;
import net.minecraft.network.message.SignedMessage;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.time.Instant;

/**
 * Mixin class for modifying the behavior of the MessageHandler.
 * Creates a waypoint if a message matches the "StringEntanglement" pattern of Pathfinder and cancels the ChatMessage internal process.
 *
 * @author BMJO
 * @version 1.0
 */
@Mixin(MessageHandler.class)
public class MixinMessageHandler {
    @Inject(method = "processChatMessageInternal", at = @At(value = "HEAD"), cancellable = true)
    public void isPathfinderMessage(MessageType.Parameters params, SignedMessage message, Text decorated, GameProfile sender, boolean onlyShowSecureChat, Instant receptionTimestamp, CallbackInfoReturnable<Boolean> cir) {
        String msg = message.getContent().getString();
        if (msg.contains("Lets meet at:") || msg.equals("Forget about my meeting point."))
            if (RegExEr.waypointFromMessage(sender.getId(), msg))
                cir.setReturnValue(true);
    }
}
