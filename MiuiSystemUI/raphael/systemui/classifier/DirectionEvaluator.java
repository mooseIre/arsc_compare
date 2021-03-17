package com.android.systemui.classifier;

public class DirectionEvaluator {
    public static float evaluate(float f, float f2, int i) {
        boolean z = Math.abs(f2) >= Math.abs(f);
        if (i != 0) {
            if (i == 1) {
                return z ? 5.5f : 0.0f;
            }
            if (i != 2) {
                if (i != 4) {
                    if (i == 5) {
                        return (((double) f) >= 0.0d || ((double) f2) <= 0.0d) ? 0.0f : 5.5f;
                    }
                    if (i == 6) {
                        return (((double) f) <= 0.0d || ((double) f2) <= 0.0d) ? 0.0f : 5.5f;
                    }
                    if (i != 8) {
                        if (i != 9) {
                            return 0.0f;
                        }
                    }
                }
                return (!z || ((double) f2) >= 0.0d) ? 5.5f : 0.0f;
            }
        }
        return (!z || ((double) f2) <= 0.0d) ? 5.5f : 0.0f;
    }
}
