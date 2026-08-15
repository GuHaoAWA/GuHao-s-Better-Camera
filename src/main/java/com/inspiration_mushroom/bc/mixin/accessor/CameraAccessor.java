package com.inspiration_mushroom.bc.mixin.accessor;

import net.minecraft.client.Camera;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Camera.class)
public interface CameraAccessor {
    @Invoker("setPosition")
    void betterCamera$setPosition(Vec3 position);

    @Accessor("eyeHeight")
    float betterCamera$getEyeHeight();

    @Accessor("eyeHeightOld")
    float betterCamera$getEyeHeightOld();
}
