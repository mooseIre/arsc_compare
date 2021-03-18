package com.android.keyguard.charge;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;
import android.view.OrientationListener;

public abstract class OrientationEventListenerWrapper {
    private boolean mEnabled;
    private OrientationListener mOldListener;
    private int mOrientation;
    private int mRate;
    private Sensor mSensor;
    private SensorEventListener mSensorEventListener;
    private SensorManager mSensorManager;

    public abstract void onOrientationChanged(int i);

    public OrientationEventListenerWrapper(Context context) {
        this(context, 3);
    }

    public OrientationEventListenerWrapper(Context context, int i) {
        this.mOrientation = -1;
        this.mEnabled = false;
        SensorManager sensorManager = (SensorManager) context.getSystemService("sensor");
        this.mSensorManager = sensorManager;
        this.mRate = i;
        Sensor defaultSensor = sensorManager.getDefaultSensor(1);
        this.mSensor = defaultSensor;
        if (defaultSensor != null) {
            this.mSensorEventListener = new SensorEventListenerImpl();
        }
    }

    public void enable() {
        Sensor sensor = this.mSensor;
        if (sensor == null) {
            Log.w("DeviceOrientationEventListener", "Cannot detect sensors. Not enabled");
        } else if (!this.mEnabled) {
            this.mSensorManager.registerListener(this.mSensorEventListener, sensor, this.mRate);
            this.mEnabled = true;
            this.mOrientation = -1;
        }
    }

    public void disable() {
        if (this.mSensor == null) {
            Log.w("DeviceOrientationEventListener", "Cannot detect sensors. Invalid disable");
        } else if (this.mEnabled) {
            this.mSensorManager.unregisterListener(this.mSensorEventListener);
            this.mEnabled = false;
        }
    }

    class SensorEventListenerImpl implements SensorEventListener {
        public void onAccuracyChanged(Sensor sensor, int i) {
        }

        SensorEventListenerImpl() {
        }

        public void onSensorChanged(SensorEvent sensorEvent) {
            int i;
            float[] fArr = sensorEvent.values;
            float f = -fArr[0];
            float f2 = -fArr[1];
            float f3 = -fArr[2];
            if (((f * f) + (f2 * f2)) * 4.0f >= f3 * f3) {
                i = 90 - Math.round(((float) Math.atan2((double) (-f2), (double) f)) * 57.29578f);
                while (i >= 360) {
                    i -= 360;
                }
                while (i < 0) {
                    i += 360;
                }
            } else {
                i = -1;
            }
            if (OrientationEventListenerWrapper.this.mOldListener != null) {
                OrientationEventListenerWrapper.this.mOldListener.onSensorChanged(1, sensorEvent.values);
            }
            if (i != OrientationEventListenerWrapper.this.mOrientation) {
                OrientationEventListenerWrapper.this.mOrientation = i;
                OrientationEventListenerWrapper.this.onOrientationChanged(i);
            }
        }
    }

    public boolean canDetectOrientation() {
        return this.mSensor != null;
    }
}
