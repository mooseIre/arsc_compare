package com.android.systemui.classifier;

public class PointerCountEvaluator {
    public static float evaluate(int i) {
        int i2 = i - 1;
        return (float) (i2 * i2);
    }
}
