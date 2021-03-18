package com.android.systemui.doze;

import android.app.AlarmManager;
import android.app.IWallpaperManager;
import android.content.Context;
import android.hardware.SensorManager;
import android.hardware.display.AmbientDisplayConfiguration;
import android.os.Handler;
import com.android.keyguard.KeyguardUpdateMonitor;
import com.android.systemui.C0021R$string;
import com.android.systemui.broadcast.BroadcastDispatcher;
import com.android.systemui.dock.DockManager;
import com.android.systemui.doze.DozeMachine;
import com.android.systemui.keyguard.WakefulnessLifecycle;
import com.android.systemui.plugins.FalsingManager;
import com.android.systemui.statusbar.phone.BiometricUnlockController;
import com.android.systemui.statusbar.phone.DozeParameters;
import com.android.systemui.statusbar.policy.BatteryController;
import com.android.systemui.util.sensors.AsyncSensorManager;
import com.android.systemui.util.sensors.ProximitySensor;
import com.android.systemui.util.wakelock.DelayedWakeLock;
import com.android.systemui.util.wakelock.WakeLock;

public class DozeFactory {
    private final AlarmManager mAlarmManager;
    private final AsyncSensorManager mAsyncSensorManager;
    private final BatteryController mBatteryController;
    private final BiometricUnlockController mBiometricUnlockController;
    private final BroadcastDispatcher mBroadcastDispatcher;
    private final DelayedWakeLock.Builder mDelayedWakeLockBuilder;
    private final DockManager mDockManager;
    private final DozeHost mDozeHost;
    private final DozeLog mDozeLog;
    private final DozeParameters mDozeParameters;
    private final FalsingManager mFalsingManager;
    private final Handler mHandler;
    private final KeyguardUpdateMonitor mKeyguardUpdateMonitor;
    private final ProximitySensor.ProximityCheck mProximityCheck;
    private final ProximitySensor mProximitySensor;
    private final WakefulnessLifecycle mWakefulnessLifecycle;
    private final IWallpaperManager mWallpaperManager;

    public DozeFactory(FalsingManager falsingManager, DozeLog dozeLog, DozeParameters dozeParameters, BatteryController batteryController, AsyncSensorManager asyncSensorManager, AlarmManager alarmManager, WakefulnessLifecycle wakefulnessLifecycle, KeyguardUpdateMonitor keyguardUpdateMonitor, DockManager dockManager, IWallpaperManager iWallpaperManager, ProximitySensor proximitySensor, ProximitySensor.ProximityCheck proximityCheck, DelayedWakeLock.Builder builder, Handler handler, BiometricUnlockController biometricUnlockController, BroadcastDispatcher broadcastDispatcher, DozeHost dozeHost) {
        this.mFalsingManager = falsingManager;
        this.mDozeLog = dozeLog;
        this.mDozeParameters = dozeParameters;
        this.mBatteryController = batteryController;
        this.mAsyncSensorManager = asyncSensorManager;
        this.mAlarmManager = alarmManager;
        this.mWakefulnessLifecycle = wakefulnessLifecycle;
        this.mKeyguardUpdateMonitor = keyguardUpdateMonitor;
        this.mDockManager = dockManager;
        this.mWallpaperManager = iWallpaperManager;
        this.mProximitySensor = proximitySensor;
        this.mProximityCheck = proximityCheck;
        this.mDelayedWakeLockBuilder = builder;
        this.mHandler = handler;
        this.mBiometricUnlockController = biometricUnlockController;
        this.mBroadcastDispatcher = broadcastDispatcher;
        this.mDozeHost = dozeHost;
    }

    /* access modifiers changed from: package-private */
    public DozeMachine assembleMachine(DozeService dozeService) {
        AmbientDisplayConfiguration ambientDisplayConfiguration = new AmbientDisplayConfiguration(dozeService);
        DelayedWakeLock.Builder builder = this.mDelayedWakeLockBuilder;
        builder.setHandler(this.mHandler);
        builder.setTag("Doze");
        DelayedWakeLock build = builder.build();
        DozeMachine.Service wrapIfNeeded = DozeSuspendScreenStatePreventingAdapter.wrapIfNeeded(DozeScreenStatePreventingAdapter.wrapIfNeeded(new DozeBrightnessHostForwarder(dozeService, this.mDozeHost), this.mDozeParameters), this.mDozeParameters);
        DozeMachine dozeMachine = new DozeMachine(wrapIfNeeded, ambientDisplayConfiguration, build, this.mWakefulnessLifecycle, this.mBatteryController, this.mDozeLog, this.mDockManager, this.mDozeHost);
        dozeMachine.setParts(new DozeMachine.Part[]{new DozePauser(this.mHandler, dozeMachine, this.mAlarmManager, this.mDozeParameters.getPolicy()), new DozeFalsingManagerAdapter(this.mFalsingManager), createDozeTriggers(dozeService, this.mAsyncSensorManager, this.mDozeHost, this.mAlarmManager, ambientDisplayConfiguration, this.mDozeParameters, build, dozeMachine, this.mDockManager, this.mDozeLog, this.mProximityCheck), createDozeUi(dozeService, this.mDozeHost, build, dozeMachine, this.mHandler, this.mAlarmManager, this.mDozeParameters, this.mDozeLog), new DozeScreenState(wrapIfNeeded, this.mHandler, this.mDozeHost, this.mDozeParameters, build), createDozeScreenBrightness(dozeService, wrapIfNeeded, this.mAsyncSensorManager, this.mDozeHost, this.mDozeParameters, this.mHandler), new DozeWallpaperState(this.mWallpaperManager, this.mBiometricUnlockController, this.mDozeParameters), new DozeDockHandler(ambientDisplayConfiguration, dozeMachine, this.mDockManager), new DozeAuthRemover(dozeService)});
        return dozeMachine;
    }

    private DozeMachine.Part createDozeScreenBrightness(Context context, DozeMachine.Service service, SensorManager sensorManager, DozeHost dozeHost, DozeParameters dozeParameters, Handler handler) {
        return new DozeScreenBrightness(context, service, sensorManager, DozeSensors.findSensorWithType(sensorManager, context.getString(C0021R$string.doze_brightness_sensor_type)), this.mBroadcastDispatcher, dozeHost, handler, dozeParameters.getPolicy());
    }

    private DozeTriggers createDozeTriggers(Context context, AsyncSensorManager asyncSensorManager, DozeHost dozeHost, AlarmManager alarmManager, AmbientDisplayConfiguration ambientDisplayConfiguration, DozeParameters dozeParameters, WakeLock wakeLock, DozeMachine dozeMachine, DockManager dockManager, DozeLog dozeLog, ProximitySensor.ProximityCheck proximityCheck) {
        return new DozeTriggers(context, dozeMachine, dozeHost, alarmManager, ambientDisplayConfiguration, dozeParameters, asyncSensorManager, wakeLock, true, dockManager, this.mProximitySensor, proximityCheck, dozeLog, this.mBroadcastDispatcher);
    }

    private DozeMachine.Part createDozeUi(Context context, DozeHost dozeHost, WakeLock wakeLock, DozeMachine dozeMachine, Handler handler, AlarmManager alarmManager, DozeParameters dozeParameters, DozeLog dozeLog) {
        return new DozeUi(context, alarmManager, dozeMachine, wakeLock, dozeHost, handler, dozeParameters, this.mKeyguardUpdateMonitor, dozeLog);
    }
}
