package com.android.systemui.util.sensors;

import android.content.res.Resources;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.util.Log;
import com.android.internal.annotations.VisibleForTesting;
import com.android.systemui.C0012R$dimen;
import com.android.systemui.C0021R$string;
import com.android.systemui.util.concurrency.DelayableExecutor;
import com.android.systemui.util.sensors.ProximitySensor;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

public class ProximitySensor {
    private static final boolean DEBUG = Log.isLoggable("ProxSensor", 3);
    private final AtomicBoolean mAlerting;
    @VisibleForTesting
    ProximityEvent mLastEvent;
    private List<ProximitySensorListener> mListeners = new ArrayList();
    @VisibleForTesting
    protected boolean mPaused;
    private boolean mRegistered;
    private final Sensor mSensor;
    private int mSensorDelay;
    private SensorEventListener mSensorEventListener;
    private final AsyncSensorManager mSensorManager;
    private String mTag;
    private final float mThreshold;

    public interface ProximitySensorListener {
        void onSensorEvent(ProximityEvent proximityEvent);
    }

    public ProximitySensor(Resources resources, AsyncSensorManager asyncSensorManager) {
        Sensor sensor = null;
        this.mTag = null;
        this.mSensorDelay = 3;
        this.mAlerting = new AtomicBoolean();
        this.mSensorEventListener = new SensorEventListener() {
            /* class com.android.systemui.util.sensors.ProximitySensor.AnonymousClass1 */

            public void onAccuracyChanged(Sensor sensor, int i) {
            }

            public synchronized void onSensorChanged(SensorEvent sensorEvent) {
                ProximitySensor.this.onSensorEvent(sensorEvent);
            }
        };
        this.mSensorManager = asyncSensorManager;
        Sensor findCustomProxSensor = findCustomProxSensor(resources);
        float f = 0.0f;
        if (findCustomProxSensor != null) {
            try {
                f = getCustomProxThreshold(resources);
            } catch (IllegalStateException e) {
                Log.e("ProxSensor", "Can not load custom proximity sensor.", e);
            }
        }
        sensor = findCustomProxSensor;
        if (sensor == null && (sensor = asyncSensorManager.getDefaultSensor(8)) != null) {
            f = sensor.getMaximumRange();
        }
        this.mThreshold = f;
        this.mSensor = sensor;
    }

    public void setTag(String str) {
        this.mTag = str;
    }

    public void setSensorDelay(int i) {
        this.mSensorDelay = i;
    }

    public void pause() {
        this.mPaused = true;
        unregisterInternal();
    }

    public void resume() {
        this.mPaused = false;
        registerInternal();
    }

    private Sensor findCustomProxSensor(Resources resources) {
        String string = resources.getString(C0021R$string.proximity_sensor_type);
        if (string.isEmpty()) {
            return null;
        }
        for (Sensor sensor : this.mSensorManager.getSensorList(-1)) {
            if (string.equals(sensor.getStringType())) {
                return sensor;
            }
        }
        return null;
    }

    private float getCustomProxThreshold(Resources resources) {
        try {
            return resources.getFloat(C0012R$dimen.proximity_sensor_threshold);
        } catch (Resources.NotFoundException unused) {
            throw new IllegalStateException("R.dimen.proximity_sensor_threshold must be set.");
        }
    }

    public boolean isRegistered() {
        return this.mRegistered;
    }

    public boolean getSensorAvailable() {
        return this.mSensor != null;
    }

    public boolean register(ProximitySensorListener proximitySensorListener) {
        if (!getSensorAvailable()) {
            return false;
        }
        if (this.mListeners.contains(proximitySensorListener)) {
            Log.d("ProxSensor", "ProxListener registered multiple times: " + proximitySensorListener);
        } else {
            this.mListeners.add(proximitySensorListener);
        }
        registerInternal();
        return true;
    }

    /* access modifiers changed from: protected */
    public void registerInternal() {
        if (!this.mRegistered && !this.mPaused && !this.mListeners.isEmpty()) {
            logDebug("Registering sensor listener");
            this.mRegistered = true;
            this.mSensorManager.registerListener(this.mSensorEventListener, this.mSensor, this.mSensorDelay);
        }
    }

    public void unregister(ProximitySensorListener proximitySensorListener) {
        this.mListeners.remove(proximitySensorListener);
        if (this.mListeners.size() == 0) {
            unregisterInternal();
        }
    }

    /* access modifiers changed from: protected */
    public void unregisterInternal() {
        if (this.mRegistered) {
            logDebug("unregistering sensor listener");
            this.mSensorManager.unregisterListener(this.mSensorEventListener);
            this.mRegistered = false;
        }
    }

    public Boolean isNear() {
        ProximityEvent proximityEvent;
        if (!getSensorAvailable() || (proximityEvent = this.mLastEvent) == null) {
            return null;
        }
        return Boolean.valueOf(proximityEvent.getNear());
    }

