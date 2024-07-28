package net.bmjo.pathfinder.util;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.util.Identifier;

import java.util.HashMap;

public class MobFaceDrawer {
    private static final HashMap<EntityType<? extends MobEntity>, Face> FACES = new HashMap<>();

    public static void drawFace(DrawContext drawContext, EntityType<?> mob) {
        if (!hasFace(mob))
            return;
        Face face = getFace(mob);
        drawContext.drawTexture(face.texture, -face.width / 2, -face.height, face.x, face.y, face.width, face.height, face.imgWidth, face.imgHeigh);
        drawContext.draw();
    }

    private static Face getFace(EntityType<?> mob) {
        return FACES.get(mob);
    }

    public static boolean hasFace(EntityType<?> mob) {
        return FACES.containsKey(mob);
    }

    static {
        FACES.put(EntityType.SHEEP, new Face(Identifier.of("textures/entity/sheep/sheep.png"), 7, 8, 8, 6, 64, 32));
        FACES.put(EntityType.PIG, new Face(Identifier.of("textures/entity/pig/pig.png"), 8, 8, 8, 8, 64, 32));
        FACES.put(EntityType.COW, new Face(Identifier.of("textures/entity/cow/cow.png"), 8, 8, 8, 8, 64, 32));
    }

    private record Face(Identifier texture, int x, int y, int width, int height, int imgWidth, int imgHeigh) {

    }
}
