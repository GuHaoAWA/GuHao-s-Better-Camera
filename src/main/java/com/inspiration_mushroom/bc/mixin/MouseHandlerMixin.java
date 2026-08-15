package com.inspiration_mushroom.bc.mixin;

import com.inspiration_mushroom.bc.api.client.CameraCompatibility;
import com.inspiration_mushroom.bc.client.CameraAxisSmoother;
import com.inspiration_mushroom.bc.config.BetterCameraConfig;
import com.mojang.blaze3d.Blaze3D;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = MouseHandler.class, priority = 500)
public abstract class MouseHandlerMixin {
    @Unique
    private static final double DEFAULT_DELTA_SECONDS = 1.0D / 60.0D;
    @Unique
    private static final double MAX_DELTA_SECONDS = 0.1D;
    @Unique
    private static final double UNINITIALIZED_TIME = Double.NaN;
    @Shadow
    private double accumulatedDX;
    @Shadow
    private double accumulatedDY;
    @Shadow
    @Final
    private Minecraft minecraft;

    @Unique
    private final CameraAxisSmoother betterCamera$horizontalSmoother = new CameraAxisSmoother();
    @Unique
    private final CameraAxisSmoother betterCamera$verticalSmoother = new CameraAxisSmoother();
    @Unique
    private ClientLevel betterCamera$previousLevel;
    @Unique
    private LocalPlayer betterCamera$previousPlayer;
    @Unique
    private CameraType betterCamera$previousCameraType;
    @Unique
    private double betterCamera$lastUpdateTime = UNINITIALIZED_TIME;

    @Shadow
    public abstract boolean isMouseGrabbed();

    @Inject(method = "turnPlayer", at = @At("HEAD"))
    private void betterCamera$smoothMouseInput(CallbackInfo callbackInfo) {
        if (!this.betterCamera$shouldApply() || CameraCompatibility.isRotationSmoothingBypassed()) {
            this.betterCamera$reset();
            return;
        }

        LocalPlayer player = this.minecraft.player;
        CameraType cameraType = this.minecraft.options.getCameraType();
        double currentTime = Blaze3D.getTime();
        if (!Double.isFinite(currentTime)) {
            this.betterCamera$reset();
            return;
        }

        double deltaSeconds;
        if (this.betterCamera$contextChanged(player, cameraType)
                || !Double.isFinite(this.betterCamera$lastUpdateTime)) {
            this.betterCamera$resetSmoothers();
            deltaSeconds = DEFAULT_DELTA_SECONDS;
        } else {
            deltaSeconds = currentTime - this.betterCamera$lastUpdateTime;
            if (!Double.isFinite(deltaSeconds)
                    || deltaSeconds <= 0.0D
                    || deltaSeconds > MAX_DELTA_SECONDS) {
                this.betterCamera$resetSmoothers();
                this.betterCamera$rememberContext(player, cameraType, currentTime);
                return;
            }
        }

        double horizontalInput = this.accumulatedDX;
        double verticalInput = this.accumulatedDY;
        if (!Double.isFinite(horizontalInput) || !Double.isFinite(verticalInput)) {
            if (!Double.isFinite(horizontalInput)) {
                this.accumulatedDX = 0.0D;
            }
            if (!Double.isFinite(verticalInput)) {
                this.accumulatedDY = 0.0D;
            }
            this.betterCamera$reset();
            return;
        }

        double fastResponseTime = BetterCameraConfig.FAST_RESPONSE_TIME.get();
        double bodyResponseTime = BetterCameraConfig.BODY_RESPONSE_TIME.get();
        double chaseResponseTime = BetterCameraConfig.CHASE_RESPONSE_TIME.get();
        double fastResponseWeight = BetterCameraConfig.FAST_RESPONSE_WEIGHT.get();
        double chaseResponseWeight = BetterCameraConfig.CHASE_RESPONSE_WEIGHT.get();
        double verticalMultiplier = BetterCameraConfig.VERTICAL_RESPONSE_MULTIPLIER.get();

        double horizontalOutput = this.betterCamera$horizontalSmoother.update(
                horizontalInput,
                deltaSeconds,
                fastResponseTime,
                bodyResponseTime,
                chaseResponseTime,
                fastResponseWeight,
                chaseResponseWeight
        );
        double verticalOutput = this.betterCamera$verticalSmoother.update(
                verticalInput,
                deltaSeconds,
                fastResponseTime * verticalMultiplier,
                bodyResponseTime * verticalMultiplier,
                chaseResponseTime * verticalMultiplier,
                fastResponseWeight,
                chaseResponseWeight
        );

        this.accumulatedDX = horizontalOutput;
        this.accumulatedDY = verticalOutput;
        this.betterCamera$rememberContext(player, cameraType, currentTime);
    }

    @Unique
    private boolean betterCamera$shouldApply() {
        if (!BetterCameraConfig.ENABLED.get()
                || this.minecraft.level == null
                || this.minecraft.player == null
                || this.minecraft.isPaused()
                || this.minecraft.screen != null
                || this.minecraft.options.smoothCamera
                || !this.isMouseGrabbed()
                || !this.minecraft.isWindowActive()
                || this.minecraft.getCameraEntity() != this.minecraft.player) {
            return false;
        }

        if (this.minecraft.options.getCameraType().isFirstPerson()) {
            return BetterCameraConfig.APPLY_IN_FIRST_PERSON.get();
        }
        return BetterCameraConfig.APPLY_IN_THIRD_PERSON.get();
    }

    @Unique
    private boolean betterCamera$contextChanged(LocalPlayer player, CameraType cameraType) {
        return this.betterCamera$previousLevel != this.minecraft.level
                || this.betterCamera$previousPlayer != player
                || this.betterCamera$previousCameraType != cameraType;
    }

    @Unique
    private void betterCamera$reset() {
        this.betterCamera$resetSmoothers();
        this.betterCamera$lastUpdateTime = UNINITIALIZED_TIME;
        this.betterCamera$previousLevel = null;
        this.betterCamera$previousPlayer = null;
        this.betterCamera$previousCameraType = null;
    }

    @Unique
    private void betterCamera$resetSmoothers() {
        this.betterCamera$horizontalSmoother.reset();
        this.betterCamera$verticalSmoother.reset();
    }

    @Unique
    private void betterCamera$rememberContext(LocalPlayer player, CameraType cameraType, double currentTime) {
        this.betterCamera$previousLevel = this.minecraft.level;
        this.betterCamera$previousPlayer = player;
        this.betterCamera$previousCameraType = cameraType;
        this.betterCamera$lastUpdateTime = currentTime;
    }
}
