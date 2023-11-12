package net.bmjo.pathfinder.util;

import net.bmjo.pathfinder.PathfinderClient;
import net.bmjo.pathfinder.waypoint.WaypointHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.minecraft.util.InvalidIdentifierException;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class RegExEr {
    public static boolean waypointFromMessage(String message) {
        UUID sender = senderFromMessage(message);
        if (sender == null) {
            PathfinderClient.LOGGER.info("Pathfinder Chat Message detected but couldn't find Sender.");
            return false;
        }
        return waypointFromMessage(sender, message);
    }

    public static boolean waypointFromMessage(UUID sender, String message) {
        if (message.contains("Lets meet at:")) {
            try {
                GlobalPos globalPos = posFromMessage(message);
                WaypointHandler.tryAddWaypoint(sender, globalPos);
                return true;
            } catch (InvalidIdentifierException | NumberFormatException e) {
                PathfinderClient.LOGGER.info("Pathfinder Chat Message detected but was wrong Pattern.");
                return false;
            }
        } else if (message.equals("Forget about my meeting point.")) {
            WaypointHandler.tryRemoveWaypoint(sender);
            return true;
        }
        return false;
    }

    @Nullable
    private static UUID senderFromMessage(String message) {
        ClientPlayerEntity clientPlayer = PathfinderClient.getPlayer();
        if (clientPlayer == null)
            return null;
        String[] parts;
        if (message.contains("Lets meet at:"))
            parts = message.split("Lets meet at:");
        else if (message.contains("Forget about my meeting point."))
            parts = message.split("Forget about my meeting point.");
        else
            return null;
        String name = parts[1].split("~")[1].split("\\W")[0]; // Name
        if (name.equals(clientPlayer.getName().getString())) // Own Message
            return null;
        PlayerListEntry playerListEntry = clientPlayer.networkHandler.getPlayerListEntry(name);
        if (playerListEntry != null)
            return playerListEntry.getProfile().getId();
        return null;
    }

    private static GlobalPos posFromMessage(String message) throws PatternSyntaxException, NumberFormatException {
        String[] splits = message.split(":|in the ");
        if (splits.length < 5)
            throw new PatternSyntaxException("Pathfinder Chat Message detected but was wrong Pattern.", ":|in the ", -1);
        int firstPos = 0;
        for (String text : splits) {
            if (text.contains("Lets meet at"))
                break;
            firstPos++;
        }
        if (firstPos > splits.length - 5)
            throw new PatternSyntaxException("Pathfinder Chat Message detected but was wrong Pattern.", ":|in the ", -1);
        int x = Integer.parseInt(splits[firstPos + 2].replaceAll("[^-?\\d]", "")); // X
        int y = Integer.parseInt(splits[firstPos + 3].replaceAll("[^-?\\d]", "")); // Y
        int z = Integer.parseInt(splits[firstPos + 4].replaceAll("[^-?\\d]", "")); // Z
        BlockPos pos = new BlockPos(x, y, z);
        String dimension = splits[firstPos + 5].split("\\.")[0]; // Dimension
        String dimensionKey = dimension.replace(" ", ":").replaceAll("[\\W&&[^:]]", "");
        RegistryKey<World> world = RegistryKey.of(RegistryKeys.WORLD, new Identifier(dimensionKey.toLowerCase()));
        return GlobalPos.create(world, pos);
    }

    public static String upperCaseFirst(String string) {
        return Pattern.compile("^.").matcher(string).replaceFirst(m -> m.group().toUpperCase());
    }
}
