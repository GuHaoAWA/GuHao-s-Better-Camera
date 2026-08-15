package com.guhao.bc.config;

import net.minecraftforge.common.ForgeConfigSpec;

public final class BetterCameraConfig {
    public static final ForgeConfigSpec SPEC;
    public static final ForgeConfigSpec.BooleanValue ENABLED;
    public static final ForgeConfigSpec.BooleanValue APPLY_IN_FIRST_PERSON;
    public static final ForgeConfigSpec.BooleanValue APPLY_IN_THIRD_PERSON;
    public static final ForgeConfigSpec.DoubleValue FAST_RESPONSE_TIME;
    public static final ForgeConfigSpec.DoubleValue BODY_RESPONSE_TIME;
    public static final ForgeConfigSpec.DoubleValue FAST_RESPONSE_WEIGHT;
    public static final ForgeConfigSpec.DoubleValue VERTICAL_RESPONSE_MULTIPLIER;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        builder.comment("AAA-style acceleration-continuous camera input shaping.").push("damping");

        ENABLED = builder.comment("Enables the damped camera.").define("enabled", true);
        APPLY_IN_FIRST_PERSON = builder
                .comment("Applies damping in first person. Disabled by default to avoid visual aiming lag.")
                .define("applyInFirstPerson", false);
        APPLY_IN_THIRD_PERSON = builder
                .comment("Applies damping in third person.")
                .define("applyInThirdPerson", true);
        FAST_RESPONSE_TIME = builder
                .comment("Fast channel 95% response time in seconds. Lower values make initial movement more immediate.")
                .defineInRange("fastResponseTime", 0.032D, 0.008D, 0.25D);
        BODY_RESPONSE_TIME = builder
                .comment("Body channel 95% response time in seconds. Higher values add more cinematic weight.")
                .defineInRange("bodyResponseTime", 0.135D, 0.02D, 0.5D);
        FAST_RESPONSE_WEIGHT = builder
                .comment("Blend weight of the fast channel. The remaining weight uses the smooth body channel.")
                .defineInRange("fastResponseWeight", 0.28D, 0.0D, 1.0D);
        VERTICAL_RESPONSE_MULTIPLIER = builder
                .comment("Multiplier for vertical response times. Values below one make vertical aim settle faster.")
                .defineInRange("verticalResponseMultiplier", 0.90D, 0.5D, 1.5D);

        builder.pop();
        SPEC = builder.build();
    }

    private BetterCameraConfig() {
    }
}
