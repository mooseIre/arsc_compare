package com.android.systemui.util.sensors;

import android.content.Context;
import android.hardware.HardwareBuffer;
import android.hardware.Sensor;
import android.hardware.SensorAdditionalInfo;
import android.hardware.SensorDirectChannel;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.hardware.TriggerEventListener;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.MemoryFile;
import android.util.Log;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.util.Preconditions;
import com.android.systemui.plugins.PluginListener;
import com.android.systemui.plugins.SensorManagerPlugin;
import com.android.systemui.shared.plugins.PluginManager;
import java.util.ArrayList;
import java.util.List;

public class AsyncSensorManager extends SensorManager implements PluginListener<SensorManagerPlugin> {
    private final Handler mHandler;
    private final SensorManager mInner;
    private final List<SensorManagerPlugin> mPlugins;
    private final List<Sensor> mSensorCache;

    public void removeSensorDisableApp(String str) {
    }

    public void setSensorDisableApp(String str) {
    }

    public AsyncSensorManager(Context context, PluginManager pluginManager) {
        this((SensorManager) context.getSystemService(SensorManager.class), pluginManager, null);
    }

    @VisibleForTesting
    public AsyncSensorManager(SensorManager sensorManager, PluginManager pluginManager, Handler handler) {
        this.mInner = sensorManager;
        if (handler == null) {
            HandlerThread handlerThread = new HandlerThread("async_sensor");
            handlerThread.start();
            this.mHandler = new Handler(handlerThread.getLooper());
        } else {
            this.mHandler = handler;
        }
        this.mSensorCache = this.mInner.getSensorList(-1);
        this.mPlugins = new ArrayList();
        if (pluginManager != null) {
            pluginManager.addPluginListener((PluginListener) this, SensorManagerPlugin.class, true);
        }
    }

    /* access modifiers changed from: protected */
    public List<Sensor> getFullSensorList() {
        return this.mSensorCache;
    }

    /* access modifiers changed from: protected */
    public List<Sensor> getFullDynamicSensorList() {
        return this.mInner.getSensorList(-1);
    }

