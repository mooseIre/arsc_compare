package com.android.systemui.classifier;

public class EndPointRatioClassifier extends StrokeClassifier {
    public String getTag() {
        return "END_RTIO";
    }

    public EndPointRatioClassifier(ClassifierData classifierData) {
        this.mClassifierData = classifierData;
    }

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
