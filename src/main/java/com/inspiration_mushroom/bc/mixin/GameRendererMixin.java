package com.inspiration_mushroom.bc.mixin;

import com.inspiration_mushroom.bc.client.CameraPositionController;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = GameRenderer.class, priority = 100)
public abstract class GameRendererMixin {
    @Shadow
    @Final
    private Camera mainCamera;

    @Inject(
            method = "renderLevel",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/LevelRenderer;prepareCullFrustum(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/world/phys/Vec3;Lorg/joml/Matrix4f;)V"
            ),
            require = 0
    )
    private void betterCamera$applyFinalCameraTranslation(
            float partialTick,
            long finishTimeNano,
            PoseStack poseStack,
            CallbackInfo callbackInfo
    ) {
        CameraPositionController.getInstance().reapplyIfOverwritten(
                Minecraft.getInstance(),
                this.mainCamera
        );
    }
}
