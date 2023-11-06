package net.bmjo.pathfinder.waypoint.render;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.ShaderProgram;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.resource.ResourceManager;

import java.io.IOException;

public class PathfinderShaders {
    public static ShaderProgram POSITION_COLOR = null;
    private static boolean firstTime = true;

    public PathfinderShaders() {
    }

    public static void onResourceReload(ResourceManager resourceManager) {

        try {
            POSITION_COLOR = reloadShader(POSITION_COLOR, new ShaderProgram(resourceManager, "pathfinder/position_color", VertexFormats.POSITION_COLOR_TEXTURE));
        } catch (IOException var3) {
            if (firstTime) {
                throw new RuntimeException("Couldn't reload the pathfinder shaders!", var3);
            }
        }

        firstTime = false;
    }

    private static <S extends ShaderProgram> S reloadShader(S current, S newOne) throws IOException {
        if (current != null) {
            current.close();
        }

        return newOne;
    }

    public static void ensureShaders() {
        if (firstTime) {
            onResourceReload(MinecraftClient.getInstance().getResourceManager());
        }

    }
}