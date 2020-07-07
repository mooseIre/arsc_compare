package com.android.systemui.classifier;

public class DurationCountEvaluator {
    public static float evaluate(float f) {
        double d = (double) f;
        float f2 = d < 0.0105d ? 1.0f : 0.0f;
        if (d < 0.00909d) {
            f2 += 1.0f;
        }
        if (d < 0.00667d) {
            f2 += 1.0f;
        }
        if (d > 0.0333d) {
            f2 += 1.0f;
        }
        return d > 0.05d ? f2 + 1.0f : f2;
    }
}
