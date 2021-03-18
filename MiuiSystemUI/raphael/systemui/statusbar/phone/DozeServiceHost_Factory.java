package com.android.systemui.statusbar.phone;

import android.os.PowerManager;
import com.android.keyguard.KeyguardUpdateMonitor;
import com.android.systemui.assist.AssistManager;
import com.android.systemui.doze.DozeLog;
import com.android.systemui.keyguard.KeyguardViewMediator;
import com.android.systemui.keyguard.WakefulnessLifecycle;
import com.android.systemui.statusbar.PulseExpansionHandler;
import com.android.systemui.statusbar.SysuiStatusBarStateController;
import com.android.systemui.statusbar.notification.NotificationWakeUpCoordinator;
import com.android.systemui.statusbar.notification.VisualStabilityManager;
import com.android.systemui.statusbar.policy.BatteryController;
import com.android.systemui.statusbar.policy.DeviceProvisionedController;
import dagger.internal.DoubleCheck;
import dagger.internal.Factory;
import javax.inject.Provider;

public final class DozeServiceHost_Factory implements Factory<DozeServiceHost> {
    private final Provider<AssistManager> assistManagerLazyProvider;
    private final Provider<BatteryController> batteryControllerProvider;
    private final Provider<BiometricUnlockController> biometricUnlockControllerLazyProvider;
    private final Provider<DeviceProvisionedController> deviceProvisionedControllerProvider;
    private final Provider<DozeLog> dozeLogProvider;
    private final Provider<DozeScrimController> dozeScrimControllerProvider;
    private final Provider<HeadsUpManagerPhone> headsUpManagerPhoneProvider;
    private final Provider<KeyguardUpdateMonitor> keyguardUpdateMonitorProvider;
    private final Provider<KeyguardViewMediator> keyguardViewMediatorProvider;
    private final Provider<LockscreenLockIconController> lockscreenLockIconControllerProvider;
    private final Provider<NotificationShadeWindowController> notificationShadeWindowControllerProvider;
    private final Provider<NotificationWakeUpCoordinator> notificationWakeUpCoordinatorProvider;
    private final Provider<PowerManager> powerManagerProvider;
    private final Provider<PulseExpansionHandler> pulseExpansionHandlerProvider;
    private final Provider<ScrimController> scrimControllerProvider;
    private final Provider<SysuiStatusBarStateController> statusBarStateControllerProvider;
    private final Provider<VisualStabilityManager> visualStabilityManagerProvider;
    private final Provider<WakefulnessLifecycle> wakefulnessLifecycleProvider;

    public DozeServiceHost_Factory(Provider<DozeLog> provider, Provider<PowerManager> provider2, Provider<WakefulnessLifecycle> provider3, Provider<SysuiStatusBarStateController> provider4, Provider<DeviceProvisionedController> provider5, Provider<HeadsUpManagerPhone> provider6, Provider<BatteryController> provider7, Provider<ScrimController> provider8, Provider<BiometricUnlockController> provider9, Provider<KeyguardViewMediator> provider10, Provider<AssistManager> provider11, Provider<DozeScrimController> provider12, Provider<KeyguardUpdateMonitor> provider13, Provider<VisualStabilityManager> provider14, Provider<PulseExpansionHandler> provider15, Provider<NotificationShadeWindowController> provider16, Provider<NotificationWakeUpCoordinator> provider17, Provider<LockscreenLockIconController> provider18) {
        this.dozeLogProvider = provider;
        this.powerManagerProvider = provider2;
        this.wakefulnessLifecycleProvider = provider3;
        this.statusBarStateControllerProvider = provider4;
        this.deviceProvisionedControllerProvider = provider5;
        this.headsUpManagerPhoneProvider = provider6;
        this.batteryControllerProvider = provider7;
        this.scrimControllerProvider = provider8;
        this.biometricUnlockControllerLazyProvider = provider9;
        this.keyguardViewMediatorProvider = provider10;
        this.assistManagerLazyProvider = provider11;
        this.dozeScrimControllerProvider = provider12;
        this.keyguardUpdateMonitorProvider = provider13;
        this.visualStabilityManagerProvider = provider14;
        this.pulseExpansionHandlerProvider = provider15;
        this.notificationShadeWindowControllerProvider = provider16;
        this.notificationWakeUpCoordinatorProvider = provider17;
        this.lockscreenLockIconControllerProvider = provider18;
    }

    @Override // javax.inject.Provider
    public DozeServiceHost get() {
        return provideInstance(this.dozeLogProvider, this.powerManagerProvider, this.wakefulnessLifecycleProvider, this.statusBarStateControllerProvider, this.deviceProvisionedControllerProvider, this.headsUpManagerPhoneProvider, this.batteryControllerProvider, this.scrimControllerProvider, this.biometricUnlockControllerLazyProvider, this.keyguardViewMediatorProvider, this.assistManagerLazyProvider, this.dozeScrimControllerProvider, this.keyguardUpdateMonitorProvider, this.visualStabilityManagerProvider, this.pulseExpansionHandlerProvider, this.notificationShadeWindowControllerProvider, this.notificationWakeUpCoordinatorProvider, this.lockscreenLockIconControllerProvider);
    }

    public static DozeServiceHost provideInstance(Provider<DozeLog> provider, Provider<PowerManager> provider2, Provider<WakefulnessLifecycle> provider3, Provider<SysuiStatusBarStateController> provider4, Provider<DeviceProvisionedController> provider5, Provider<HeadsUpManagerPhone> provider6, Provider<BatteryController> provider7, Provider<ScrimController> provider8, Provider<BiometricUnlockController> provider9, Provider<KeyguardViewMediator> provider10, Provider<AssistManager> provider11, Provider<DozeScrimController> provider12, Provider<KeyguardUpdateMonitor> provider13, Provider<VisualStabilityManager> provider14, Provider<PulseExpansionHandler> provider15, Provider<NotificationShadeWindowController> provider16, Provider<NotificationWakeUpCoordinator> provider17, Provider<LockscreenLockIconController> provider18) {
        return new DozeServiceHost(provider.get(), provider2.get(), provider3.get(), provider4.get(), provider5.get(), provider6.get(), provider7.get(), provider8.get(), DoubleCheck.lazy(provider9), provider10.get(), DoubleCheck.lazy(provider11), provider12.get(), provider13.get(), provider14.get(), provider15.get(), provider16.get(), provider17.get(), provider18.get());
    }

    public static DozeServiceHost_Factory create(Provider<DozeLog> provider, Provider<PowerManager> provider2, Provider<WakefulnessLifecycle> provider3, Provider<SysuiStatusBarStateController> provider4, Provider<DeviceProvisionedController> provider5, Provider<HeadsUpManagerPhone> provider6, Provider<BatteryController> provider7, Provider<ScrimController> provider8, Provider<BiometricUnlockController> provider9, Provider<KeyguardViewMediator> provider10, Provider<AssistManager> provider11, Provider<DozeScrimController> provider12, Provider<KeyguardUpdateMonitor> provider13, Provider<VisualStabilityManager> provider14, Provider<PulseExpansionHandler> provider15, Provider<NotificationShadeWindowController> provider16, Provider<NotificationWakeUpCoordinator> provider17, Provider<LockscreenLockIconController> provider18) {
        return new DozeServiceHost_Factory(provider, provider2, provider3, provider4, provider5, provider6, provider7, provider8, provider9, provider10, provider11, provider12, provider13, provider14, provider15, provider16, provider17, provider18);
    }
}
