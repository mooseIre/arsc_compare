package com.android.systemui.statusbar.phone;

import android.app.ActivityManager;
import android.os.PowerManager;
import android.view.accessibility.AccessibilityManager;
import com.android.internal.logging.MetricsLogger;
import com.android.internal.util.LatencyTracker;
import com.android.keyguard.KeyguardUpdateMonitor;
import com.android.systemui.controlcenter.phone.ControlPanelController;
import com.android.systemui.doze.DozeLog;
import com.android.systemui.keyguard.WakefulnessLifecycle;
import com.android.systemui.media.MediaHierarchyManager;
import com.android.systemui.plugins.FalsingManager;
import com.android.systemui.plugins.statusbar.StatusBarStateController;
import com.android.systemui.statusbar.CommandQueue;
import com.android.systemui.statusbar.FlingAnimationUtils;
import com.android.systemui.statusbar.NotificationLockscreenUserManager;
import com.android.systemui.statusbar.PulseExpansionHandler;
import com.android.systemui.statusbar.VibratorHelper;
import com.android.systemui.statusbar.notification.ConversationNotificationManager;
import com.android.systemui.statusbar.notification.DynamicPrivacyController;
import com.android.systemui.statusbar.notification.NotificationEntryManager;
import com.android.systemui.statusbar.notification.NotificationWakeUpCoordinator;
import com.android.systemui.statusbar.policy.ConfigurationController;
import com.android.systemui.statusbar.policy.KeyguardStateController;
import com.android.systemui.statusbar.policy.ZenModeController;
import com.android.systemui.util.InjectionInflationController;
import com.miui.systemui.EventTracker;
import dagger.internal.DoubleCheck;
import dagger.internal.Factory;
import javax.inject.Provider;

public final class MiuiNotificationPanelViewController_Factory implements Factory<MiuiNotificationPanelViewController> {
    private final Provider<AccessibilityManager> accessibilityManagerProvider;
    private final Provider<ActivityManager> activityManagerProvider;
    private final Provider<BiometricUnlockController> biometricUnlockControllerProvider;
    private final Provider<KeyguardBypassController> bypassControllerProvider;
    private final Provider<CommandQueue> commandQueueProvider;
    private final Provider<ConfigurationController> configurationControllerProvider;
    private final Provider<ControlPanelController> controlPanelControllerProvider;
    private final Provider<ConversationNotificationManager> conversationNotificationManagerProvider;
    private final Provider<NotificationWakeUpCoordinator> coordinatorProvider;
    private final Provider<Integer> displayIdProvider;
    private final Provider<DozeLog> dozeLogProvider;
    private final Provider<DozeParameters> dozeParametersProvider;
    private final Provider<DynamicPrivacyController> dynamicPrivacyControllerProvider;
    private final Provider<EventTracker> eventTrackerProvider;
    private final Provider<FalsingManager> falsingManagerProvider;
    private final Provider<FlingAnimationUtils.Builder> flingAnimationUtilsBuilderProvider;
    private final Provider<InjectionInflationController> injectionInflationControllerProvider;
    private final Provider<KeyguardStateController> keyguardStateControllerProvider;
    private final Provider<KeyguardUpdateMonitor> keyguardUpdateMonitorProvider;
    private final Provider<LatencyTracker> latencyTrackerProvider;
    private final Provider<MediaHierarchyManager> mediaHierarchyManagerProvider;
    private final Provider<MetricsLogger> metricsLoggerProvider;
    private final Provider<NotificationEntryManager> notificationEntryManagerProvider;
    private final Provider<NotificationLockscreenUserManager> notificationLockscreenUserManagerProvider;
    private final Provider<NotificationPanelView> panelViewProvider;
    private final Provider<PowerManager> powerManagerProvider;
    private final Provider<PulseExpansionHandler> pulseExpansionHandlerProvider;
    private final Provider<ShadeController> shadeControllerProvider;
    private final Provider<NotificationShadeWindowController> shadeWindowControllerProvider;
    private final Provider<StatusBarKeyguardViewManager> statusBarKeyguardViewManagerProvider;
    private final Provider<StatusBarStateController> statusBarStateControllerProvider;
    private final Provider<StatusBarTouchableRegionManager> statusBarTouchableRegionManagerProvider;
    private final Provider<VibratorHelper> vibratorHelperProvider;
    private final Provider<WakefulnessLifecycle> wakefulnessLifecycleProvider;
    private final Provider<ZenModeController> zenModeControllerProvider;

