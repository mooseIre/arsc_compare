package com.android.systemui.classifier;

public class EndPointRatioEvaluator {
    public static float evaluate(float f) {
        double d = (double) f;
        float f2 = d < 0.85d ? 1.0f : 0.0f;
        if (d < 0.75d) {
            f2 += 1.0f;
        }
        if (d < 0.65d) {
            f2 += 1.0f;
        }
        if (d < 0.55d) {
            f2 += 1.0f;
        }
        if (d < 0.45d) {
            f2 += 1.0f;
        }
        return d < 0.35d ? f2 + 1.0f : f2;
    }
}
