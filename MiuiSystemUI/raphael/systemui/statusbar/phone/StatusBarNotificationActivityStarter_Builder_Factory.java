package com.android.systemui.statusbar.phone;

import android.app.KeyguardManager;
import android.content.Context;
import android.os.Handler;
import android.service.dreams.IDreamManager;
import com.android.internal.logging.MetricsLogger;
import com.android.internal.widget.LockPatternUtils;
import com.android.systemui.ActivityIntentHelper;
import com.android.systemui.assist.AssistManager;
import com.android.systemui.bubbles.BubbleController;
import com.android.systemui.plugins.ActivityStarter;
import com.android.systemui.plugins.statusbar.StatusBarStateController;
import com.android.systemui.statusbar.CommandQueue;
import com.android.systemui.statusbar.FeatureFlags;
import com.android.systemui.statusbar.NotificationClickNotifier;
import com.android.systemui.statusbar.NotificationLockscreenUserManager;
import com.android.systemui.statusbar.NotificationRemoteInputManager;
import com.android.systemui.statusbar.notification.NotificationEntryManager;
import com.android.systemui.statusbar.notification.collection.NotifCollection;
import com.android.systemui.statusbar.notification.collection.NotifPipeline;
import com.android.systemui.statusbar.notification.interruption.NotificationInterruptStateProvider;
import com.android.systemui.statusbar.phone.StatusBarNotificationActivityStarter;
import com.android.systemui.statusbar.policy.KeyguardStateController;
import dagger.internal.DoubleCheck;
import dagger.internal.Factory;
import java.util.concurrent.Executor;
import javax.inject.Provider;

public final class StatusBarNotificationActivityStarter_Builder_Factory implements Factory<StatusBarNotificationActivityStarter.Builder> {
    private final Provider<ActivityIntentHelper> activityIntentHelperProvider;
    private final Provider<ActivityStarter> activityStarterProvider;
    private final Provider<AssistManager> assistManagerLazyProvider;
    private final Provider<Handler> backgroundHandlerProvider;
    private final Provider<BubbleController> bubbleControllerProvider;
    private final Provider<NotificationClickNotifier> clickNotifierProvider;
    private final Provider<CommandQueue> commandQueueProvider;
    private final Provider<Context> contextProvider;
    private final Provider<IDreamManager> dreamManagerProvider;
    private final Provider<NotificationEntryManager> entryManagerProvider;
    private final Provider<FeatureFlags> featureFlagsProvider;
    private final Provider<NotificationGroupManager> groupManagerProvider;
    private final Provider<HeadsUpManagerPhone> headsUpManagerProvider;
    private final Provider<KeyguardManager> keyguardManagerProvider;
    private final Provider<KeyguardStateController> keyguardStateControllerProvider;
    private final Provider<LockPatternUtils> lockPatternUtilsProvider;
    private final Provider<NotificationLockscreenUserManager> lockscreenUserManagerProvider;
    private final Provider<StatusBarNotificationActivityStarterLogger> loggerProvider;
    private final Provider<Handler> mainThreadHandlerProvider;
    private final Provider<MetricsLogger> metricsLoggerProvider;
    private final Provider<NotifCollection> notifCollectionProvider;
    private final Provider<NotifPipeline> notifPipelineProvider;
    private final Provider<NotificationInterruptStateProvider> notificationInterruptStateProvider;
    private final Provider<StatusBarRemoteInputCallback> remoteInputCallbackProvider;
    private final Provider<NotificationRemoteInputManager> remoteInputManagerProvider;
    private final Provider<ShadeController> shadeControllerProvider;
    private final Provider<StatusBarKeyguardViewManager> statusBarKeyguardViewManagerProvider;
    private final Provider<StatusBarStateController> statusBarStateControllerProvider;
    private final Provider<Executor> uiBgExecutorProvider;

    public StatusBarNotificationActivityStarter_Builder_Factory(Provider<Context> provider, Provider<CommandQueue> provider2, Provider<Handler> provider3, Provider<Handler> provider4, Provider<Executor> provider5, Provider<NotificationEntryManager> provider6, Provider<NotifPipeline> provider7, Provider<NotifCollection> provider8, Provider<HeadsUpManagerPhone> provider9, Provider<ActivityStarter> provider10, Provider<NotificationClickNotifier> provider11, Provider<StatusBarStateController> provider12, Provider<StatusBarKeyguardViewManager> provider13, Provider<KeyguardManager> provider14, Provider<IDreamManager> provider15, Provider<BubbleController> provider16, Provider<AssistManager> provider17, Provider<NotificationRemoteInputManager> provider18, Provider<NotificationGroupManager> provider19, Provider<NotificationLockscreenUserManager> provider20, Provider<ShadeController> provider21, Provider<KeyguardStateController> provider22, Provider<NotificationInterruptStateProvider> provider23, Provider<LockPatternUtils> provider24, Provider<StatusBarRemoteInputCallback> provider25, Provider<ActivityIntentHelper> provider26, Provider<FeatureFlags> provider27, Provider<MetricsLogger> provider28, Provider<StatusBarNotificationActivityStarterLogger> provider29) {
        this.contextProvider = provider;
        this.commandQueueProvider = provider2;
        this.mainThreadHandlerProvider = provider3;
        this.backgroundHandlerProvider = provider4;
        this.uiBgExecutorProvider = provider5;
        this.entryManagerProvider = provider6;
        this.notifPipelineProvider = provider7;
        this.notifCollectionProvider = provider8;
        this.headsUpManagerProvider = provider9;
        this.activityStarterProvider = provider10;
        this.clickNotifierProvider = provider11;
        this.statusBarStateControllerProvider = provider12;
        this.statusBarKeyguardViewManagerProvider = provider13;
        this.keyguardManagerProvider = provider14;
        this.dreamManagerProvider = provider15;
        this.bubbleControllerProvider = provider16;
        this.assistManagerLazyProvider = provider17;
        this.remoteInputManagerProvider = provider18;
        this.groupManagerProvider = provider19;
        this.lockscreenUserManagerProvider = provider20;
        this.shadeControllerProvider = provider21;
        this.keyguardStateControllerProvider = provider22;
        this.notificationInterruptStateProvider = provider23;
        this.lockPatternUtilsProvider = provider24;
        this.remoteInputCallbackProvider = provider25;
        this.activityIntentHelperProvider = provider26;
        this.featureFlagsProvider = provider27;
        this.metricsLoggerProvider = provider28;
        this.loggerProvider = provider29;
    }

