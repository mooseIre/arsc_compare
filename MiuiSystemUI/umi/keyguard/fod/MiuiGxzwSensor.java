package com.android.keyguard.fod;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import com.android.systemui.C0010R$bool;

/* access modifiers changed from: package-private */
public class MiuiGxzwSensor {
    private static int TYPE_NONUI_SENSOR = 33171027;
    private static int TYPE_PUT_UP_DETECT = 33171030;
    private final Context mContext;
    private final Handler mHandler = new Handler(Looper.getMainLooper());
    private MiuiGxzwSensorListener mMiuiGxzwSensorListener;
    private final SensorEventListener mNonUIListener = new SensorEventListener() {
        /* class com.android.keyguard.fod.MiuiGxzwSensor.AnonymousClass2 */

        public void onAccuracyChanged(Sensor sensor, int i) {
        }

        public void onSensorChanged(SensorEvent sensorEvent) {
            if (sensorEvent != null && MiuiGxzwSensor.this.mMiuiGxzwSensorListener != null) {
                float[] fArr = sensorEvent.values;
                if (fArr[0] == 0.0f) {
                    MiuiGxzwSensor.this.mMiuiGxzwSensorListener.onEixtNonUI((int) sensorEvent.values[0]);
                } else if (fArr[0] == 1.0f || fArr[0] == 2.0f) {
                    MiuiGxzwSensor.this.mMiuiGxzwSensorListener.onEnterNonUI((int) sensorEvent.values[0]);
                } else {
                    Log.w("MiuiGxzwSensor", "event.values[0] = " + sensorEvent.values[0]);
                }
            }
        }
    };
    private final SensorEventListener mPutUpSensorListener = new SensorEventListener() {
        /* class com.android.keyguard.fod.MiuiGxzwSensor.AnonymousClass1 */

        public void onAccuracyChanged(Sensor sensor, int i) {
        }

        public void onSensorChanged(SensorEvent sensorEvent) {
            if (sensorEvent != null && MiuiGxzwSensor.this.mMiuiGxzwSensorListener != null) {
                float[] fArr = sensorEvent.values;
                if (fArr[0] == 1.0f) {
                    MiuiGxzwSensor.this.mMiuiGxzwSensorListener.onDeviceMove();
                } else if (fArr[0] == 2.0f) {
                    MiuiGxzwSensor.this.mMiuiGxzwSensorListener.onDeviceStable();
                } else if (fArr[0] == 3.0f) {
                    MiuiGxzwSensor.this.mMiuiGxzwSensorListener.onDevicePutUp();
                } else {
                    Log.w("MiuiGxzwSensor", "event.values[0] = " + sensorEvent.values[0]);
                }
            }
        }
    };
    private final SensorManager mSensorManager;
    private final boolean mSupportNonuiSensor;

    /* access modifiers changed from: package-private */
    public interface MiuiGxzwSensorListener {
        void onDeviceMove();

        void onDevicePutUp();

        void onDeviceStable();

        void onEixtNonUI(int i);

        void onEnterNonUI(int i);
    }

    MiuiGxzwSensor(Context context) {
        this.mContext = context;
        this.mSensorManager = (SensorManager) context.getSystemService("sensor");
        this.mSupportNonuiSensor = isSupportNonuiSensor();
    }

    public void registerDozeSensor(MiuiGxzwSensorListener miuiGxzwSensorListener) {
        if (this.mSensorManager == null) {
            Log.e("MiuiGxzwSensor", "sensor not supported");
            return;
        }
        this.mMiuiGxzwSensorListener = miuiGxzwSensorListener;
        if (MiuiGxzwUtils.isFodAodShowEnable(this.mContext)) {
            AsyncTask.THREAD_POOL_EXECUTOR.execute(new Runnable() {
                /* class com.android.keyguard.fod.$$Lambda$MiuiGxzwSensor$NwWCpZOTkkfzWBURlu7NhW1soPA */

                public final void run() {
                    MiuiGxzwSensor.lambda$NwWCpZOTkkfzWBURlu7NhW1soPA(MiuiGxzwSensor.this);
                }
            });
        }
    }

    private boolean isSupportNonuiSensor() {
        SensorManager sensorManager = this.mSensorManager;
        if (sensorManager == null) {
            Log.e("MiuiGxzwSensor", "sensor not supported");
            return false;
        } else if (sensorManager.getDefaultSensor(TYPE_NONUI_SENSOR) == null) {
            return false;
        } else {
            if (this.mContext.getResources().getBoolean(C0010R$bool.config_enableFodNonuiSensor) || MiuiGxzwUtils.isSupportNonuiSensor()) {
                return true;
            }
            return false;
        }
    }

    public void unregisterDozeSensor() {
        this.mMiuiGxzwSensorListener = null;
        if (this.mSensorManager == null) {
            Log.e("MiuiGxzwSensor", "sensor not supported");
        } else {
            AsyncTask.THREAD_POOL_EXECUTOR.execute(new Runnable() {
                /* class com.android.keyguard.fod.$$Lambda$MiuiGxzwSensor$tytcQllx08Mrvnkbc0VdDWjTliQ */

                public final void run() {
                    MiuiGxzwSensor.lambda$tytcQllx08Mrvnkbc0VdDWjTliQ(MiuiGxzwSensor.this);
                }
            });
        }
    }

    /* access modifiers changed from: private */
    public void doRegisterDozeSensor() {
        Sensor defaultSensor = this.mSensorManager.getDefaultSensor(TYPE_PUT_UP_DETECT, true);
        if (defaultSensor != null) {
            this.mSensorManager.registerListener(this.mPutUpSensorListener, defaultSensor, 3, this.mHandler);
        } else {
            Log.e("MiuiGxzwSensor", "no put up sensor");
        }
        if (this.mSupportNonuiSensor) {
            Sensor defaultSensor2 = this.mSensorManager.getDefaultSensor(TYPE_NONUI_SENSOR, true);
            if (defaultSensor2 != null) {
                this.mSensorManager.registerListener(this.mNonUIListener, defaultSensor2, 3, this.mHandler);
            } else {
                Log.e("MiuiGxzwSensor", "no nonui sensor");
            }
        }
    }

    /* access modifiers changed from: private */
    public void doUnregisterSensor() {
        this.mSensorManager.unregisterListener(this.mPutUpSensorListener);
        this.mSensorManager.unregisterListener(this.mNonUIListener);
    }
}
