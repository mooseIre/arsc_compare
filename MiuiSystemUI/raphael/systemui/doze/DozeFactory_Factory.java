package com.android.systemui.doze;

import android.app.AlarmManager;
import android.app.IWallpaperManager;
import android.os.Handler;
import com.android.keyguard.KeyguardUpdateMonitor;
import com.android.systemui.broadcast.BroadcastDispatcher;
import com.android.systemui.dock.DockManager;
import com.android.systemui.keyguard.WakefulnessLifecycle;
import com.android.systemui.plugins.FalsingManager;
import com.android.systemui.statusbar.phone.BiometricUnlockController;
import com.android.systemui.statusbar.phone.DozeParameters;
import com.android.systemui.statusbar.policy.BatteryController;
import com.android.systemui.util.sensors.AsyncSensorManager;
import com.android.systemui.util.sensors.ProximitySensor;
import com.android.systemui.util.wakelock.DelayedWakeLock;
import dagger.internal.Factory;
import javax.inject.Provider;

public final class DozeFactory_Factory implements Factory<DozeFactory> {
    private final Provider<AlarmManager> alarmManagerProvider;
    private final Provider<AsyncSensorManager> asyncSensorManagerProvider;
    private final Provider<BatteryController> batteryControllerProvider;
    private final Provider<BiometricUnlockController> biometricUnlockControllerProvider;
    private final Provider<BroadcastDispatcher> broadcastDispatcherProvider;
    private final Provider<DelayedWakeLock.Builder> delayedWakeLockBuilderProvider;
    private final Provider<DockManager> dockManagerProvider;
    private final Provider<DozeHost> dozeHostProvider;
    private final Provider<DozeLog> dozeLogProvider;
    private final Provider<DozeParameters> dozeParametersProvider;
    private final Provider<FalsingManager> falsingManagerProvider;
    private final Provider<Handler> handlerProvider;
    private final Provider<KeyguardUpdateMonitor> keyguardUpdateMonitorProvider;
    private final Provider<ProximitySensor.ProximityCheck> proximityCheckProvider;
    private final Provider<ProximitySensor> proximitySensorProvider;
    private final Provider<WakefulnessLifecycle> wakefulnessLifecycleProvider;
    private final Provider<IWallpaperManager> wallpaperManagerProvider;

    public DozeFactory_Factory(Provider<FalsingManager> provider, Provider<DozeLog> provider2, Provider<DozeParameters> provider3, Provider<BatteryController> provider4, Provider<AsyncSensorManager> provider5, Provider<AlarmManager> provider6, Provider<WakefulnessLifecycle> provider7, Provider<KeyguardUpdateMonitor> provider8, Provider<DockManager> provider9, Provider<IWallpaperManager> provider10, Provider<ProximitySensor> provider11, Provider<ProximitySensor.ProximityCheck> provider12, Provider<DelayedWakeLock.Builder> provider13, Provider<Handler> provider14, Provider<BiometricUnlockController> provider15, Provider<BroadcastDispatcher> provider16, Provider<DozeHost> provider17) {
        this.falsingManagerProvider = provider;
        this.dozeLogProvider = provider2;
        this.dozeParametersProvider = provider3;
        this.batteryControllerProvider = provider4;
        this.asyncSensorManagerProvider = provider5;
        this.alarmManagerProvider = provider6;
        this.wakefulnessLifecycleProvider = provider7;
        this.keyguardUpdateMonitorProvider = provider8;
        this.dockManagerProvider = provider9;
        this.wallpaperManagerProvider = provider10;
        this.proximitySensorProvider = provider11;
        this.proximityCheckProvider = provider12;
        this.delayedWakeLockBuilderProvider = provider13;
        this.handlerProvider = provider14;
        this.biometricUnlockControllerProvider = provider15;
        this.broadcastDispatcherProvider = provider16;
        this.dozeHostProvider = provider17;
    }

    @Override // javax.inject.Provider
    public DozeFactory get() {
        return provideInstance(this.falsingManagerProvider, this.dozeLogProvider, this.dozeParametersProvider, this.batteryControllerProvider, this.asyncSensorManagerProvider, this.alarmManagerProvider, this.wakefulnessLifecycleProvider, this.keyguardUpdateMonitorProvider, this.dockManagerProvider, this.wallpaperManagerProvider, this.proximitySensorProvider, this.proximityCheckProvider, this.delayedWakeLockBuilderProvider, this.handlerProvider, this.biometricUnlockControllerProvider, this.broadcastDispatcherProvider, this.dozeHostProvider);
    }

    public static DozeFactory provideInstance(Provider<FalsingManager> provider, Provider<DozeLog> provider2, Provider<DozeParameters> provider3, Provider<BatteryController> provider4, Provider<AsyncSensorManager> provider5, Provider<AlarmManager> provider6, Provider<WakefulnessLifecycle> provider7, Provider<KeyguardUpdateMonitor> provider8, Provider<DockManager> provider9, Provider<IWallpaperManager> provider10, Provider<ProximitySensor> provider11, Provider<ProximitySensor.ProximityCheck> provider12, Provider<DelayedWakeLock.Builder> provider13, Provider<Handler> provider14, Provider<BiometricUnlockController> provider15, Provider<BroadcastDispatcher> provider16, Provider<DozeHost> provider17) {
        return new DozeFactory(provider.get(), provider2.get(), provider3.get(), provider4.get(), provider5.get(), provider6.get(), provider7.get(), provider8.get(), provider9.get(), provider10.get(), provider11.get(), provider12.get(), provider13.get(), provider14.get(), provider15.get(), provider16.get(), provider17.get());
    }

    public static DozeFactory_Factory create(Provider<FalsingManager> provider, Provider<DozeLog> provider2, Provider<DozeParameters> provider3, Provider<BatteryController> provider4, Provider<AsyncSensorManager> provider5, Provider<AlarmManager> provider6, Provider<WakefulnessLifecycle> provider7, Provider<KeyguardUpdateMonitor> provider8, Provider<DockManager> provider9, Provider<IWallpaperManager> provider10, Provider<ProximitySensor> provider11, Provider<ProximitySensor.ProximityCheck> provider12, Provider<DelayedWakeLock.Builder> provider13, Provider<Handler> provider14, Provider<BiometricUnlockController> provider15, Provider<BroadcastDispatcher> provider16, Provider<DozeHost> provider17) {
        return new DozeFactory_Factory(provider, provider2, provider3, provider4, provider5, provider6, provider7, provider8, provider9, provider10, provider11, provider12, provider13, provider14, provider15, provider16, provider17);
    }
}
