package com.inspiration_mushroom.bc.mixin;

import com.inspiration_mushroom.bc.api.client.CameraCompatibility;
import com.inspiration_mushroom.bc.client.CameraPositionSmoother;
import com.inspiration_mushroom.bc.config.BetterCameraConfig;
import com.mojang.blaze3d.Blaze3D;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = Camera.class, priority = 500)
public abstract class CameraMixin {
    @Unique
    private static final double MAX_DELTA_SECONDS = 0.1D;
    @Unique
    private static final double MAX_TARGET_JUMP_SQUARED = 144.0D;
    @Unique
    private static final double COLLISION_MARGIN = 0.05D;
    @Unique
    private static final double UNINITIALIZED_TIME = Double.NaN;

    @Unique
    private final CameraPositionSmoother betterCamera$positionSmoother = new CameraPositionSmoother();
    @Unique
    private BlockGetter betterCamera$previousLevel;
    @Unique
    private Entity betterCamera$previousEntity;
    @Unique
    private Vec3 betterCamera$previousTarget;
    @Unique
    private boolean betterCamera$previousReverse;
    @Unique
    private double betterCamera$lastUpdateTime = UNINITIALIZED_TIME;

    @Shadow
    protected abstract void setPosition(Vec3 position);

    @Inject(method = "setup", at = @At("RETURN"))
    private void betterCamera$smoothPosition(
            BlockGetter level,
            Entity entity,
            boolean detached,
            boolean thirdPersonReverse,
            float partialTick,
            CallbackInfo callbackInfo
    ) {
        Minecraft minecraft = Minecraft.getInstance();
        Camera camera = (Camera) (Object) this;
        Vec3 targetPosition = camera.getPosition();

        if (!this.betterCamera$shouldApply(minecraft, entity, detached)
                || !betterCamera$isFinite(targetPosition)) {
            this.betterCamera$reset();
            return;
        }

        double currentTime = Blaze3D.getTime();
        boolean contextChanged = this.betterCamera$previousLevel != level
                || this.betterCamera$previousEntity != entity
                || this.betterCamera$previousReverse != thirdPersonReverse;
        boolean targetJumped = this.betterCamera$previousTarget != null
                && this.betterCamera$previousTarget.distanceToSqr(targetPosition) > MAX_TARGET_JUMP_SQUARED;

        if (!Double.isFinite(currentTime)
                || contextChanged
                || targetJumped
                || !Double.isFinite(this.betterCamera$lastUpdateTime)) {
            this.betterCamera$positionSmoother.snap(targetPosition);
            this.betterCamera$rememberContext(level, entity, thirdPersonReverse, targetPosition, currentTime);
            return;
        }

        double deltaSeconds = currentTime - this.betterCamera$lastUpdateTime;
        if (!Double.isFinite(deltaSeconds)
                || deltaSeconds <= 0.0D
                || deltaSeconds > MAX_DELTA_SECONDS) {
            this.betterCamera$positionSmoother.snap(targetPosition);
            this.betterCamera$rememberContext(level, entity, thirdPersonReverse, targetPosition, currentTime);
            return;
        }

        Vec3 smoothedPosition = this.betterCamera$positionSmoother.update(
                targetPosition,
                deltaSeconds,
                BetterCameraConfig.POSITION_RESPONSE_TIME.get()
        );
        Vec3 focusPosition = entity.getEyePosition(partialTick);
        Vec3 safePosition = betterCamera$isFinite(focusPosition)
                ? this.betterCamera$clampToCollision(level, entity, focusPosition, smoothedPosition)
                : targetPosition;
        if (!betterCamera$isFinite(safePosition)) {
            safePosition = targetPosition;
        }

        if (safePosition.distanceToSqr(smoothedPosition) > 1.0E-10D) {
            this.betterCamera$positionSmoother.snap(safePosition);
        }

        this.setPosition(safePosition);
        this.betterCamera$rememberContext(
                level,
                entity,
                thirdPersonReverse,
                targetPosition,
                currentTime
        );
    }

    @Unique
    private boolean betterCamera$shouldApply(Minecraft minecraft, Entity entity, boolean detached) {
        return BetterCameraConfig.ENABLED.get()
                && BetterCameraConfig.APPLY_IN_THIRD_PERSON.get()
                && BetterCameraConfig.APPLY_POSITION_DAMPING.get()
                && detached
                && minecraft.level != null
                && minecraft.player != null
                && entity == minecraft.player
                && minecraft.getCameraEntity() == minecraft.player
                && !minecraft.isPaused()
                && minecraft.screen == null
                && !minecraft.options.smoothCamera
                && minecraft.isWindowActive()
                && !CameraCompatibility.isSmoothingBypassed();
    }

    @Unique
    private Vec3 betterCamera$clampToCollision(
            BlockGetter level,
            Entity entity,
            Vec3 focusPosition,
            Vec3 desiredPosition
    ) {
        Vec3 displacement = desiredPosition.subtract(focusPosition);
        double desiredDistance = displacement.length();
        if (!Double.isFinite(desiredDistance) || desiredDistance < 1.0E-6D) {
            return desiredPosition;
        }

        Vec3 direction = displacement.scale(1.0D / desiredDistance);
        double allowedDistance = desiredDistance;
        for (int index = 0; index < 8; index++) {
            double offsetX = ((index & 1) * 2 - 1) * 0.1D;
            double offsetY = ((index >> 1 & 1) * 2 - 1) * 0.1D;
            double offsetZ = ((index >> 2 & 1) * 2 - 1) * 0.1D;
            Vec3 offset = new Vec3(offsetX, offsetY, offsetZ);
            Vec3 rayStart = focusPosition.add(offset);
            Vec3 rayEnd = focusPosition.add(direction.scale(allowedDistance)).add(offset);
            HitResult hitResult = level.clip(new ClipContext(
                    rayStart,
                    rayEnd,
                    ClipContext.Block.VISUAL,
                    ClipContext.Fluid.NONE,
                    entity
            ));
            if (hitResult.getType() != HitResult.Type.MISS) {
                double hitDistance = hitResult.getLocation().distanceTo(rayStart) - COLLISION_MARGIN;
                allowedDistance = Math.min(allowedDistance, Math.max(0.0D, hitDistance));
            }
        }

        return focusPosition.add(direction.scale(allowedDistance));
    }

    @Unique
    private void betterCamera$rememberContext(
            BlockGetter level,
            Entity entity,
            boolean thirdPersonReverse,
            Vec3 targetPosition,
            double currentTime
    ) {
        this.betterCamera$previousLevel = level;
        this.betterCamera$previousEntity = entity;
        this.betterCamera$previousReverse = thirdPersonReverse;
        this.betterCamera$previousTarget = targetPosition;
        this.betterCamera$lastUpdateTime = currentTime;
    }

    @Unique
    private void betterCamera$reset() {
        this.betterCamera$positionSmoother.reset();
        this.betterCamera$previousLevel = null;
        this.betterCamera$previousEntity = null;
        this.betterCamera$previousTarget = null;
        this.betterCamera$previousReverse = false;
        this.betterCamera$lastUpdateTime = UNINITIALIZED_TIME;
    }

    @Unique
    private static boolean betterCamera$isFinite(Vec3 position) {
        return Double.isFinite(position.x)
                && Double.isFinite(position.y)
                && Double.isFinite(position.z);
    }
}