    public MiuiNotificationPanelViewController_Factory(Provider<NotificationPanelView> provider, Provider<InjectionInflationController> provider2, Provider<NotificationWakeUpCoordinator> provider3, Provider<PulseExpansionHandler> provider4, Provider<DynamicPrivacyController> provider5, Provider<KeyguardBypassController> provider6, Provider<FalsingManager> provider7, Provider<ShadeController> provider8, Provider<NotificationLockscreenUserManager> provider9, Provider<NotificationEntryManager> provider10, Provider<KeyguardStateController> provider11, Provider<StatusBarStateController> provider12, Provider<DozeLog> provider13, Provider<DozeParameters> provider14, Provider<CommandQueue> provider15, Provider<VibratorHelper> provider16, Provider<LatencyTracker> provider17, Provider<PowerManager> provider18, Provider<AccessibilityManager> provider19, Provider<Integer> provider20, Provider<KeyguardUpdateMonitor> provider21, Provider<MetricsLogger> provider22, Provider<ActivityManager> provider23, Provider<ZenModeController> provider24, Provider<ConfigurationController> provider25, Provider<FlingAnimationUtils.Builder> provider26, Provider<StatusBarTouchableRegionManager> provider27, Provider<ConversationNotificationManager> provider28, Provider<MediaHierarchyManager> provider29, Provider<BiometricUnlockController> provider30, Provider<StatusBarKeyguardViewManager> provider31, Provider<ControlPanelController> provider32, Provider<EventTracker> provider33, Provider<WakefulnessLifecycle> provider34, Provider<NotificationShadeWindowController> provider35) {
        this.panelViewProvider = provider;
        this.injectionInflationControllerProvider = provider2;
        this.coordinatorProvider = provider3;
        this.pulseExpansionHandlerProvider = provider4;
        this.dynamicPrivacyControllerProvider = provider5;
        this.bypassControllerProvider = provider6;
        this.falsingManagerProvider = provider7;
        this.shadeControllerProvider = provider8;
        this.notificationLockscreenUserManagerProvider = provider9;
        this.notificationEntryManagerProvider = provider10;
        this.keyguardStateControllerProvider = provider11;
        this.statusBarStateControllerProvider = provider12;
        this.dozeLogProvider = provider13;
        this.dozeParametersProvider = provider14;
        this.commandQueueProvider = provider15;
        this.vibratorHelperProvider = provider16;
        this.latencyTrackerProvider = provider17;
        this.powerManagerProvider = provider18;
        this.accessibilityManagerProvider = provider19;
        this.displayIdProvider = provider20;
        this.keyguardUpdateMonitorProvider = provider21;
        this.metricsLoggerProvider = provider22;
        this.activityManagerProvider = provider23;
        this.zenModeControllerProvider = provider24;
        this.configurationControllerProvider = provider25;
        this.flingAnimationUtilsBuilderProvider = provider26;
        this.statusBarTouchableRegionManagerProvider = provider27;
        this.conversationNotificationManagerProvider = provider28;
        this.mediaHierarchyManagerProvider = provider29;
        this.biometricUnlockControllerProvider = provider30;
        this.statusBarKeyguardViewManagerProvider = provider31;
        this.controlPanelControllerProvider = provider32;
        this.eventTrackerProvider = provider33;
        this.wakefulnessLifecycleProvider = provider34;
        this.shadeWindowControllerProvider = provider35;
    }

    @Override // javax.inject.Provider
    public MiuiNotificationPanelViewController get() {
        return provideInstance(this.panelViewProvider, this.injectionInflationControllerProvider, this.coordinatorProvider, this.pulseExpansionHandlerProvider, this.dynamicPrivacyControllerProvider, this.bypassControllerProvider, this.falsingManagerProvider, this.shadeControllerProvider, this.notificationLockscreenUserManagerProvider, this.notificationEntryManagerProvider, this.keyguardStateControllerProvider, this.statusBarStateControllerProvider, this.dozeLogProvider, this.dozeParametersProvider, this.commandQueueProvider, this.vibratorHelperProvider, this.latencyTrackerProvider, this.powerManagerProvider, this.accessibilityManagerProvider, this.displayIdProvider, this.keyguardUpdateMonitorProvider, this.metricsLoggerProvider, this.activityManagerProvider, this.zenModeControllerProvider, this.configurationControllerProvider, this.flingAnimationUtilsBuilderProvider, this.statusBarTouchableRegionManagerProvider, this.conversationNotificationManagerProvider, this.mediaHierarchyManagerProvider, this.biometricUnlockControllerProvider, this.statusBarKeyguardViewManagerProvider, this.controlPanelControllerProvider, this.eventTrackerProvider, this.wakefulnessLifecycleProvider, this.shadeWindowControllerProvider);
    }

