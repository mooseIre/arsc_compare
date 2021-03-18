package com.android.systemui.globalactions;

import android.app.IActivityManager;
import android.app.admin.DevicePolicyManager;
import android.app.trust.TrustManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.res.Resources;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.os.Handler;
import android.os.UserManager;
import android.os.Vibrator;
import android.service.dreams.IDreamManager;
import android.telecom.TelecomManager;
import android.telephony.TelephonyManager;
import android.view.IWindowManager;
import com.android.internal.logging.MetricsLogger;
import com.android.internal.logging.UiEventLogger;
import com.android.internal.statusbar.IStatusBarService;
import com.android.internal.widget.LockPatternUtils;
import com.android.systemui.broadcast.BroadcastDispatcher;
import com.android.systemui.colorextraction.SysuiColorExtractor;
import com.android.systemui.controls.dagger.ControlsComponent;
import com.android.systemui.model.SysUiState;
import com.android.systemui.plugins.ActivityStarter;
import com.android.systemui.plugins.GlobalActions;
import com.android.systemui.settings.CurrentUserContextTracker;
import com.android.systemui.statusbar.NotificationShadeDepthController;
import com.android.systemui.statusbar.phone.NotificationShadeWindowController;
import com.android.systemui.statusbar.policy.ConfigurationController;
import com.android.systemui.statusbar.policy.KeyguardStateController;
import com.android.systemui.util.RingerModeTracker;
import dagger.internal.Factory;
import java.util.concurrent.Executor;
import javax.inject.Provider;

public final class GlobalActionsDialog_Factory implements Factory<GlobalActionsDialog> {
    private final Provider<ActivityStarter> activityStarterProvider;
    private final Provider<AudioManager> audioManagerProvider;
    private final Provider<Executor> backgroundExecutorProvider;
    private final Provider<BroadcastDispatcher> broadcastDispatcherProvider;
    private final Provider<SysuiColorExtractor> colorExtractorProvider;
    private final Provider<ConfigurationController> configurationControllerProvider;
    private final Provider<ConnectivityManager> connectivityManagerProvider;
    private final Provider<ContentResolver> contentResolverProvider;
    private final Provider<Context> contextProvider;
    private final Provider<ControlsComponent> controlsComponentProvider;
    private final Provider<CurrentUserContextTracker> currentUserContextTrackerProvider;
    private final Provider<NotificationShadeDepthController> depthControllerProvider;
    private final Provider<DevicePolicyManager> devicePolicyManagerProvider;
    private final Provider<Handler> handlerProvider;
    private final Provider<IActivityManager> iActivityManagerProvider;
    private final Provider<IDreamManager> iDreamManagerProvider;
    private final Provider<IWindowManager> iWindowManagerProvider;
    private final Provider<KeyguardStateController> keyguardStateControllerProvider;
    private final Provider<LockPatternUtils> lockPatternUtilsProvider;
    private final Provider<MetricsLogger> metricsLoggerProvider;
    private final Provider<NotificationShadeWindowController> notificationShadeWindowControllerProvider;
    private final Provider<Resources> resourcesProvider;
    private final Provider<RingerModeTracker> ringerModeTrackerProvider;
    private final Provider<IStatusBarService> statusBarServiceProvider;
    private final Provider<SysUiState> sysUiStateProvider;
    private final Provider<TelecomManager> telecomManagerProvider;
    private final Provider<TelephonyManager> telephonyManagerProvider;
    private final Provider<TrustManager> trustManagerProvider;
    private final Provider<UiEventLogger> uiEventLoggerProvider;
    private final Provider<UserManager> userManagerProvider;
    private final Provider<Vibrator> vibratorProvider;
    private final Provider<GlobalActions.GlobalActionsManager> windowManagerFuncsProvider;

    public GlobalActionsDialog_Factory(Provider<Context> provider, Provider<GlobalActions.GlobalActionsManager> provider2, Provider<AudioManager> provider3, Provider<IDreamManager> provider4, Provider<DevicePolicyManager> provider5, Provider<LockPatternUtils> provider6, Provider<BroadcastDispatcher> provider7, Provider<ConnectivityManager> provider8, Provider<TelephonyManager> provider9, Provider<ContentResolver> provider10, Provider<Vibrator> provider11, Provider<Resources> provider12, Provider<ConfigurationController> provider13, Provider<ActivityStarter> provider14, Provider<KeyguardStateController> provider15, Provider<UserManager> provider16, Provider<TrustManager> provider17, Provider<IActivityManager> provider18, Provider<TelecomManager> provider19, Provider<MetricsLogger> provider20, Provider<NotificationShadeDepthController> provider21, Provider<SysuiColorExtractor> provider22, Provider<IStatusBarService> provider23, Provider<NotificationShadeWindowController> provider24, Provider<IWindowManager> provider25, Provider<Executor> provider26, Provider<UiEventLogger> provider27, Provider<RingerModeTracker> provider28, Provider<SysUiState> provider29, Provider<Handler> provider30, Provider<ControlsComponent> provider31, Provider<CurrentUserContextTracker> provider32) {
        this.contextProvider = provider;
        this.windowManagerFuncsProvider = provider2;
        this.audioManagerProvider = provider3;
        this.iDreamManagerProvider = provider4;
        this.devicePolicyManagerProvider = provider5;
        this.lockPatternUtilsProvider = provider6;
        this.broadcastDispatcherProvider = provider7;
        this.connectivityManagerProvider = provider8;
        this.telephonyManagerProvider = provider9;
        this.contentResolverProvider = provider10;
        this.vibratorProvider = provider11;
        this.resourcesProvider = provider12;
        this.configurationControllerProvider = provider13;
        this.activityStarterProvider = provider14;
        this.keyguardStateControllerProvider = provider15;
        this.userManagerProvider = provider16;
        this.trustManagerProvider = provider17;
        this.iActivityManagerProvider = provider18;
        this.telecomManagerProvider = provider19;
        this.metricsLoggerProvider = provider20;
        this.depthControllerProvider = provider21;
        this.colorExtractorProvider = provider22;
        this.statusBarServiceProvider = provider23;
        this.notificationShadeWindowControllerProvider = provider24;
        this.iWindowManagerProvider = provider25;
        this.backgroundExecutorProvider = provider26;
        this.uiEventLoggerProvider = provider27;
        this.ringerModeTrackerProvider = provider28;
        this.sysUiStateProvider = provider29;
        this.handlerProvider = provider30;
        this.controlsComponentProvider = provider31;
        this.currentUserContextTrackerProvider = provider32;
    }

