package com.android.systemui.classifier;

import android.hardware.SensorEvent;
import android.view.MotionEvent;

public class ProximityClassifier extends GestureClassifier {
    private float mAverageNear;
    private long mGestureStartTimeNano;
    private boolean mNear;
    private long mNearDuration;
    private long mNearStartTimeNano;

    @Override // com.android.systemui.classifier.Classifier
    public String getTag() {
        return "PROX";
    }

    public ProximityClassifier(ClassifierData classifierData) {
    }

    @Override // com.android.systemui.classifier.Classifier
    public void onSensorChanged(SensorEvent sensorEvent) {
        if (sensorEvent.sensor.getType() == 8) {
            boolean z = false;
            if (sensorEvent.values[0] < sensorEvent.sensor.getMaximumRange()) {
                z = true;
            }
            update(z, sensorEvent.timestamp);
        }
    }

    @Override // com.android.systemui.classifier.Classifier
    public void onTouchEvent(MotionEvent motionEvent) {
        int actionMasked = motionEvent.getActionMasked();
        if (actionMasked == 0) {
            this.mGestureStartTimeNano = motionEvent.getEventTimeNano();
            this.mNearStartTimeNano = motionEvent.getEventTimeNano();
            this.mNearDuration = 0;
        }
        if (actionMasked == 1 || actionMasked == 3) {
            update(this.mNear, motionEvent.getEventTimeNano());
            long eventTimeNano = motionEvent.getEventTimeNano() - this.mGestureStartTimeNano;
            if (eventTimeNano == 0) {
                this.mAverageNear = this.mNear ? 1.0f : 0.0f;
            } else {
                this.mAverageNear = ((float) this.mNearDuration) / ((float) eventTimeNano);
            }
        }
    }

    private void update(boolean z, long j) {
        long j2 = this.mNearStartTimeNano;
        if (j > j2) {
            if (this.mNear) {
                this.mNearDuration += j - j2;
            }
            if (z) {
                this.mNearStartTimeNano = j;
            }
        }
        this.mNear = z;
    }

    @Override // com.android.systemui.classifier.GestureClassifier
    public float getFalseTouchEvaluation(int i) {
        return ProximityEvaluator.evaluate(this.mAverageNear, i);
    }
}
