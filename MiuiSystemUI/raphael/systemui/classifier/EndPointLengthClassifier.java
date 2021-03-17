package com.android.systemui.classifier;

public class EndPointLengthClassifier extends StrokeClassifier {
    public String getTag() {
        return "END_LNGTH";
    }

    public EndPointLengthClassifier(ClassifierData classifierData) {
    }

    public float getFalseTouchEvaluation(int i, Stroke stroke) {
        return EndPointLengthEvaluator.evaluate(stroke.getEndPointLength());
    }
}