    public void alertListeners() {
        if (!this.mAlerting.getAndSet(true)) {
            new ArrayList(this.mListeners).forEach(new Consumer() {
                /* class com.android.systemui.util.sensors.$$Lambda$ProximitySensor$ghFL7mqmC5TPLUcAxsPYh6a_M */

                @Override // java.util.function.Consumer
                public final void accept(Object obj) {
                    ProximitySensor.this.lambda$alertListeners$0$ProximitySensor((ProximitySensor.ProximitySensorListener) obj);
                }
            });
            this.mAlerting.set(false);
        }
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$alertListeners$0 */
    public /* synthetic */ void lambda$alertListeners$0$ProximitySensor(ProximitySensorListener proximitySensorListener) {
        proximitySensorListener.onSensorEvent(this.mLastEvent);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void onSensorEvent(SensorEvent sensorEvent) {
        boolean z = false;
        if (sensorEvent.values[0] < this.mThreshold) {
            z = true;
        }
        this.mLastEvent = new ProximityEvent(z, sensorEvent.timestamp);
        alertListeners();
    }

    public String toString() {
        return String.format("{registered=%s, paused=%s, near=%s, sensor=%s}", Boolean.valueOf(isRegistered()), Boolean.valueOf(this.mPaused), isNear(), this.mSensor);
    }

    public static class ProximityCheck implements Runnable {
        private List<Consumer<Boolean>> mCallbacks = new ArrayList();
        private final DelayableExecutor mDelayableExecutor;
        private final ProximitySensorListener mListener;
        private final AtomicBoolean mRegistered = new AtomicBoolean();
        private final ProximitySensor mSensor;

        public ProximityCheck(ProximitySensor proximitySensor, DelayableExecutor delayableExecutor) {
            this.mSensor = proximitySensor;
            proximitySensor.setTag("prox_check");
            this.mDelayableExecutor = delayableExecutor;
            this.mListener = new ProximitySensorListener() {
                /* class com.android.systemui.util.sensors.$$Lambda$ProximitySensor$ProximityCheck$VWMwluPHAE8LZl7uid6iAJaZ0zg */

                @Override // com.android.systemui.util.sensors.ProximitySensor.ProximitySensorListener
                public final void onSensorEvent(ProximitySensor.ProximityEvent proximityEvent) {
                    ProximitySensor.ProximityCheck.this.onProximityEvent(proximityEvent);
                }
            };
        }

        public void run() {
            unregister();
            this.mSensor.alertListeners();
        }

        public void check(long j, Consumer<Boolean> consumer) {
            if (!this.mSensor.getSensorAvailable()) {
                consumer.accept(null);
            }
            this.mCallbacks.add(consumer);
            if (!this.mRegistered.getAndSet(true)) {
                this.mSensor.register(this.mListener);
                this.mDelayableExecutor.executeDelayed(this, j);
            }
        }

        private void unregister() {
            this.mSensor.unregister(this.mListener);
            this.mRegistered.set(false);
        }

        /* access modifiers changed from: private */
        public void onProximityEvent(ProximityEvent proximityEvent) {
            this.mCallbacks.forEach(new Consumer() {
                /* class com.android.systemui.util.sensors.$$Lambda$ProximitySensor$ProximityCheck$ruTS1Tk02_hYvk7mh0KkebUZDkE */

                @Override // java.util.function.Consumer
                public final void accept(Object obj) {
                    ProximitySensor.ProximityCheck.lambda$onProximityEvent$0(ProximitySensor.ProximityEvent.this, (Consumer) obj);
                }
            });
            this.mCallbacks.clear();
            unregister();
            this.mRegistered.set(false);
        }

        static /* synthetic */ void lambda$onProximityEvent$0(ProximityEvent proximityEvent, Consumer consumer) {
            consumer.accept(proximityEvent == null ? null : Boolean.valueOf(proximityEvent.getNear()));
        }
    }

    public static class ProximityEvent {
        private final boolean mNear;
        private final long mTimestampNs;

        public ProximityEvent(boolean z, long j) {
            this.mNear = z;
            this.mTimestampNs = j;
        }

        public boolean getNear() {
            return this.mNear;
        }

        public long getTimestampNs() {
            return this.mTimestampNs;
        }

        public String toString() {
            return String.format(null, "{near=%s, timestamp_ns=%d}", Boolean.valueOf(this.mNear), Long.valueOf(this.mTimestampNs));
        }
    }

    private void logDebug(String str) {
        String str2;
        if (DEBUG) {
            StringBuilder sb = new StringBuilder();
            if (this.mTag != null) {
                str2 = "[" + this.mTag + "] ";
            } else {
                str2 = "";
            }
            sb.append(str2);
            sb.append(str);
            Log.d("ProxSensor", sb.toString());
        }
    }
}
