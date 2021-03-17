package com.android.systemui.statusbar.policy;

import android.content.Context;
import android.hardware.SensorPrivacyManager;
import com.android.systemui.statusbar.policy.SensorPrivacyController;
import java.util.ArrayList;
import java.util.List;

public class SensorPrivacyControllerImpl implements SensorPrivacyController, SensorPrivacyManager.OnSensorPrivacyChangedListener {
    private final List<SensorPrivacyController.OnSensorPrivacyChangedListener> mListeners;
    private Object mLock = new Object();
    private boolean mSensorPrivacyEnabled;
    private SensorPrivacyManager mSensorPrivacyManager;

    public SensorPrivacyControllerImpl(Context context) {
        SensorPrivacyManager sensorPrivacyManager = (SensorPrivacyManager) context.getSystemService("sensor_privacy");
        this.mSensorPrivacyManager = sensorPrivacyManager;
        this.mSensorPrivacyEnabled = sensorPrivacyManager.isSensorPrivacyEnabled();
        this.mSensorPrivacyManager.addSensorPrivacyListener(this);
        this.mListeners = new ArrayList(1);
    }

    @Override // com.android.systemui.statusbar.policy.SensorPrivacyController
    public boolean isSensorPrivacyEnabled() {
        boolean z;
        synchronized (this.mLock) {
            z = this.mSensorPrivacyEnabled;
        }
        return z;
    }

    public void addCallback(SensorPrivacyController.OnSensorPrivacyChangedListener onSensorPrivacyChangedListener) {
        synchronized (this.mLock) {
            this.mListeners.add(onSensorPrivacyChangedListener);
            notifyListenerLocked(onSensorPrivacyChangedListener);
        }
    }

    public void removeCallback(SensorPrivacyController.OnSensorPrivacyChangedListener onSensorPrivacyChangedListener) {
        synchronized (this.mLock) {
            this.mListeners.remove(onSensorPrivacyChangedListener);
        }
    }

    public void onSensorPrivacyChanged(boolean z) {
        synchronized (this.mLock) {
            this.mSensorPrivacyEnabled = z;
            for (SensorPrivacyController.OnSensorPrivacyChangedListener onSensorPrivacyChangedListener : this.mListeners) {
                notifyListenerLocked(onSensorPrivacyChangedListener);
            }
        }
    }

    private void notifyListenerLocked(SensorPrivacyController.OnSensorPrivacyChangedListener onSensorPrivacyChangedListener) {
        onSensorPrivacyChangedListener.onSensorPrivacyChanged(this.mSensorPrivacyEnabled);
    }
}
