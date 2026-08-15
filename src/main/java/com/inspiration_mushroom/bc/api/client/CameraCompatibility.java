package com.inspiration_mushroom.bc.api.client;

import com.inspiration_mushroom.bc.BetterCamera;

import java.util.Arrays;
import java.util.Objects;
import java.util.function.BooleanSupplier;

public final class CameraCompatibility {
    private static final BypassRegistry allSmoothingBypasses = new BypassRegistry("all camera smoothing");
    private static final BypassRegistry rotationSmoothingBypasses = new BypassRegistry("rotation smoothing");
    private static final BypassRegistry positionSmoothingBypasses = new BypassRegistry("position smoothing");

    public static void registerSmoothingBypass(BooleanSupplier condition) {
        allSmoothingBypasses.register(condition);
    }

    public static void unregisterSmoothingBypass(BooleanSupplier condition) {
        allSmoothingBypasses.unregister(condition);
    }

    public static void registerRotationSmoothingBypass(BooleanSupplier condition) {
        rotationSmoothingBypasses.register(condition);
    }

    public static void unregisterRotationSmoothingBypass(BooleanSupplier condition) {
        rotationSmoothingBypasses.unregister(condition);
    }

    public static void registerPositionSmoothingBypass(BooleanSupplier condition) {
        positionSmoothingBypasses.register(condition);
    }

    public static void unregisterPositionSmoothingBypass(BooleanSupplier condition) {
        positionSmoothingBypasses.unregister(condition);
    }

    public static boolean isRotationSmoothingBypassed() {
        return allSmoothingBypasses.isActive() || rotationSmoothingBypasses.isActive();
    }

    public static boolean isPositionSmoothingBypassed() {
        return allSmoothingBypasses.isActive() || positionSmoothingBypasses.isActive();
    }

    public static boolean isSmoothingBypassed() {
        return allSmoothingBypasses.isActive()
                || rotationSmoothingBypasses.isActive()
                || positionSmoothingBypasses.isActive();
    }

    private CameraCompatibility() {
    }

    private static final class BypassRegistry {
        private final String description;
        private volatile BooleanSupplier[] conditions = new BooleanSupplier[0];

        private BypassRegistry(String description) {
            this.description = description;
        }

        private synchronized void register(BooleanSupplier condition) {
            Objects.requireNonNull(condition, "condition");
            for (BooleanSupplier registered : this.conditions) {
                if (registered == condition) {
                    return;
                }
            }

            BooleanSupplier[] updated = Arrays.copyOf(this.conditions, this.conditions.length + 1);
            updated[updated.length - 1] = condition;
            this.conditions = updated;
        }

        private synchronized void unregister(BooleanSupplier condition) {
            for (int index = 0; index < this.conditions.length; index++) {
                if (this.conditions[index] != condition) {
                    continue;
                }

                BooleanSupplier[] updated = new BooleanSupplier[this.conditions.length - 1];
                System.arraycopy(this.conditions, 0, updated, 0, index);
                System.arraycopy(
                        this.conditions,
                        index + 1,
                        updated,
                        index,
                        this.conditions.length - index - 1
                );
                this.conditions = updated;
                return;
            }
        }

        private boolean isActive() {
            BooleanSupplier[] currentConditions = this.conditions;
            for (BooleanSupplier condition : currentConditions) {
                try {
                    if (condition.getAsBoolean()) {
                        return true;
                    }
                } catch (RuntimeException | LinkageError exception) {
                    this.unregister(condition);
                    BetterCamera.LOGGER.error(
                            "Removed a failing {} bypass condition.",
                            this.description,
                            exception
                    );
                }
            }
            return false;
        }
    }
}
