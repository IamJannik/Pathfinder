package net.bmjo.pathfinder.waypoint.render;

import com.google.common.collect.Maps;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.util.Util;

import java.util.HashMap;
import java.util.Map;

public class PathfinderVertexConsumer {
    private final VertexConsumerProvider.Immediate waypointRenderTypeBuffers;

    public PathfinderVertexConsumer() {
        Map<RenderLayer, BufferBuilder> builders = Util.make(Maps.newHashMap(), (map) -> toMap(map, PathfinderRenderLayer.WAYPOINT, new BufferBuilder(256)));
        this.waypointRenderTypeBuffers = VertexConsumerProvider.immediate(builders, new BufferBuilder(256));
    }

    public VertexConsumerProvider.Immediate getWaypointRenderTypeBuffers() {
        return this.waypointRenderTypeBuffers;
    }

    @SuppressWarnings("SameParameterValue")
    private static void toMap(HashMap<RenderLayer, BufferBuilder> map, RenderLayer layer, BufferBuilder builder) {
        map.put(layer, builder);
    }
}