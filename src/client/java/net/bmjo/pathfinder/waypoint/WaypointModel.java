package net.bmjo.pathfinder.waypoint;

import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.util.math.MatrixStack;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.List;

@Deprecated
public record WaypointModel(float[] vertices, float[] uvs) {
    public void render(MatrixStack matrixStack, VertexConsumer vertexConsumer, int color) {
        Matrix4f modelMatrix = matrixStack.peek().getPositionMatrix();
        Matrix3f normalMatrix = matrixStack.peek().getNormalMatrix();
        int count = vertices.length / 3;
        for (int i = 0; i < count; i++) {
            vertexConsumer
                    .vertex(modelMatrix, vertices[i * 3], vertices[i * 3 + 1], vertices[i * 3 + 2])
                    .color(-1)
                    .texture(uvs[i * 2], uvs[i * 2 + 1])
                    .overlay(OverlayTexture.DEFAULT_UV)
                    .light(color)
                    .normal(normalMatrix, 1, 0.35f, 0)
                    .next();
        }
    }

    public static WaypointModel create(int height, float radius) {
        List<Float> vertices = new ArrayList<>(24);
        List<Float> uvs = new ArrayList<>(16);
        for (int x = -1; x <= 1 ; x += 2)
            for (int z = -1; z <= 1 ; z += 2) {
                addPos(x + radius, 0, z + radius, x * z < 0 ? 0 : 1, 0, vertices, uvs);
                addPos(x + radius, height, z + radius, x * z < 0 ? 0 : 1, height, vertices, uvs);
            }
        return new WaypointModel(toFloatArray(vertices), toFloatArray(uvs));
    }

    private static void addPos(float x, float y, float z, float u, float v, List<Float> vertices, List<Float> uvs) {
        vertices.add(x);
        vertices.add(y);
        vertices.add(z);
        uvs.add(u);
        uvs.add(v);
    }

    private static float[] toFloatArray(List<Float> floats) {
        float[] array = new float[floats.size()];
        int i = 0;
        for (float f : floats)
            array[i++] = f;
        return array;
    }
}