    @Override // javax.inject.Provider
    public GlobalActionsDialog get() {
        return provideInstance(this.contextProvider, this.windowManagerFuncsProvider, this.audioManagerProvider, this.iDreamManagerProvider, this.devicePolicyManagerProvider, this.lockPatternUtilsProvider, this.broadcastDispatcherProvider, this.connectivityManagerProvider, this.telephonyManagerProvider, this.contentResolverProvider, this.vibratorProvider, this.resourcesProvider, this.configurationControllerProvider, this.activityStarterProvider, this.keyguardStateControllerProvider, this.userManagerProvider, this.trustManagerProvider, this.iActivityManagerProvider, this.telecomManagerProvider, this.metricsLoggerProvider, this.depthControllerProvider, this.colorExtractorProvider, this.statusBarServiceProvider, this.notificationShadeWindowControllerProvider, this.iWindowManagerProvider, this.backgroundExecutorProvider, this.uiEventLoggerProvider, this.ringerModeTrackerProvider, this.sysUiStateProvider, this.handlerProvider, this.controlsComponentProvider, this.currentUserContextTrackerProvider);
    }

    public static GlobalActionsDialog provideInstance(Provider<Context> provider, Provider<GlobalActions.GlobalActionsManager> provider2, Provider<AudioManager> provider3, Provider<IDreamManager> provider4, Provider<DevicePolicyManager> provider5, Provider<LockPatternUtils> provider6, Provider<BroadcastDispatcher> provider7, Provider<ConnectivityManager> provider8, Provider<TelephonyManager> provider9, Provider<ContentResolver> provider10, Provider<Vibrator> provider11, Provider<Resources> provider12, Provider<ConfigurationController> provider13, Provider<ActivityStarter> provider14, Provider<KeyguardStateController> provider15, Provider<UserManager> provider16, Provider<TrustManager> provider17, Provider<IActivityManager> provider18, Provider<TelecomManager> provider19, Provider<MetricsLogger> provider20, Provider<NotificationShadeDepthController> provider21, Provider<SysuiColorExtractor> provider22, Provider<IStatusBarService> provider23, Provider<NotificationShadeWindowController> provider24, Provider<IWindowManager> provider25, Provider<Executor> provider26, Provider<UiEventLogger> provider27, Provider<RingerModeTracker> provider28, Provider<SysUiState> provider29, Provider<Handler> provider30, Provider<ControlsComponent> provider31, Provider<CurrentUserContextTracker> provider32) {
        return new GlobalActionsDialog(provider.get(), provider2.get(), provider3.get(), provider4.get(), provider5.get(), provider6.get(), provider7.get(), provider8.get(), provider9.get(), provider10.get(), provider11.get(), provider12.get(), provider13.get(), provider14.get(), provider15.get(), provider16.get(), provider17.get(), provider18.get(), provider19.get(), provider20.get(), provider21.get(), provider22.get(), provider23.get(), provider24.get(), provider25.get(), provider26.get(), provider27.get(), provider28.get(), provider29.get(), provider30.get(), provider31.get(), provider32.get());
    }

    public static GlobalActionsDialog_Factory create(Provider<Context> provider, Provider<GlobalActions.GlobalActionsManager> provider2, Provider<AudioManager> provider3, Provider<IDreamManager> provider4, Provider<DevicePolicyManager> provider5, Provider<LockPatternUtils> provider6, Provider<BroadcastDispatcher> provider7, Provider<ConnectivityManager> provider8, Provider<TelephonyManager> provider9, Provider<ContentResolver> provider10, Provider<Vibrator> provider11, Provider<Resources> provider12, Provider<ConfigurationController> provider13, Provider<ActivityStarter> provider14, Provider<KeyguardStateController> provider15, Provider<UserManager> provider16, Provider<TrustManager> provider17, Provider<IActivityManager> provider18, Provider<TelecomManager> provider19, Provider<MetricsLogger> provider20, Provider<NotificationShadeDepthController> provider21, Provider<SysuiColorExtractor> provider22, Provider<IStatusBarService> provider23, Provider<NotificationShadeWindowController> provider24, Provider<IWindowManager> provider25, Provider<Executor> provider26, Provider<UiEventLogger> provider27, Provider<RingerModeTracker> provider28, Provider<SysUiState> provider29, Provider<Handler> provider30, Provider<ControlsComponent> provider31, Provider<CurrentUserContextTracker> provider32) {
        return new GlobalActionsDialog_Factory(provider, provider2, provider3, provider4, provider5, provider6, provider7, provider8, provider9, provider10, provider11, provider12, provider13, provider14, provider15, provider16, provider17, provider18, provider19, provider20, provider21, provider22, provider23, provider24, provider25, provider26, provider27, provider28, provider29, provider30, provider31, provider32);
    }
}
