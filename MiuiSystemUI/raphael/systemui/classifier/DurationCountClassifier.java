package com.android.systemui.classifier;

public class DurationCountClassifier extends StrokeClassifier {
    @Override // com.android.systemui.classifier.Classifier
    public String getTag() {
        return "DUR";
    }

    public DurationCountClassifier(ClassifierData classifierData) {
    }

    @Override // com.android.systemui.classifier.StrokeClassifier
    public float getFalseTouchEvaluation(int i, Stroke stroke) {
        return DurationCountEvaluator.evaluate(stroke.getDurationSeconds() / ((float) stroke.getCount()));
    }
}