    public static MiuiNotificationPanelViewController provideInstance(Provider<NotificationPanelView> provider, Provider<InjectionInflationController> provider2, Provider<NotificationWakeUpCoordinator> provider3, Provider<PulseExpansionHandler> provider4, Provider<DynamicPrivacyController> provider5, Provider<KeyguardBypassController> provider6, Provider<FalsingManager> provider7, Provider<ShadeController> provider8, Provider<NotificationLockscreenUserManager> provider9, Provider<NotificationEntryManager> provider10, Provider<KeyguardStateController> provider11, Provider<StatusBarStateController> provider12, Provider<DozeLog> provider13, Provider<DozeParameters> provider14, Provider<CommandQueue> provider15, Provider<VibratorHelper> provider16, Provider<LatencyTracker> provider17, Provider<PowerManager> provider18, Provider<AccessibilityManager> provider19, Provider<Integer> provider20, Provider<KeyguardUpdateMonitor> provider21, Provider<MetricsLogger> provider22, Provider<ActivityManager> provider23, Provider<ZenModeController> provider24, Provider<ConfigurationController> provider25, Provider<FlingAnimationUtils.Builder> provider26, Provider<StatusBarTouchableRegionManager> provider27, Provider<ConversationNotificationManager> provider28, Provider<MediaHierarchyManager> provider29, Provider<BiometricUnlockController> provider30, Provider<StatusBarKeyguardViewManager> provider31, Provider<ControlPanelController> provider32, Provider<EventTracker> provider33, Provider<WakefulnessLifecycle> provider34, Provider<NotificationShadeWindowController> provider35) {
        return new MiuiNotificationPanelViewController(provider.get(), provider2.get(), provider3.get(), provider4.get(), provider5.get(), provider6.get(), provider7.get(), provider8.get(), provider9.get(), provider10.get(), provider11.get(), provider12.get(), provider13.get(), provider14.get(), provider15.get(), provider16.get(), provider17.get(), provider18.get(), provider19.get(), provider20.get().intValue(), provider21.get(), provider22.get(), provider23.get(), provider24.get(), provider25.get(), provider26.get(), provider27.get(), provider28.get(), provider29.get(), provider30.get(), provider31.get(), DoubleCheck.lazy(provider32), provider33.get(), provider34.get(), provider35.get());
    }

    public static MiuiNotificationPanelViewController_Factory create(Provider<NotificationPanelView> provider, Provider<InjectionInflationController> provider2, Provider<NotificationWakeUpCoordinator> provider3, Provider<PulseExpansionHandler> provider4, Provider<DynamicPrivacyController> provider5, Provider<KeyguardBypassController> provider6, Provider<FalsingManager> provider7, Provider<ShadeController> provider8, Provider<NotificationLockscreenUserManager> provider9, Provider<NotificationEntryManager> provider10, Provider<KeyguardStateController> provider11, Provider<StatusBarStateController> provider12, Provider<DozeLog> provider13, Provider<DozeParameters> provider14, Provider<CommandQueue> provider15, Provider<VibratorHelper> provider16, Provider<LatencyTracker> provider17, Provider<PowerManager> provider18, Provider<AccessibilityManager> provider19, Provider<Integer> provider20, Provider<KeyguardUpdateMonitor> provider21, Provider<MetricsLogger> provider22, Provider<ActivityManager> provider23, Provider<ZenModeController> provider24, Provider<ConfigurationController> provider25, Provider<FlingAnimationUtils.Builder> provider26, Provider<StatusBarTouchableRegionManager> provider27, Provider<ConversationNotificationManager> provider28, Provider<MediaHierarchyManager> provider29, Provider<BiometricUnlockController> provider30, Provider<StatusBarKeyguardViewManager> provider31, Provider<ControlPanelController> provider32, Provider<EventTracker> provider33, Provider<WakefulnessLifecycle> provider34, Provider<NotificationShadeWindowController> provider35) {
        return new MiuiNotificationPanelViewController_Factory(provider, provider2, provider3, provider4, provider5, provider6, provider7, provider8, provider9, provider10, provider11, provider12, provider13, provider14, provider15, provider16, provider17, provider18, provider19, provider20, provider21, provider22, provider23, provider24, provider25, provider26, provider27, provider28, provider29, provider30, provider31, provider32, provider33, provider34, provider35);
    }
}
