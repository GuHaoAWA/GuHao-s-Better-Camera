package com.inspiration_mushroom.bc.client;

import com.inspiration_mushroom.bc.api.client.CameraCompatibility;
import com.inspiration_mushroom.bc.config.BetterCameraConfig;
import com.inspiration_mushroom.bc.mixin.accessor.CameraAccessor;
import com.mojang.blaze3d.Blaze3D;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public final class CameraPositionController {
    private static final CameraPositionController INSTANCE = new CameraPositionController();
    private static final double MAX_DELTA_SECONDS = 0.1D;
    private static final double MAX_FOCUS_JUMP_SQUARED = 16.0D;
    private static final double COLLISION_MARGIN = 0.02D;
    private static final double UNINITIALIZED_TIME = Double.NaN;

    private final CameraPositionSmoother focusSmoother = new CameraPositionSmoother();
    private BlockGetter previousLevel;
    private Entity previousEntity;
    private Vec3 previousFocus;
    private double lastUpdateTime = UNINITIALIZED_TIME;
    private Camera frameCamera;
    private Entity frameEntity;
    private Vec3 frameAppliedPosition;
    private Vec3 frameTranslation;

    public static CameraPositionController getInstance() {
        return INSTANCE;
    }

    private CameraPositionController() {
    }

    public void apply(Minecraft minecraft, Camera camera, float partialTick) {
        this.clearFrameState();
        Entity entity = camera.getEntity();
        Vec3 targetPosition = camera.getPosition();
        Vec3 focusPosition = this.getFocusPosition(camera, entity, partialTick);

        if (!this.shouldApply(minecraft, camera, entity)
                || !isFinite(targetPosition)
                || !isFinite(focusPosition)) {
            this.reset();
            return;
        }

        double currentTime = Blaze3D.getTime();
        boolean contextChanged = this.previousLevel != minecraft.level
                || this.previousEntity != entity;
        boolean focusJumped = this.previousFocus != null
                && this.previousFocus.distanceToSqr(focusPosition) > MAX_FOCUS_JUMP_SQUARED;

        if (!Double.isFinite(currentTime)
                || contextChanged
                || focusJumped
                || !Double.isFinite(this.lastUpdateTime)) {
            this.focusSmoother.snap(focusPosition);
            this.rememberContext(minecraft.level, entity, focusPosition, currentTime);
            return;
        }

        double deltaSeconds = currentTime - this.lastUpdateTime;
        if (!Double.isFinite(deltaSeconds)
                || deltaSeconds <= 0.0D
                || deltaSeconds > MAX_DELTA_SECONDS) {
            this.focusSmoother.snap(focusPosition);
            this.rememberContext(minecraft.level, entity, focusPosition, currentTime);
            return;
        }

        Vec3 smoothedFocus = this.focusSmoother.update(
                focusPosition,
                deltaSeconds,
                BetterCameraConfig.POSITION_RESPONSE_TIME.get()
        );
        Vec3 desiredPosition = targetPosition.add(smoothedFocus.subtract(focusPosition));
        Vec3 safePosition = this.clampAddedTranslation(
                minecraft.level,
                entity,
                targetPosition,
                desiredPosition
        );
        if (!isFinite(safePosition)) {
            safePosition = targetPosition;
        }

        ((CameraAccessor) (Object) camera).betterCamera$setPosition(safePosition);
        this.frameCamera = camera;
        this.frameEntity = entity;
        this.frameAppliedPosition = safePosition;
        this.frameTranslation = safePosition.subtract(targetPosition);
        this.rememberContext(minecraft.level, entity, focusPosition, currentTime);
    }

    public void reapplyIfOverwritten(Minecraft minecraft, Camera camera) {
        if (this.frameCamera != camera
                || this.frameEntity != camera.getEntity()
                || !isFinite(this.frameAppliedPosition)
                || !isFinite(this.frameTranslation)) {
            this.clearFrameState();
            return;
        }

        Vec3 currentPosition = camera.getPosition();
        if (!isFinite(currentPosition)
                || currentPosition.distanceToSqr(this.frameAppliedPosition) <= 1.0E-12D) {
            this.clearFrameState();
            return;
        }

        Vec3 desiredPosition = currentPosition.add(this.frameTranslation);
        Vec3 safePosition = minecraft.level == null
                ? currentPosition
                : this.clampAddedTranslation(
                        minecraft.level,
                        this.frameEntity,
                        currentPosition,
                        desiredPosition
                );
        if (isFinite(safePosition)) {
            ((CameraAccessor) (Object) camera).betterCamera$setPosition(safePosition);
        }
        this.clearFrameState();
    }

    public void reset() {
        this.focusSmoother.reset();
        this.previousLevel = null;
        this.previousEntity = null;
        this.previousFocus = null;
        this.lastUpdateTime = UNINITIALIZED_TIME;
        this.clearFrameState();
    }

    private boolean shouldApply(Minecraft minecraft, Camera camera, Entity entity) {
        return BetterCameraConfig.ENABLED.get()
                && BetterCameraConfig.APPLY_IN_THIRD_PERSON.get()
                && BetterCameraConfig.APPLY_POSITION_DAMPING.get()
                && camera.isDetached()
                && minecraft.level != null
                && minecraft.player != null
                && entity == minecraft.player
                && minecraft.getCameraEntity() == minecraft.player
                && !minecraft.isPaused()
                && minecraft.screen == null
                && !minecraft.options.smoothCamera
                && minecraft.isWindowActive()
                && !CameraCompatibility.isPositionSmoothingBypassed();
    }

    private Vec3 getFocusPosition(Camera camera, Entity entity, float partialTick) {
        if (entity == null) {
            return null;
        }

        CameraAccessor accessor = (CameraAccessor) (Object) camera;
        return new Vec3(
                Mth.lerp((double) partialTick, entity.xo, entity.getX()),
                Mth.lerp((double) partialTick, entity.yo, entity.getY())
                        + Mth.lerp(partialTick, accessor.betterCamera$getEyeHeightOld(), accessor.betterCamera$getEyeHeight()),
                Mth.lerp((double) partialTick, entity.zo, entity.getZ())
        );
    }

    private Vec3 clampAddedTranslation(
            BlockGetter level,
            Entity entity,
            Vec3 targetPosition,
            Vec3 desiredPosition
    ) {
        Vec3 displacement = desiredPosition.subtract(targetPosition);
        double desiredDistance = displacement.length();
        if (!Double.isFinite(desiredDistance) || desiredDistance < 1.0E-6D) {
            return targetPosition;
        }

        Vec3 direction = displacement.scale(1.0D / desiredDistance);
        double allowedDistance = desiredDistance;
        for (int index = 0; index < 8; index++) {
            double offsetX = ((index & 1) * 2 - 1) * 0.1D;
            double offsetY = ((index >> 1 & 1) * 2 - 1) * 0.1D;
            double offsetZ = ((index >> 2 & 1) * 2 - 1) * 0.1D;
            Vec3 offset = new Vec3(offsetX, offsetY, offsetZ);
            Vec3 rayStart = targetPosition.add(offset);
            Vec3 rayEnd = targetPosition.add(direction.scale(allowedDistance)).add(offset);
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

        return targetPosition.add(direction.scale(allowedDistance));
    }

    private void rememberContext(
            BlockGetter level,
            Entity entity,
            Vec3 focusPosition,
            double currentTime
    ) {
        this.previousLevel = level;
        this.previousEntity = entity;
        this.previousFocus = focusPosition;
        this.lastUpdateTime = currentTime;
    }

    private void clearFrameState() {
        this.frameCamera = null;
        this.frameEntity = null;
        this.frameAppliedPosition = null;
        this.frameTranslation = null;
    }

    private static boolean isFinite(Vec3 position) {
        return position != null
                && Double.isFinite(position.x)
                && Double.isFinite(position.y)
                && Double.isFinite(position.z);
    }
}
