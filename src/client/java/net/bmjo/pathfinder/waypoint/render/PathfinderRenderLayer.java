package net.bmjo.pathfinder.waypoint.render;

import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderPhase;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;

public class PathfinderRenderLayer extends RenderLayer {
    public static final RenderLayer WAYPOINT;

    private PathfinderRenderLayer(String name, VertexFormat vertexFormat, VertexFormat.DrawMode drawMode, int expectedBufferSize, boolean hasCrumbling, boolean translucent, Runnable startAction, Runnable endAction) {
        super(name, vertexFormat, drawMode, expectedBufferSize, hasCrumbling, translucent, startAction, endAction);
    }

    static {
        RenderLayer.MultiPhaseParameters multiPhaseParameters = RenderLayer.MultiPhaseParameters.builder().program(RenderPhase.COLOR_PROGRAM).transparency(Transparency.TRANSLUCENT_TRANSPARENCY).layering(RenderPhase.POLYGON_OFFSET_LAYERING).build(false);
        WAYPOINT = of("waypoint", VertexFormats.POSITION_COLOR, VertexFormat.DrawMode.QUADS, 256, false, false, multiPhaseParameters);
    }
}