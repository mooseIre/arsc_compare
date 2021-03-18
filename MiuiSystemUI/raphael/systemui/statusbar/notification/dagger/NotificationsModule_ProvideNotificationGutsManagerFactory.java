package com.android.systemui.statusbar.notification.dagger;

import android.app.INotificationManager;
import android.content.Context;
import android.content.pm.LauncherApps;
import android.content.pm.ShortcutManager;
import android.os.Handler;
import android.view.accessibility.AccessibilityManager;
import com.android.internal.logging.UiEventLogger;
import com.android.systemui.bubbles.BubbleController;
import com.android.systemui.settings.CurrentUserContextTracker;
import com.android.systemui.statusbar.notification.VisualStabilityManager;
import com.android.systemui.statusbar.notification.collection.provider.HighPriorityProvider;
import com.android.systemui.statusbar.notification.row.ChannelEditorDialogController;
import com.android.systemui.statusbar.notification.row.NotificationGutsManager;
import com.android.systemui.statusbar.notification.row.PriorityOnboardingDialogController;
import com.android.systemui.statusbar.phone.StatusBar;
import dagger.Lazy;
import dagger.internal.DoubleCheck;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import javax.inject.Provider;

public final class NotificationsModule_ProvideNotificationGutsManagerFactory implements Factory<NotificationGutsManager> {
    private final Provider<AccessibilityManager> accessibilityManagerProvider;
    private final Provider<Handler> bgHandlerProvider;
    private final Provider<BubbleController> bubbleControllerProvider;
    private final Provider<PriorityOnboardingDialogController.Builder> builderProvider;
    private final Provider<ChannelEditorDialogController> channelEditorDialogControllerProvider;
    private final Provider<Context> contextProvider;
    private final Provider<CurrentUserContextTracker> contextTrackerProvider;
    private final Provider<HighPriorityProvider> highPriorityProvider;
    private final Provider<LauncherApps> launcherAppsProvider;
    private final Provider<Handler> mainHandlerProvider;
    private final Provider<INotificationManager> notificationManagerProvider;
    private final Provider<ShortcutManager> shortcutManagerProvider;
    private final Provider<StatusBar> statusBarLazyProvider;
    private final Provider<UiEventLogger> uiEventLoggerProvider;
    private final Provider<VisualStabilityManager> visualStabilityManagerProvider;

    public NotificationsModule_ProvideNotificationGutsManagerFactory(Provider<Context> provider, Provider<VisualStabilityManager> provider2, Provider<StatusBar> provider3, Provider<Handler> provider4, Provider<Handler> provider5, Provider<AccessibilityManager> provider6, Provider<HighPriorityProvider> provider7, Provider<INotificationManager> provider8, Provider<LauncherApps> provider9, Provider<ShortcutManager> provider10, Provider<ChannelEditorDialogController> provider11, Provider<CurrentUserContextTracker> provider12, Provider<PriorityOnboardingDialogController.Builder> provider13, Provider<BubbleController> provider14, Provider<UiEventLogger> provider15) {
        this.contextProvider = provider;
        this.visualStabilityManagerProvider = provider2;
        this.statusBarLazyProvider = provider3;
        this.mainHandlerProvider = provider4;
        this.bgHandlerProvider = provider5;
        this.accessibilityManagerProvider = provider6;
        this.highPriorityProvider = provider7;
        this.notificationManagerProvider = provider8;
        this.launcherAppsProvider = provider9;
        this.shortcutManagerProvider = provider10;
        this.channelEditorDialogControllerProvider = provider11;
        this.contextTrackerProvider = provider12;
        this.builderProvider = provider13;
        this.bubbleControllerProvider = provider14;
        this.uiEventLoggerProvider = provider15;
    }

    @Override // javax.inject.Provider
    public NotificationGutsManager get() {
        return provideInstance(this.contextProvider, this.visualStabilityManagerProvider, this.statusBarLazyProvider, this.mainHandlerProvider, this.bgHandlerProvider, this.accessibilityManagerProvider, this.highPriorityProvider, this.notificationManagerProvider, this.launcherAppsProvider, this.shortcutManagerProvider, this.channelEditorDialogControllerProvider, this.contextTrackerProvider, this.builderProvider, this.bubbleControllerProvider, this.uiEventLoggerProvider);
    }

    public static NotificationGutsManager provideInstance(Provider<Context> provider, Provider<VisualStabilityManager> provider2, Provider<StatusBar> provider3, Provider<Handler> provider4, Provider<Handler> provider5, Provider<AccessibilityManager> provider6, Provider<HighPriorityProvider> provider7, Provider<INotificationManager> provider8, Provider<LauncherApps> provider9, Provider<ShortcutManager> provider10, Provider<ChannelEditorDialogController> provider11, Provider<CurrentUserContextTracker> provider12, Provider<PriorityOnboardingDialogController.Builder> provider13, Provider<BubbleController> provider14, Provider<UiEventLogger> provider15) {
        return proxyProvideNotificationGutsManager(provider.get(), provider2.get(), DoubleCheck.lazy(provider3), provider4.get(), provider5.get(), provider6.get(), provider7.get(), provider8.get(), provider9.get(), provider10.get(), provider11.get(), provider12.get(), provider13, provider14.get(), provider15.get());
    }

    public static NotificationsModule_ProvideNotificationGutsManagerFactory create(Provider<Context> provider, Provider<VisualStabilityManager> provider2, Provider<StatusBar> provider3, Provider<Handler> provider4, Provider<Handler> provider5, Provider<AccessibilityManager> provider6, Provider<HighPriorityProvider> provider7, Provider<INotificationManager> provider8, Provider<LauncherApps> provider9, Provider<ShortcutManager> provider10, Provider<ChannelEditorDialogController> provider11, Provider<CurrentUserContextTracker> provider12, Provider<PriorityOnboardingDialogController.Builder> provider13, Provider<BubbleController> provider14, Provider<UiEventLogger> provider15) {
        return new NotificationsModule_ProvideNotificationGutsManagerFactory(provider, provider2, provider3, provider4, provider5, provider6, provider7, provider8, provider9, provider10, provider11, provider12, provider13, provider14, provider15);
    }

    public static NotificationGutsManager proxyProvideNotificationGutsManager(Context context, VisualStabilityManager visualStabilityManager, Lazy<StatusBar> lazy, Handler handler, Handler handler2, AccessibilityManager accessibilityManager, HighPriorityProvider highPriorityProvider2, INotificationManager iNotificationManager, LauncherApps launcherApps, ShortcutManager shortcutManager, ChannelEditorDialogController channelEditorDialogController, CurrentUserContextTracker currentUserContextTracker, Provider<PriorityOnboardingDialogController.Builder> provider, BubbleController bubbleController, UiEventLogger uiEventLogger) {
        NotificationGutsManager provideNotificationGutsManager = NotificationsModule.provideNotificationGutsManager(context, visualStabilityManager, lazy, handler, handler2, accessibilityManager, highPriorityProvider2, iNotificationManager, launcherApps, shortcutManager, channelEditorDialogController, currentUserContextTracker, provider, bubbleController, uiEventLogger);
        Preconditions.checkNotNull(provideNotificationGutsManager, "Cannot return null from a non-@Nullable @Provides method");
        return provideNotificationGutsManager;
    }
}
