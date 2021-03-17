package com.android.systemui.classifier;

public class DurationCountClassifier extends StrokeClassifier {
    public String getTag() {
        return "DUR";
    }

    public DurationCountClassifier(ClassifierData classifierData) {
    }

    public float getFalseTouchEvaluation(int i, Stroke stroke) {
        return DurationCountEvaluator.evaluate(stroke.getDurationSeconds() / ((float) stroke.getCount()));
    }
}
