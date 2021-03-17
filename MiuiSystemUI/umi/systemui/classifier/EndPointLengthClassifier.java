package com.android.systemui.classifier;

public class EndPointLengthClassifier extends StrokeClassifier {
    @Override // com.android.systemui.classifier.Classifier
    public String getTag() {
        return "END_LNGTH";
    }

    public EndPointLengthClassifier(ClassifierData classifierData) {
    }

    @Override // com.android.systemui.classifier.StrokeClassifier
    public float getFalseTouchEvaluation(int i, Stroke stroke) {
        return EndPointLengthEvaluator.evaluate(stroke.getEndPointLength());
    }
}
