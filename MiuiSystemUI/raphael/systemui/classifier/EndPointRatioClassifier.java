package com.android.systemui.classifier;

public class EndPointRatioClassifier extends StrokeClassifier {
    @Override // com.android.systemui.classifier.Classifier
    public String getTag() {
        return "END_RTIO";
    }

    public EndPointRatioClassifier(ClassifierData classifierData) {
        this.mClassifierData = classifierData;
    }

    @Override // com.android.systemui.classifier.StrokeClassifier
    public float getFalseTouchEvaluation(int i, Stroke stroke) {
        float f;
        if (stroke.getTotalLength() == 0.0f) {
            f = 1.0f;
        } else {
            f = stroke.getEndPointLength() / stroke.getTotalLength();
        }
        return EndPointRatioEvaluator.evaluate(f);
    }
}
