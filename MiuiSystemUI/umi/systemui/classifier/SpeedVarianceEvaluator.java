package com.android.systemui.classifier;

public class SpeedVarianceEvaluator {
    public static float evaluate(float f) {
        double d = (double) f;
        float f2 = d > 0.06d ? 1.0f : 0.0f;
        if (d > 0.15d) {
            f2 += 1.0f;
        }
        if (d > 0.3d) {
            f2 += 1.0f;
        }
        return d > 0.6d ? f2 + 1.0f : f2;
    }
}
