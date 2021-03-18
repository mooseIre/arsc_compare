package com.android.systemui.classifier;

import android.view.MotionEvent;
import java.util.HashMap;

public class AccelerationClassifier extends StrokeClassifier {
    private final HashMap<Stroke, Data> mStrokeMap = new HashMap<>();

    @Override // com.android.systemui.classifier.Classifier
    public String getTag() {
        return "ACC";
    }

    public AccelerationClassifier(ClassifierData classifierData) {
        this.mClassifierData = classifierData;
    }

    @Override // com.android.systemui.classifier.Classifier
    public void onTouchEvent(MotionEvent motionEvent) {
        if (motionEvent.getActionMasked() == 0) {
            this.mStrokeMap.clear();
        }
        for (int i = 0; i < motionEvent.getPointerCount(); i++) {
            Stroke stroke = this.mClassifierData.getStroke(motionEvent.getPointerId(i));
            Point point = stroke.getPoints().get(stroke.getPoints().size() - 1);
            if (this.mStrokeMap.get(stroke) == null) {
                this.mStrokeMap.put(stroke, new Data(point));
            } else {
                this.mStrokeMap.get(stroke).addPoint(point);
            }
        }
    }

    @Override // com.android.systemui.classifier.StrokeClassifier
    public float getFalseTouchEvaluation(int i, Stroke stroke) {
        return SpeedRatioEvaluator.evaluate(this.mStrokeMap.get(stroke).maxSpeedRatio) * 2.0f;
    }

    private static class Data {
        float maxSpeedRatio = 0.0f;
        Point previousPoint;
        float previousSpeed = 0.0f;

        public Data(Point point) {
            this.previousPoint = point;
        }

        public void addPoint(Point point) {
            float dist = this.previousPoint.dist(point);
            float f = (float) ((point.timeOffsetNano - this.previousPoint.timeOffsetNano) + 1);
            float f2 = dist / f;
            if (f > 2.0E7f || f < 5000000.0f) {
                this.previousSpeed = 0.0f;
                this.previousPoint = point;
                return;
            }
            float f3 = this.previousSpeed;
            if (f3 != 0.0f) {
                this.maxSpeedRatio = Math.max(this.maxSpeedRatio, f2 / f3);
            }
            this.previousSpeed = f2;
            this.previousPoint = point;
        }
    }
}
