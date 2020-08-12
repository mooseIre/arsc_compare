package com.android.systemui.classifier;

public class LengthCountClassifier extends StrokeClassifier {
    public String getTag() {
        return "LEN_CNT";
    }

    public LengthCountClassifier(ClassifierData classifierData) {
    }

    public float getFalseTouchEvaluation(int i, Stroke stroke) {
        return LengthCountEvaluator.evaluate(stroke.getTotalLength() / Math.max(1.0f, (float) (stroke.getCount() - 2)));
    }
}
