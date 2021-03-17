package com.android.systemui.statusbar.phone;

import android.app.AlarmManager;
import android.app.IActivityManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.UserManager;
import android.telecom.TelecomManager;
import com.android.systemui.broadcast.BroadcastDispatcher;
import com.android.systemui.screenrecord.RecordingController;
import com.android.systemui.statusbar.CommandQueue;
import com.android.systemui.statusbar.policy.BluetoothController;
import com.android.systemui.statusbar.policy.CastController;
import com.android.systemui.statusbar.policy.DataSaverController;
import com.android.systemui.statusbar.policy.DeviceProvisionedController;
import com.android.systemui.statusbar.policy.HotspotController;
import com.android.systemui.statusbar.policy.KeyguardStateController;
import com.android.systemui.statusbar.policy.LocationController;
import com.android.systemui.statusbar.policy.NextAlarmController;
import com.android.systemui.statusbar.policy.RotationLockController;
import com.android.systemui.statusbar.policy.SensorPrivacyController;
import com.android.systemui.statusbar.policy.UserInfoController;
import com.android.systemui.statusbar.policy.ZenModeController;
import com.android.systemui.util.RingerModeTracker;
import com.android.systemui.util.time.DateFormatUtil;
import dagger.internal.Factory;
import java.util.concurrent.Executor;
import javax.inject.Provider;

public final class MiuiPhoneStatusBarPolicy_Factory implements Factory<MiuiPhoneStatusBarPolicy> {
    private final Provider<AlarmManager> alarmManagerProvider;
    private final Provider<BluetoothController> bluetoothControllerProvider;
    private final Provider<BroadcastDispatcher> broadcastDispatcherProvider;
    private final Provider<CastController> castControllerProvider;
    private final Provider<CommandQueue> commandQueueProvider;
    private final Provider<Context> contextProvider;
    private final Provider<DataSaverController> dataSaverControllerProvider;
    private final Provider<DateFormatUtil> dateFormatUtilProvider;
    private final Provider<DeviceProvisionedController> deviceProvisionedControllerProvider;
    private final Provider<Integer> displayIdProvider;
    private final Provider<HotspotController> hotspotControllerProvider;
    private final Provider<IActivityManager> iActivityManagerProvider;
    private final Provider<StatusBarIconController> iconControllerProvider;
    private final Provider<KeyguardStateController> keyguardStateControllerProvider;
    private final Provider<LocationController> locationControllerProvider;
    private final Provider<NextAlarmController> nextAlarmControllerProvider;
    private final Provider<RecordingController> recordingControllerProvider;
    private final Provider<Resources> resourcesProvider;
    private final Provider<RingerModeTracker> ringerModeTrackerProvider;
    private final Provider<RotationLockController> rotationLockControllerProvider;
    private final Provider<SensorPrivacyController> sensorPrivacyControllerProvider;
    private final Provider<SharedPreferences> sharedPreferencesProvider;
    private final Provider<TelecomManager> telecomManagerProvider;
    private final Provider<Executor> uiBgExecutorProvider;
    private final Provider<UserInfoController> userInfoControllerProvider;
    private final Provider<UserManager> userManagerProvider;
    private final Provider<ZenModeController> zenModeControllerProvider;

    public MiuiPhoneStatusBarPolicy_Factory(Provider<Context> provider, Provider<StatusBarIconController> provider2, Provider<CommandQueue> provider3, Provider<BroadcastDispatcher> provider4, Provider<Executor> provider5, Provider<Resources> provider6, Provider<CastController> provider7, Provider<HotspotController> provider8, Provider<BluetoothController> provider9, Provider<NextAlarmController> provider10, Provider<UserInfoController> provider11, Provider<RotationLockController> provider12, Provider<DataSaverController> provider13, Provider<ZenModeController> provider14, Provider<DeviceProvisionedController> provider15, Provider<KeyguardStateController> provider16, Provider<LocationController> provider17, Provider<SensorPrivacyController> provider18, Provider<IActivityManager> provider19, Provider<AlarmManager> provider20, Provider<UserManager> provider21, Provider<RecordingController> provider22, Provider<TelecomManager> provider23, Provider<Integer> provider24, Provider<SharedPreferences> provider25, Provider<DateFormatUtil> provider26, Provider<RingerModeTracker> provider27) {
        this.contextProvider = provider;
        this.iconControllerProvider = provider2;
        this.commandQueueProvider = provider3;
        this.broadcastDispatcherProvider = provider4;
        this.uiBgExecutorProvider = provider5;
        this.resourcesProvider = provider6;
        this.castControllerProvider = provider7;
        this.hotspotControllerProvider = provider8;
        this.bluetoothControllerProvider = provider9;
        this.nextAlarmControllerProvider = provider10;
        this.userInfoControllerProvider = provider11;
        this.rotationLockControllerProvider = provider12;
        this.dataSaverControllerProvider = provider13;
        this.zenModeControllerProvider = provider14;
        this.deviceProvisionedControllerProvider = provider15;
        this.keyguardStateControllerProvider = provider16;
        this.locationControllerProvider = provider17;
        this.sensorPrivacyControllerProvider = provider18;
        this.iActivityManagerProvider = provider19;
        this.alarmManagerProvider = provider20;
        this.userManagerProvider = provider21;
        this.recordingControllerProvider = provider22;
        this.telecomManagerProvider = provider23;
        this.displayIdProvider = provider24;
        this.sharedPreferencesProvider = provider25;
        this.dateFormatUtilProvider = provider26;
        this.ringerModeTrackerProvider = provider27;
    }

