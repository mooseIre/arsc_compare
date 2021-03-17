package com.android.systemui.classifier;

public class SpeedClassifier extends StrokeClassifier {
    @Override // com.android.systemui.classifier.Classifier
    public String getTag() {
        return "SPD";
    }

    public SpeedClassifier(ClassifierData classifierData) {
    }

    @Override // com.android.systemui.classifier.StrokeClassifier
    public float getFalseTouchEvaluation(int i, Stroke stroke) {
        float durationNanos = ((float) stroke.getDurationNanos()) / 1.0E9f;
        if (durationNanos == 0.0f) {
            return SpeedEvaluator.evaluate(0.0f);
        }
        return SpeedEvaluator.evaluate(stroke.getTotalLength() / durationNanos);
    }
}
