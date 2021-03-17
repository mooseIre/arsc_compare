package com.android.systemui.classifier;

public class SpeedClassifier extends StrokeClassifier {
    public String getTag() {
        return "SPD";
    }

    public SpeedClassifier(ClassifierData classifierData) {
    }

    public float getFalseTouchEvaluation(int i, Stroke stroke) {
        float durationNanos = ((float) stroke.getDurationNanos()) / 1.0E9f;
        if (durationNanos == 0.0f) {
            return SpeedEvaluator.evaluate(0.0f);
        }
        return SpeedEvaluator.evaluate(stroke.getTotalLength() / durationNanos);
    }
}
