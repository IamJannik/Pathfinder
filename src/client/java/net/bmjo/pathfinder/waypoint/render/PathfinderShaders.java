package net.bmjo.pathfinder.waypoint.render;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.ShaderProgram;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.resource.ResourceManager;
import xaero.common.graphics.shader.FramebufferLinesShader;

import java.io.IOException;

public class PathfinderShaders {
    public static FramebufferLinesShader FRAMEBUFFER_LINES = null;
    public static ShaderProgram POSITION_COLOR = null;
    private static boolean firstTime = true;

    public PathfinderShaders() {
    }

    public static void onResourceReload(ResourceManager resourceManager) {

        try {
            FRAMEBUFFER_LINES = reloadShader(FRAMEBUFFER_LINES, new FramebufferLinesShader(resourceManager));
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
        if (FRAMEBUFFER_LINES == null && firstTime) {
            onResourceReload(MinecraftClient.getInstance().getResourceManager());
        }

    }
}