package net.bmjo.pathfinder.util;

import net.bmjo.pathfinder.Pathfinder;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;

public class PathfinderSounds {
    public static final Identifier WAYPOINT_CREATE_ID = Pathfinder.identifier("waypoint_create");
    public static SoundEvent WAYPOINT_CREATE = SoundEvent.of(WAYPOINT_CREATE_ID);

    public static void registerSounds() {
        Registry.register(Registries.SOUND_EVENT, WAYPOINT_CREATE_ID, WAYPOINT_CREATE);
    }
}
