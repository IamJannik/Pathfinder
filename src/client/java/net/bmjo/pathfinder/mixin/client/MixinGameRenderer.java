package net.bmjo.pathfinder.mixin.client;

import net.bmjo.pathfinder.waypoint.WaypointModel;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import org.joml.Matrix4f;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({GameRenderer.class})
public class MixinGameRenderer {
    @Inject(method = "loadProjectionMatrix", at = @At("HEAD"))
    public void onLoadProjectionMatrixStart(Matrix4f matrix, CallbackInfo info) {
        WaypointModel.onResetProjectionMatrix(matrix);
    }

    @Inject(method = "renderWorld", at = @At("HEAD"))
    public void onRenderWorldStart(float tickDelta, long limitTime, MatrixStack matrix, CallbackInfo info) {
        WaypointModel.beforeRenderWorld();
    }

    @Inject(method = "renderWorld", at = @At(value = "FIELD", opcode = Opcodes.GETFIELD, target = "Lnet/minecraft/client/render/GameRenderer;renderHand:Z"))
    public void onRenderWorldHand(float tickDelta, long limitTime, MatrixStack matrixStack, CallbackInfo info) {
        WaypointModel.onWorldModelViewMatrix(matrixStack);
    }
}