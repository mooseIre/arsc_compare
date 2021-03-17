package com.android.systemui.classifier;

import android.hardware.SensorEvent;
import android.view.MotionEvent;

public abstract class Classifier {
    protected ClassifierData mClassifierData;

    public abstract String getTag();

    public void onSensorChanged(SensorEvent sensorEvent) {
    }

    public void onTouchEvent(MotionEvent motionEvent) {
    }
}
