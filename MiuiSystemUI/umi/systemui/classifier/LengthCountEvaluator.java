package com.android.systemui.classifier;

public class LengthCountEvaluator {
    public static float evaluate(float f) {
        double d = (double) f;
        float f2 = d < 0.09d ? 1.0f : 0.0f;
        if (d < 0.05d) {
            f2 += 1.0f;
        }
        if (d < 0.02d) {
            f2 += 1.0f;
        }
        if (d > 0.6d) {
            f2 += 1.0f;
        }
        if (d > 0.9d) {
            f2 += 1.0f;
        }
        return d > 1.2d ? f2 + 1.0f : f2;
    }
}
