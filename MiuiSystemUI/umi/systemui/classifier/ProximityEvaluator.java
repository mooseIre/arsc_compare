package com.android.systemui.classifier;

public class ProximityEvaluator {
    public static float evaluate(float f, int i) {
        if (f >= (i == 0 ? 1.0f : 0.1f)) {
            return (float) (((double) 0.0f) + 2.0d);
        }
        return 0.0f;
    }
}
