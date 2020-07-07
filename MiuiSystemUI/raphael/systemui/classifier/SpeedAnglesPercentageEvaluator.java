package com.android.systemui.classifier;

public class SpeedAnglesPercentageEvaluator {
    public static float evaluate(float f) {
        double d = (double) f;
        float f2 = d < 1.0d ? 1.0f : 0.0f;
        if (d < 0.9d) {
            f2 += 1.0f;
        }
        return d < 0.7d ? f2 + 1.0f : f2;
    }
}