    @Override // javax.inject.Provider
    public StatusBarNotificationActivityStarter.Builder get() {
        return provideInstance(this.contextProvider, this.commandQueueProvider, this.mainThreadHandlerProvider, this.backgroundHandlerProvider, this.uiBgExecutorProvider, this.entryManagerProvider, this.notifPipelineProvider, this.notifCollectionProvider, this.headsUpManagerProvider, this.activityStarterProvider, this.clickNotifierProvider, this.statusBarStateControllerProvider, this.statusBarKeyguardViewManagerProvider, this.keyguardManagerProvider, this.dreamManagerProvider, this.bubbleControllerProvider, this.assistManagerLazyProvider, this.remoteInputManagerProvider, this.groupManagerProvider, this.lockscreenUserManagerProvider, this.shadeControllerProvider, this.keyguardStateControllerProvider, this.notificationInterruptStateProvider, this.lockPatternUtilsProvider, this.remoteInputCallbackProvider, this.activityIntentHelperProvider, this.featureFlagsProvider, this.metricsLoggerProvider, this.loggerProvider);
    }

    public static StatusBarNotificationActivityStarter.Builder provideInstance(Provider<Context> provider, Provider<CommandQueue> provider2, Provider<Handler> provider3, Provider<Handler> provider4, Provider<Executor> provider5, Provider<NotificationEntryManager> provider6, Provider<NotifPipeline> provider7, Provider<NotifCollection> provider8, Provider<HeadsUpManagerPhone> provider9, Provider<ActivityStarter> provider10, Provider<NotificationClickNotifier> provider11, Provider<StatusBarStateController> provider12, Provider<StatusBarKeyguardViewManager> provider13, Provider<KeyguardManager> provider14, Provider<IDreamManager> provider15, Provider<BubbleController> provider16, Provider<AssistManager> provider17, Provider<NotificationRemoteInputManager> provider18, Provider<NotificationGroupManager> provider19, Provider<NotificationLockscreenUserManager> provider20, Provider<ShadeController> provider21, Provider<KeyguardStateController> provider22, Provider<NotificationInterruptStateProvider> provider23, Provider<LockPatternUtils> provider24, Provider<StatusBarRemoteInputCallback> provider25, Provider<ActivityIntentHelper> provider26, Provider<FeatureFlags> provider27, Provider<MetricsLogger> provider28, Provider<StatusBarNotificationActivityStarterLogger> provider29) {
        return new StatusBarNotificationActivityStarter.Builder(provider.get(), provider2.get(), provider3.get(), provider4.get(), provider5.get(), provider6.get(), provider7.get(), provider8.get(), provider9.get(), provider10.get(), provider11.get(), provider12.get(), provider13.get(), provider14.get(), provider15.get(), provider16.get(), DoubleCheck.lazy(provider17), provider18.get(), provider19.get(), provider20.get(), provider21.get(), provider22.get(), provider23.get(), provider24.get(), provider25.get(), provider26.get(), provider27.get(), provider28.get(), provider29.get());
    }

    public static StatusBarNotificationActivityStarter_Builder_Factory create(Provider<Context> provider, Provider<CommandQueue> provider2, Provider<Handler> provider3, Provider<Handler> provider4, Provider<Executor> provider5, Provider<NotificationEntryManager> provider6, Provider<NotifPipeline> provider7, Provider<NotifCollection> provider8, Provider<HeadsUpManagerPhone> provider9, Provider<ActivityStarter> provider10, Provider<NotificationClickNotifier> provider11, Provider<StatusBarStateController> provider12, Provider<StatusBarKeyguardViewManager> provider13, Provider<KeyguardManager> provider14, Provider<IDreamManager> provider15, Provider<BubbleController> provider16, Provider<AssistManager> provider17, Provider<NotificationRemoteInputManager> provider18, Provider<NotificationGroupManager> provider19, Provider<NotificationLockscreenUserManager> provider20, Provider<ShadeController> provider21, Provider<KeyguardStateController> provider22, Provider<NotificationInterruptStateProvider> provider23, Provider<LockPatternUtils> provider24, Provider<StatusBarRemoteInputCallback> provider25, Provider<ActivityIntentHelper> provider26, Provider<FeatureFlags> provider27, Provider<MetricsLogger> provider28, Provider<StatusBarNotificationActivityStarterLogger> provider29) {
        return new StatusBarNotificationActivityStarter_Builder_Factory(provider, provider2, provider3, provider4, provider5, provider6, provider7, provider8, provider9, provider10, provider11, provider12, provider13, provider14, provider15, provider16, provider17, provider18, provider19, provider20, provider21, provider22, provider23, provider24, provider25, provider26, provider27, provider28, provider29);
    }
}
