package com.guhao.bc.client;

public final class CameraAxisSmoother {
    private final VelocityChannel fastChannel = new VelocityChannel();
    private final VelocityChannel bodyChannel = new VelocityChannel();

    public double update(
            double inputDelta,
            double deltaSeconds,
            double fastResponseTime,
            double bodyResponseTime,
            double fastResponseWeight
    ) {
        if (!Double.isFinite(inputDelta) || !Double.isFinite(deltaSeconds) || deltaSeconds <= 0.0D) {
            this.reset();
            return 0.0D;
        }

        double targetVelocity = inputDelta / deltaSeconds;
        double weight = clamp(fastResponseWeight, 0.0D, 1.0D);
        double fastDelta = this.fastChannel.update(targetVelocity, deltaSeconds, fastResponseTime);
        double bodyDelta = this.bodyChannel.update(targetVelocity, deltaSeconds, bodyResponseTime);
        return fastDelta * weight + bodyDelta * (1.0D - weight);
    }

    public void reset() {
        this.fastChannel.reset();
        this.bodyChannel.reset();
    }

    private static double clamp(double value, double minimum, double maximum) {
        return Math.max(minimum, Math.min(maximum, value));
    }

    private static final class VelocityChannel {
        private static final double FIVE_PERCENT_SETTLING_FACTOR = 4.743864518390577D;

        private double velocity;
        private double acceleration;

        private double update(double targetVelocity, double deltaSeconds, double responseTime) {
            if (!Double.isFinite(targetVelocity) || !Double.isFinite(responseTime) || responseTime <= 0.0D) {
                this.reset();
                return targetVelocity * deltaSeconds;
            }

            double angularFrequency = FIVE_PERCENT_SETTLING_FACTOR / responseTime;
            double normalizedTime = angularFrequency * deltaSeconds;
            double decay = Math.exp(-normalizedTime);
            double oneMinusDecay = -Math.expm1(-normalizedTime);
            double error = this.velocity - targetVelocity;
            double coefficient = this.acceleration + angularFrequency * error;
            double displacement = targetVelocity * deltaSeconds
                    + error * oneMinusDecay / angularFrequency
                    + coefficient * (oneMinusDecay - normalizedTime * decay)
                    / (angularFrequency * angularFrequency);

            this.velocity = targetVelocity + (error + coefficient * deltaSeconds) * decay;
            this.acceleration = (this.acceleration - angularFrequency * coefficient * deltaSeconds) * decay;

            if (!Double.isFinite(displacement)
                    || !Double.isFinite(this.velocity)
                    || !Double.isFinite(this.acceleration)) {
                this.reset();
                return 0.0D;
            }

            if (Math.abs(targetVelocity) < 1.0E-5D
                    && Math.abs(this.velocity) < 1.0E-5D
                    && Math.abs(this.acceleration) < 1.0E-3D) {
                this.reset();
            }

            return displacement;
        }

        private void reset() {
            this.velocity = 0.0D;
            this.acceleration = 0.0D;
        }
    }
}
