package com.inspiration_mushroom.bc.config;

import net.minecraftforge.common.ForgeConfigSpec;

public final class BetterCameraConfig {
    public static final double FAST_RESPONSE_TIME_MIN = 0.008D;
    public static final double FAST_RESPONSE_TIME_MAX = 0.25D;
    public static final double BODY_RESPONSE_TIME_MIN = 0.02D;
    public static final double BODY_RESPONSE_TIME_MAX = 0.5D;
    public static final double CHASE_RESPONSE_TIME_MIN = 0.05D;
    public static final double CHASE_RESPONSE_TIME_MAX = 0.8D;
    public static final double RESPONSE_WEIGHT_MIN = 0.0D;
    public static final double RESPONSE_WEIGHT_MAX = 1.0D;
    public static final double POSITION_RESPONSE_TIME_MIN = 0.02D;
    public static final double POSITION_RESPONSE_TIME_MAX = 0.6D;
    public static final double VERTICAL_RESPONSE_MULTIPLIER_MIN = 0.5D;
    public static final double VERTICAL_RESPONSE_MULTIPLIER_MAX = 1.5D;

    public static final ForgeConfigSpec SPEC;
    public static final ForgeConfigSpec.BooleanValue ENABLED;
    public static final ForgeConfigSpec.BooleanValue APPLY_IN_FIRST_PERSON;
    public static final ForgeConfigSpec.BooleanValue APPLY_IN_THIRD_PERSON;
    public static final ForgeConfigSpec.BooleanValue APPLY_POSITION_DAMPING;
    public static final ForgeConfigSpec.DoubleValue FAST_RESPONSE_TIME;
    public static final ForgeConfigSpec.DoubleValue BODY_RESPONSE_TIME;
    public static final ForgeConfigSpec.DoubleValue CHASE_RESPONSE_TIME;
    public static final ForgeConfigSpec.DoubleValue FAST_RESPONSE_WEIGHT;
    public static final ForgeConfigSpec.DoubleValue CHASE_RESPONSE_WEIGHT;
    public static final ForgeConfigSpec.DoubleValue POSITION_RESPONSE_TIME;
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
        APPLY_POSITION_DAMPING = builder
                .comment("Adds character-translation smoothing after other camera transforms have been resolved.")
                .define("applyPositionDamping", true);
        FAST_RESPONSE_TIME = builder
                .comment("Fast channel 95% response time in seconds. Lower values make initial movement more immediate.")
                .defineInRange("fastResponseTime", 0.032D, FAST_RESPONSE_TIME_MIN, FAST_RESPONSE_TIME_MAX);
        BODY_RESPONSE_TIME = builder
                .comment("Body channel 95% response time in seconds. Higher values add more cinematic weight.")
                .defineInRange("bodyResponseTime", 0.135D, BODY_RESPONSE_TIME_MIN, BODY_RESPONSE_TIME_MAX);
        CHASE_RESPONSE_TIME = builder
                .comment("Chase channel 95% response time in seconds. Higher values pursue the target point longer.")
                .defineInRange("chaseResponseTime", 0.2D, CHASE_RESPONSE_TIME_MIN, CHASE_RESPONSE_TIME_MAX);
        FAST_RESPONSE_WEIGHT = builder
                .comment("Blend weight of the fast channel. The remaining weight uses the cinematic channels.")
                .defineInRange("fastResponseWeight", 0.28D, RESPONSE_WEIGHT_MIN, RESPONSE_WEIGHT_MAX);
        CHASE_RESPONSE_WEIGHT = builder
                .comment("Share of the cinematic response assigned to the slow target-chasing channel.")
                .defineInRange("chaseResponseWeight", 0.3D, RESPONSE_WEIGHT_MIN, RESPONSE_WEIGHT_MAX);
        POSITION_RESPONSE_TIME = builder
                .comment("Character-focus translation 95% response time in seconds. Higher values add more movement lag.")
                .defineInRange("positionResponseTime", 0.24D, POSITION_RESPONSE_TIME_MIN, POSITION_RESPONSE_TIME_MAX);
        VERTICAL_RESPONSE_MULTIPLIER = builder
                .comment("Multiplier for vertical response times. Values below one make vertical aim settle faster.")
                .defineInRange(
                        "verticalResponseMultiplier",
                        0.90D,
                        VERTICAL_RESPONSE_MULTIPLIER_MIN,
                        VERTICAL_RESPONSE_MULTIPLIER_MAX
                );

        builder.pop();
        SPEC = builder.build();
    }

    private BetterCameraConfig() {
    }
}
