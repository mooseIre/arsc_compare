package com.android.systemui.classifier;

public class DirectionEvaluator {
    public static float evaluate(float f, float f2, int i) {
        boolean z = Math.abs(f2) >= Math.abs(f);
        if (i != 0) {
            if (i == 1) {
                return z ? 5.5f : 0.0f;
            }
            if (i != 2) {
                return i != 4 ? i != 5 ? (i == 6 && ((double) f) > 0.0d && ((double) f2) > 0.0d) ? 5.5f : 0.0f : (((double) f) >= 0.0d || ((double) f2) <= 0.0d) ? 0.0f : 5.5f : (!z || ((double) f2) >= 0.0d) ? 5.5f : 0.0f;
            }
        }
        return (!z || ((double) f2) <= 0.0d) ? 5.5f : 0.0f;
    }
}
