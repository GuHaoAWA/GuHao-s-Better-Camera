package com.inspiration_mushroom.bc.client;

import net.minecraft.world.phys.Vec3;

public final class CameraPositionSmoother {
    private static final double FIVE_PERCENT_SETTLING_FACTOR = 4.743864518390577D;

    private final PositionAxis xAxis = new PositionAxis();
    private final PositionAxis yAxis = new PositionAxis();
    private final PositionAxis zAxis = new PositionAxis();
    private boolean initialized;

    public Vec3 update(Vec3 targetPosition, double deltaSeconds, double responseTime) {
        if (!isFinite(targetPosition)
                || !Double.isFinite(deltaSeconds)
                || deltaSeconds <= 0.0D
                || !Double.isFinite(responseTime)
                || responseTime <= 0.0D) {
            this.snap(targetPosition);
            return targetPosition;
        }

        if (!this.initialized) {
            this.snap(targetPosition);
            return targetPosition;
        }

        double angularFrequency = FIVE_PERCENT_SETTLING_FACTOR / responseTime;
        return new Vec3(
                this.xAxis.update(targetPosition.x, deltaSeconds, angularFrequency),
                this.yAxis.update(targetPosition.y, deltaSeconds, angularFrequency),
                this.zAxis.update(targetPosition.z, deltaSeconds, angularFrequency)
        );
    }

    public void snap(Vec3 position) {
        if (!isFinite(position)) {
            this.reset();
            return;
        }

        this.xAxis.snap(position.x);
        this.yAxis.snap(position.y);
        this.zAxis.snap(position.z);
        this.initialized = true;
    }

    public void reset() {
        this.xAxis.reset();
        this.yAxis.reset();
        this.zAxis.reset();
        this.initialized = false;
    }

    private static boolean isFinite(Vec3 position) {
        return Double.isFinite(position.x)
                && Double.isFinite(position.y)
                && Double.isFinite(position.z);
    }

    private static final class PositionAxis {
        private double position;
        private double velocity;
        private double targetPosition;

        private double update(double targetPosition, double deltaSeconds, double angularFrequency) {
            double normalizedTime = angularFrequency * deltaSeconds;
            double decay = Math.exp(-normalizedTime);
            double targetVelocity = (targetPosition - this.targetPosition) / deltaSeconds;
            double error = this.position - this.targetPosition;
            double shiftedError = error + 2.0D * targetVelocity / angularFrequency;
            double shiftedVelocity = this.velocity - targetVelocity;
            double coefficient = shiftedVelocity + angularFrequency * shiftedError;

            shiftedError = (shiftedError + coefficient * deltaSeconds) * decay;
            shiftedVelocity = (shiftedVelocity - angularFrequency * coefficient * deltaSeconds) * decay;
            this.position = targetPosition + shiftedError - 2.0D * targetVelocity / angularFrequency;
            this.velocity = shiftedVelocity + targetVelocity;
            this.targetPosition = targetPosition;

            if (!Double.isFinite(this.position) || !Double.isFinite(this.velocity)) {
                this.snap(targetPosition);
            } else if (Math.abs(this.position - targetPosition) < 1.0E-7D
                    && Math.abs(this.velocity) < 1.0E-6D) {
                this.snap(targetPosition);
            }

            return this.position;
        }

        private void snap(double position) {
            this.position = position;
            this.velocity = 0.0D;
            this.targetPosition = position;
        }

        private void reset() {
            this.position = 0.0D;
            this.velocity = 0.0D;
            this.targetPosition = 0.0D;
        }
    }
}
