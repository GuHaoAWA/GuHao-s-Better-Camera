package com.guhao.bc.api.client;

import com.guhao.bc.BetterCamera;

import java.util.Arrays;
import java.util.Objects;
import java.util.function.BooleanSupplier;

public final class CameraCompatibility {
    private static volatile BooleanSupplier[] smoothingBypasses = new BooleanSupplier[0];

    public static synchronized void registerSmoothingBypass(BooleanSupplier condition) {
        Objects.requireNonNull(condition, "condition");
        for (BooleanSupplier registered : smoothingBypasses) {
            if (registered == condition) {
                return;
            }
        }

        BooleanSupplier[] updated = Arrays.copyOf(smoothingBypasses, smoothingBypasses.length + 1);
        updated[updated.length - 1] = condition;
        smoothingBypasses = updated;
    }

    public static synchronized void unregisterSmoothingBypass(BooleanSupplier condition) {
        for (int index = 0; index < smoothingBypasses.length; index++) {
            if (smoothingBypasses[index] != condition) {
                continue;
            }

            BooleanSupplier[] updated = new BooleanSupplier[smoothingBypasses.length - 1];
            System.arraycopy(smoothingBypasses, 0, updated, 0, index);
            System.arraycopy(
                    smoothingBypasses,
                    index + 1,
                    updated,
                    index,
                    smoothingBypasses.length - index - 1
            );
            smoothingBypasses = updated;
            return;
        }
    }

    public static boolean isSmoothingBypassed() {
        BooleanSupplier[] conditions = smoothingBypasses;
        for (BooleanSupplier condition : conditions) {
            try {
                if (condition.getAsBoolean()) {
                    return true;
                }
            } catch (RuntimeException | LinkageError exception) {
                unregisterSmoothingBypass(condition);
                BetterCamera.LOGGER.error("Removed a failing camera smoothing bypass condition.", exception);
            }
        }
        return false;
    }

    private CameraCompatibility() {
    }
}
