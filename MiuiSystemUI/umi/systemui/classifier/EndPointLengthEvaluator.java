package com.android.systemui.classifier;

public class EndPointLengthEvaluator {
    public static float evaluate(float f) {
        double d = (double) f;
        float f2 = 0.0f;
        if (d < 0.05d) {
            f2 = (float) (((double) 0.0f) + 2.0d);
        }
        if (d < 0.1d) {
            f2 = (float) (((double) f2) + 2.0d);
        }
        if (d < 0.2d) {
            f2 = (float) (((double) f2) + 2.0d);
        }
        if (d < 0.3d) {
            f2 = (float) (((double) f2) + 2.0d);
        }
        if (d < 0.4d) {
            f2 = (float) (((double) f2) + 2.0d);
        }
        return d < 0.5d ? (float) (((double) f2) + 2.0d) : f2;
    }
}
