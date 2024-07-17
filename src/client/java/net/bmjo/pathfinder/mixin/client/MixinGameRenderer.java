package net.bmjo.pathfinder.mixin.client;

import net.bmjo.pathfinder.waypoint.WaypointModel;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

/**
 * Mixin class for modifying the behavior of the Minecraft GameRenderer.
 * Hooks into the rendering process to handle the waypoint Rendering.
 */
@Mixin({GameRenderer.class})
public class MixinGameRenderer {
    @Inject(method = "renderWorld", at = @At("HEAD"))
    public void onRenderWorldStart(RenderTickCounter tickCounter, CallbackInfo ci) {
        WaypointModel.beforeRenderWorld();
    }

    @Inject(method = "loadProjectionMatrix", at = @At("HEAD"))
    public void onLoadProjectionMatrixStart(Matrix4f matrix, CallbackInfo info) {
        WaypointModel.onResetProjectionMatrix(matrix);
    }

    @Inject(method = "renderWorld", at = @At(value = "FIELD", opcode = Opcodes.GETFIELD, target = "Lnet/minecraft/client/render/GameRenderer;renderHand:Z"), locals = LocalCapture.CAPTURE_FAILHARD)
    public void onRenderWorldHand(RenderTickCounter tickCounter, CallbackInfo ci, float f, boolean bl, Camera camera, Entity entity, float g, double d, Matrix4f matrix4f, MatrixStack matrixStack, float h, float i, Quaternionf quaternionf, Matrix4f matrix4f2) {
        WaypointModel.onWorldModelViewMatrix(matrix4f2);
    }
}