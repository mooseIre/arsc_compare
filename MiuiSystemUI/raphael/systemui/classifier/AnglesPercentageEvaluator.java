package com.android.systemui.classifier;

public class AnglesPercentageEvaluator {
    public static float evaluate(float f, int i) {
        boolean z = i == 8;
        float f2 = 0.0f;
        double d = (double) f;
        if (d < 1.0d && !z) {
            f2 = 1.0f;
        }
        if (d < 0.9d && !z) {
            f2 += 1.0f;
        }
        return d < 0.7d ? f2 + 1.0f : f2;
    }
}
