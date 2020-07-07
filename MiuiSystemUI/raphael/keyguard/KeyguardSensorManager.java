package com.android.keyguard;

import android.content.Context;
import android.hardware.SensorManager;
import android.os.Handler;
import com.android.keyguard.analytics.AnalyticsHelper;
import miui.util.ProximitySensorWrapper;

public class KeyguardSensorManager {
    private static volatile KeyguardSensorManager sKeyguardSensorManager;
    /* access modifiers changed from: private */
    public Context mContext;
    private Handler mHandler = new Handler();
    /* access modifiers changed from: private */
    public ProximitySensorChangeCallback mProximitySensorChangeCallback = null;
    private ProximitySensorWrapper mProximitySensorWrapper = null;
    private final ProximitySensorWrapper.ProximitySensorChangeListener mSensorListener = new ProximitySensorWrapper.ProximitySensorChangeListener() {
        public void onSensorChanged(boolean z) {
            if (KeyguardSensorManager.this.mProximitySensorChangeCallback != null) {
                AnalyticsHelper.getInstance(KeyguardSensorManager.this.mContext).recordKeyguardProximitySensor(z);
                KeyguardSensorManager.this.mProximitySensorChangeCallback.onChange(z);
            }
            KeyguardSensorManager.this.unregisterProximitySensor();
        }
    };
    private SensorManager mSensorManager = null;
    private Runnable mUnregisterProximitySensorRunnable = new Runnable() {
        public void run() {
            KeyguardSensorManager.this.unregisterProximitySensor();
        }
    };

    public interface ProximitySensorChangeCallback {
        void onChange(boolean z);
    }

    private KeyguardSensorManager() {
    }

    private KeyguardSensorManager(Context context) {
        this.mContext = context;
        this.mSensorManager = (SensorManager) context.getSystemService("sensor");
    }

    public static KeyguardSensorManager getInstance(Context context) {
        if (sKeyguardSensorManager == null) {
            synchronized (KeyguardSensorManager.class) {
                if (sKeyguardSensorManager == null) {
                    sKeyguardSensorManager = new KeyguardSensorManager(context);
                }
            }
        }
        return sKeyguardSensorManager;
    }

    public void registerProximitySensor(ProximitySensorChangeCallback proximitySensorChangeCallback) {
        if (this.mProximitySensorWrapper == null) {
            this.mProximitySensorWrapper = new ProximitySensorWrapper(this.mContext);
            this.mProximitySensorWrapper.registerListener(this.mSensorListener);
            this.mProximitySensorChangeCallback = proximitySensorChangeCallback;
            this.mHandler.postDelayed(this.mUnregisterProximitySensorRunnable, 2000);
        }
    }

    public void unregisterProximitySensor() {
        if (this.mProximitySensorWrapper != null) {
            this.mHandler.removeCallbacks(this.mUnregisterProximitySensorRunnable);
            this.mProximitySensorWrapper.unregisterAllListeners();
            this.mProximitySensorWrapper = null;
            this.mProximitySensorChangeCallback = null;
        }
    }
}