    /* access modifiers changed from: protected */
    public boolean registerListenerImpl(SensorEventListener sensorEventListener, Sensor sensor, int i, Handler handler, int i2, int i3) {
        if (sensor == null) {
            Log.e("AsyncSensorManager", "sensor cannot be null \n" + Log.getStackTraceString(new Throwable()));
            return false;
        }
        this.mHandler.post(new Runnable(sensor, sensorEventListener, i, i2, handler) {
            /* class com.android.systemui.util.sensors.$$Lambda$AsyncSensorManager$l441pTGMx1gou2inAUrhn1G5pgI */
            public final /* synthetic */ Sensor f$1;
            public final /* synthetic */ SensorEventListener f$2;
            public final /* synthetic */ int f$3;
            public final /* synthetic */ int f$4;
            public final /* synthetic */ Handler f$5;

            {
                this.f$1 = r2;
                this.f$2 = r3;
                this.f$3 = r4;
                this.f$4 = r5;
                this.f$5 = r6;
            }

            public final void run() {
                AsyncSensorManager.this.lambda$registerListenerImpl$0$AsyncSensorManager(this.f$1, this.f$2, this.f$3, this.f$4, this.f$5);
            }
        });
        return true;
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$registerListenerImpl$0 */
    public /* synthetic */ void lambda$registerListenerImpl$0$AsyncSensorManager(Sensor sensor, SensorEventListener sensorEventListener, int i, int i2, Handler handler) {
        if (sensor == null) {
            Log.e("AsyncSensorManager", "sensor cannot be null");
        }
        if (!this.mInner.registerListener(sensorEventListener, sensor, i, i2, handler)) {
            Log.e("AsyncSensorManager", "Registering " + sensorEventListener + " for " + sensor + " failed.");
        }
    }

    /* access modifiers changed from: protected */
    public boolean flushImpl(SensorEventListener sensorEventListener) {
        return this.mInner.flush(sensorEventListener);
    }

    /* access modifiers changed from: protected */
    public SensorDirectChannel createDirectChannelImpl(MemoryFile memoryFile, HardwareBuffer hardwareBuffer) {
        throw new UnsupportedOperationException("not implemented");
    }

    /* access modifiers changed from: protected */
    public void destroyDirectChannelImpl(SensorDirectChannel sensorDirectChannel) {
        throw new UnsupportedOperationException("not implemented");
    }

    /* access modifiers changed from: protected */
    public int configureDirectChannelImpl(SensorDirectChannel sensorDirectChannel, Sensor sensor, int i) {
        throw new UnsupportedOperationException("not implemented");
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$registerDynamicSensorCallbackImpl$1 */
    public /* synthetic */ void lambda$registerDynamicSensorCallbackImpl$1$AsyncSensorManager(SensorManager.DynamicSensorCallback dynamicSensorCallback, Handler handler) {
        this.mInner.registerDynamicSensorCallback(dynamicSensorCallback, handler);
    }

    /* access modifiers changed from: protected */
    public void registerDynamicSensorCallbackImpl(SensorManager.DynamicSensorCallback dynamicSensorCallback, Handler handler) {
        this.mHandler.post(new Runnable(dynamicSensorCallback, handler) {
            /* class com.android.systemui.util.sensors.$$Lambda$AsyncSensorManager$F0WdeYeejMV4rm6D4L93BEBFpEM */
            public final /* synthetic */ SensorManager.DynamicSensorCallback f$1;
            public final /* synthetic */ Handler f$2;

            {
                this.f$1 = r2;
                this.f$2 = r3;
            }

            public final void run() {
                AsyncSensorManager.this.lambda$registerDynamicSensorCallbackImpl$1$AsyncSensorManager(this.f$1, this.f$2);
            }
        });
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$unregisterDynamicSensorCallbackImpl$2 */
    public /* synthetic */ void lambda$unregisterDynamicSensorCallbackImpl$2$AsyncSensorManager(SensorManager.DynamicSensorCallback dynamicSensorCallback) {
        this.mInner.unregisterDynamicSensorCallback(dynamicSensorCallback);
    }

    /* access modifiers changed from: protected */
    public void unregisterDynamicSensorCallbackImpl(SensorManager.DynamicSensorCallback dynamicSensorCallback) {
        this.mHandler.post(new Runnable(dynamicSensorCallback) {
            /* class com.android.systemui.util.sensors.$$Lambda$AsyncSensorManager$jlPEINewb64o1tvevL779Llyz9o */
            public final /* synthetic */ SensorManager.DynamicSensorCallback f$1;

            {
                this.f$1 = r2;
            }

            public final void run() {
                AsyncSensorManager.this.lambda$unregisterDynamicSensorCallbackImpl$2$AsyncSensorManager(this.f$1);
            }
        });
    }

    /* access modifiers changed from: protected */
    public boolean requestTriggerSensorImpl(TriggerEventListener triggerEventListener, Sensor sensor) {
        if (triggerEventListener == null) {
            throw new IllegalArgumentException("listener cannot be null");
        } else if (sensor != null) {
            this.mHandler.post(new Runnable(sensor, triggerEventListener) {
                /* class com.android.systemui.util.sensors.$$Lambda$AsyncSensorManager$3wS3f7_WzpwBtp5OJ_snyLIVx0A */
                public final /* synthetic */ Sensor f$1;
                public final /* synthetic */ TriggerEventListener f$2;

                {
                    this.f$1 = r2;
                    this.f$2 = r3;
                }

                public final void run() {
                    AsyncSensorManager.this.lambda$requestTriggerSensorImpl$3$AsyncSensorManager(this.f$1, this.f$2);
                }
            });
            return true;
        } else {
            throw new IllegalArgumentException("sensor cannot be null");
        }
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$requestTriggerSensorImpl$3 */
    public /* synthetic */ void lambda$requestTriggerSensorImpl$3$AsyncSensorManager(Sensor sensor, TriggerEventListener triggerEventListener) {
        if (sensor == null) {
            Log.e("AsyncSensorManager", "sensor cannot be null");
        }
        if (!this.mInner.requestTriggerSensor(triggerEventListener, sensor)) {
            Log.e("AsyncSensorManager", "Requesting " + triggerEventListener + " for " + sensor + " failed.");
        }
    }

    /* access modifiers changed from: protected */
    public boolean cancelTriggerSensorImpl(TriggerEventListener triggerEventListener, Sensor sensor, boolean z) {
        Preconditions.checkArgument(z);
        this.mHandler.post(new Runnable(triggerEventListener, sensor) {
            /* class com.android.systemui.util.sensors.$$Lambda$AsyncSensorManager$rUtRem6mSTBr22Jz6SCHZv3qC7c */
            public final /* synthetic */ TriggerEventListener f$1;
            public final /* synthetic */ Sensor f$2;

            {
                this.f$1 = r2;
                this.f$2 = r3;
            }

            public final void run() {
                AsyncSensorManager.this.lambda$cancelTriggerSensorImpl$4$AsyncSensorManager(this.f$1, this.f$2);
            }
        });
        return true;
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$cancelTriggerSensorImpl$4 */
    public /* synthetic */ void lambda$cancelTriggerSensorImpl$4$AsyncSensorManager(TriggerEventListener triggerEventListener, Sensor sensor) {
        if (!this.mInner.cancelTriggerSensor(triggerEventListener, sensor)) {
            Log.e("AsyncSensorManager", "Canceling " + triggerEventListener + " for " + sensor + " failed.");
        }
    }

    public boolean registerPluginListener(SensorManagerPlugin.Sensor sensor, SensorManagerPlugin.SensorEventListener sensorEventListener) {
        if (this.mPlugins.isEmpty()) {
            Log.w("AsyncSensorManager", "No plugins registered");
            return false;
        }
        this.mHandler.post(new Runnable(sensor, sensorEventListener) {
            /* class com.android.systemui.util.sensors.$$Lambda$AsyncSensorManager$aB8h3ftRLBeKmBma753sk37oSSM */
            public final /* synthetic */ SensorManagerPlugin.Sensor f$1;
            public final /* synthetic */ SensorManagerPlugin.SensorEventListener f$2;

            {
                this.f$1 = r2;
                this.f$2 = r3;
            }

            public final void run() {
                AsyncSensorManager.this.lambda$registerPluginListener$5$AsyncSensorManager(this.f$1, this.f$2);
            }
        });
        return true;
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$registerPluginListener$5 */
    public /* synthetic */ void lambda$registerPluginListener$5$AsyncSensorManager(SensorManagerPlugin.Sensor sensor, SensorManagerPlugin.SensorEventListener sensorEventListener) {
        for (int i = 0; i < this.mPlugins.size(); i++) {
            this.mPlugins.get(i).registerListener(sensor, sensorEventListener);
        }
    }

    public void unregisterPluginListener(SensorManagerPlugin.Sensor sensor, SensorManagerPlugin.SensorEventListener sensorEventListener) {
        this.mHandler.post(new Runnable(sensor, sensorEventListener) {
            /* class com.android.systemui.util.sensors.$$Lambda$AsyncSensorManager$BRp9Isao7Fg71C6pjzafSkiRQo */
            public final /* synthetic */ SensorManagerPlugin.Sensor f$1;
            public final /* synthetic */ SensorManagerPlugin.SensorEventListener f$2;

            {
                this.f$1 = r2;
                this.f$2 = r3;
            }

            public final void run() {
                AsyncSensorManager.this.lambda$unregisterPluginListener$6$AsyncSensorManager(this.f$1, this.f$2);
            }
        });
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$unregisterPluginListener$6 */
    public /* synthetic */ void lambda$unregisterPluginListener$6$AsyncSensorManager(SensorManagerPlugin.Sensor sensor, SensorManagerPlugin.SensorEventListener sensorEventListener) {
        for (int i = 0; i < this.mPlugins.size(); i++) {
            this.mPlugins.get(i).unregisterListener(sensor, sensorEventListener);
        }
    }

    /* access modifiers changed from: protected */
    public boolean initDataInjectionImpl(boolean z) {
        throw new UnsupportedOperationException("not implemented");
    }

    /* access modifiers changed from: protected */
    public boolean injectSensorDataImpl(Sensor sensor, float[] fArr, int i, long j) {
        throw new UnsupportedOperationException("not implemented");
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$setOperationParameterImpl$7 */
    public /* synthetic */ void lambda$setOperationParameterImpl$7$AsyncSensorManager(SensorAdditionalInfo sensorAdditionalInfo) {
        this.mInner.setOperationParameter(sensorAdditionalInfo);
    }

    /* access modifiers changed from: protected */
    public boolean setOperationParameterImpl(SensorAdditionalInfo sensorAdditionalInfo) {
        this.mHandler.post(new Runnable(sensorAdditionalInfo) {
            /* class com.android.systemui.util.sensors.$$Lambda$AsyncSensorManager$I0ubq9rKvgslyfuYRTkWnHfBQA */
            public final /* synthetic */ SensorAdditionalInfo f$1;

            {
                this.f$1 = r2;
            }

            public final void run() {
                AsyncSensorManager.this.lambda$setOperationParameterImpl$7$AsyncSensorManager(this.f$1);
            }
        });
        return true;
    }

    /* access modifiers changed from: protected */
    public void unregisterListenerImpl(SensorEventListener sensorEventListener, Sensor sensor) {
        this.mHandler.post(new Runnable(sensor, sensorEventListener) {
            /* class com.android.systemui.util.sensors.$$Lambda$AsyncSensorManager$hqcwBQ7SIv_uRvhgQvDc2LVBJ_U */
            public final /* synthetic */ Sensor f$1;
            public final /* synthetic */ SensorEventListener f$2;

            {
                this.f$1 = r2;
                this.f$2 = r3;
            }

            public final void run() {
                AsyncSensorManager.this.lambda$unregisterListenerImpl$8$AsyncSensorManager(this.f$1, this.f$2);
            }
        });
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$unregisterListenerImpl$8 */
    public /* synthetic */ void lambda$unregisterListenerImpl$8$AsyncSensorManager(Sensor sensor, SensorEventListener sensorEventListener) {
        if (sensor == null) {
            this.mInner.unregisterListener(sensorEventListener);
        } else {
            this.mInner.unregisterListener(sensorEventListener, sensor);
        }
    }

    public void onPluginConnected(SensorManagerPlugin sensorManagerPlugin, Context context) {
        this.mPlugins.add(sensorManagerPlugin);
    }

    public void onPluginDisconnected(SensorManagerPlugin sensorManagerPlugin) {
        this.mPlugins.remove(sensorManagerPlugin);
    }
}
