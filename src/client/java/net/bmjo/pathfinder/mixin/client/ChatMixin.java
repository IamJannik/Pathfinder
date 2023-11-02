package net.bmjo.pathfinder.mixin.client;

import com.mojang.authlib.GameProfile;
import net.bmjo.pathfinder.waypoint.Waypoints;
import net.minecraft.client.network.message.MessageHandler;
import net.minecraft.network.message.MessageType;
import net.minecraft.network.message.SignedMessage;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Mixin(MessageHandler.class)
public class ChatMixin {
    @Inject(method = "onChatMessage", at = @At(value = "INVOKE", target = "Ljava/time/Instant;now()Ljava/time/Instant;"), cancellable = true)
    public void isPFMessage(SignedMessage message, GameProfile sender, MessageType.Parameters params, CallbackInfo ci) {
        String msg = message.getContent().getString();
        if (msg.contains("Lets meet here: X:")) {
            int[] cords = new int[3];
            Pattern pattern = Pattern.compile("\\d+");
            Matcher matcher = pattern.matcher(msg);
            int cord = 0;
            for (; cord < 3 && matcher.find(); cord++)
                cords[cord] = Integer.parseInt(matcher.group());
            if (cord == 3) {
                Waypoints.addWaypoint(0, new BlockPos(cords[0], cords[1], cords[2]));
                ci.cancel();
            }
        }
    }
}
