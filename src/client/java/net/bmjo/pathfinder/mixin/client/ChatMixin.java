package net.bmjo.pathfinder.mixin.client;

import com.mojang.authlib.GameProfile;
import net.bmjo.pathfinder.waypoint.WaypointHandler;
import net.minecraft.client.network.message.MessageHandler;
import net.minecraft.network.message.MessageType;
import net.minecraft.network.message.SignedMessage;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.time.Instant;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Mixin(MessageHandler.class)
public class ChatMixin {
    //@Inject(method = "onChatMessage", at = @At(value = "INVOKE", target = "Ljava/time/Instant;now()Ljava/time/Instant;"), cancellable = true)
    @Inject(method = "processChatMessageInternal", at = @At(value = "HEAD"), cancellable = true)
    public void isPFMessage(MessageType.Parameters params, SignedMessage message, Text decorated, GameProfile sender, boolean onlyShowSecureChat, Instant receptionTimestamp, CallbackInfoReturnable<Boolean> cir) {
        String msg = message.getContent().getString();
        if (msg.contains("Lets meet here: X:")) {
            int[] cords = new int[3];
            Pattern pattern = Pattern.compile("-?\\d+");
            Matcher matcher = pattern.matcher(msg);
            int cord = 0;
            for (; cord < 3 && matcher.find(); cord++)
                cords[cord] = Integer.parseInt(matcher.group());
            if (cord == 3)
                WaypointHandler.tryAddWaypoint(sender.getId(), new BlockPos(cords[0], cords[1], cords[2]));
        } else if (msg.equals("Forget about my meeting point.")) {
            WaypointHandler.tryRemoveWaypoint(sender.getId());
        } else {
            return;
        }
        cir.setReturnValue(true);
    }
}
