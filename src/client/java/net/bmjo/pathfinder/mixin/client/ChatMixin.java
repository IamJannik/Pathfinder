package net.bmjo.pathfinder.mixin.client;

import com.mojang.authlib.GameProfile;
import net.bmjo.pathfinder.gang.GangHandler;
import net.bmjo.pathfinder.waypoint.WaypointHandler;
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
    @Inject(method = "onChatMessage", at = @At(value = "INVOKE", target = "Ljava/time/Instant;now()Ljava/time/Instant;"))
    public void isPFMessage(SignedMessage message, GameProfile sender, MessageType.Parameters params, CallbackInfo ci) {
        if (!GangHandler.isMember(sender.getId()))
            return;
        String msg = message.getContent().getString();
        if (msg.contains("Lets meet here: X:")) {
            int[] cords = new int[3];
            Pattern pattern = Pattern.compile("-?\\d+");
            Matcher matcher = pattern.matcher(msg);
            int cord = 0;
            for (; cord < 3 && matcher.find(); cord++)
                cords[cord] = Integer.parseInt(matcher.group());
            if (cord == 3)
                WaypointHandler.addWaypoint(sender.getId(), new BlockPos(cords[0], cords[1], cords[2]));
        } else if (msg.equals("Forget about my meeting point.")) {
            WaypointHandler.removeWaypoint(sender.getId());
        } else if (msg.equals("I added you to my gang.")) { //TODO doch weg
            GangHandler.addMember(sender.getId());
        } else if (msg.equals("I kicked you out of my gang.")) {
            GangHandler.removeMember(sender.getId());
        }
    }
}