    @Override // javax.inject.Provider
    public MiuiPhoneStatusBarPolicy get() {
        return provideInstance(this.contextProvider, this.iconControllerProvider, this.commandQueueProvider, this.broadcastDispatcherProvider, this.uiBgExecutorProvider, this.resourcesProvider, this.castControllerProvider, this.hotspotControllerProvider, this.bluetoothControllerProvider, this.nextAlarmControllerProvider, this.userInfoControllerProvider, this.rotationLockControllerProvider, this.dataSaverControllerProvider, this.zenModeControllerProvider, this.deviceProvisionedControllerProvider, this.keyguardStateControllerProvider, this.locationControllerProvider, this.sensorPrivacyControllerProvider, this.iActivityManagerProvider, this.alarmManagerProvider, this.userManagerProvider, this.recordingControllerProvider, this.telecomManagerProvider, this.displayIdProvider, this.sharedPreferencesProvider, this.dateFormatUtilProvider, this.ringerModeTrackerProvider);
    }

    public static MiuiPhoneStatusBarPolicy provideInstance(Provider<Context> provider, Provider<StatusBarIconController> provider2, Provider<CommandQueue> provider3, Provider<BroadcastDispatcher> provider4, Provider<Executor> provider5, Provider<Resources> provider6, Provider<CastController> provider7, Provider<HotspotController> provider8, Provider<BluetoothController> provider9, Provider<NextAlarmController> provider10, Provider<UserInfoController> provider11, Provider<RotationLockController> provider12, Provider<DataSaverController> provider13, Provider<ZenModeController> provider14, Provider<DeviceProvisionedController> provider15, Provider<KeyguardStateController> provider16, Provider<LocationController> provider17, Provider<SensorPrivacyController> provider18, Provider<IActivityManager> provider19, Provider<AlarmManager> provider20, Provider<UserManager> provider21, Provider<RecordingController> provider22, Provider<TelecomManager> provider23, Provider<Integer> provider24, Provider<SharedPreferences> provider25, Provider<DateFormatUtil> provider26, Provider<RingerModeTracker> provider27) {
        return new MiuiPhoneStatusBarPolicy(provider.get(), provider2.get(), provider3.get(), provider4.get(), provider5.get(), provider6.get(), provider7.get(), provider8.get(), provider9.get(), provider10.get(), provider11.get(), provider12.get(), provider13.get(), provider14.get(), provider15.get(), provider16.get(), provider17.get(), provider18.get(), provider19.get(), provider20.get(), provider21.get(), provider22.get(), provider23.get(), provider24.get().intValue(), provider25.get(), provider26.get(), provider27.get());
    }

    public static MiuiPhoneStatusBarPolicy_Factory create(Provider<Context> provider, Provider<StatusBarIconController> provider2, Provider<CommandQueue> provider3, Provider<BroadcastDispatcher> provider4, Provider<Executor> provider5, Provider<Resources> provider6, Provider<CastController> provider7, Provider<HotspotController> provider8, Provider<BluetoothController> provider9, Provider<NextAlarmController> provider10, Provider<UserInfoController> provider11, Provider<RotationLockController> provider12, Provider<DataSaverController> provider13, Provider<ZenModeController> provider14, Provider<DeviceProvisionedController> provider15, Provider<KeyguardStateController> provider16, Provider<LocationController> provider17, Provider<SensorPrivacyController> provider18, Provider<IActivityManager> provider19, Provider<AlarmManager> provider20, Provider<UserManager> provider21, Provider<RecordingController> provider22, Provider<TelecomManager> provider23, Provider<Integer> provider24, Provider<SharedPreferences> provider25, Provider<DateFormatUtil> provider26, Provider<RingerModeTracker> provider27) {
        return new MiuiPhoneStatusBarPolicy_Factory(provider, provider2, provider3, provider4, provider5, provider6, provider7, provider8, provider9, provider10, provider11, provider12, provider13, provider14, provider15, provider16, provider17, provider18, provider19, provider20, provider21, provider22, provider23, provider24, provider25, provider26, provider27);
    }
}
