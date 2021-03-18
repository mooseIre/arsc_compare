package com.android.systemui.statusbar.notification.dagger;

import android.app.INotificationManager;
import android.content.Context;
import android.content.pm.LauncherApps;
import android.content.pm.ShortcutManager;
import android.os.Handler;
import android.view.accessibility.AccessibilityManager;
import com.android.internal.logging.MetricsLogger;
import com.android.internal.logging.UiEventLogger;
import com.android.systemui.C0010R$bool;
import com.android.systemui.bubbles.BubbleController;
import com.android.systemui.plugins.statusbar.StatusBarStateController;
import com.android.systemui.settings.CurrentUserContextTracker;
import com.android.systemui.statusbar.FeatureFlags;
import com.android.systemui.statusbar.NotificationListener;
import com.android.systemui.statusbar.NotificationRemoteInputManager;
import com.android.systemui.statusbar.notification.ForegroundServiceDismissalFeatureController;
import com.android.systemui.statusbar.notification.NotificationEntryManager;
import com.android.systemui.statusbar.notification.NotificationEntryManagerLogger;
import com.android.systemui.statusbar.notification.VisualStabilityManager;
import com.android.systemui.statusbar.notification.collection.NotifPipeline;
import com.android.systemui.statusbar.notification.collection.NotificationRankingManager;
import com.android.systemui.statusbar.notification.collection.inflation.NotificationRowBinder;
import com.android.systemui.statusbar.notification.collection.notifcollection.CommonNotifCollection;
import com.android.systemui.statusbar.notification.collection.provider.HighPriorityProvider;
import com.android.systemui.statusbar.notification.init.NotificationsController;
import com.android.systemui.statusbar.notification.init.NotificationsControllerImpl;
import com.android.systemui.statusbar.notification.init.NotificationsControllerStub;
import com.android.systemui.statusbar.notification.logging.NotificationLogger;
import com.android.systemui.statusbar.notification.logging.NotificationPanelLogger;
import com.android.systemui.statusbar.notification.logging.NotificationPanelLoggerImpl;
import com.android.systemui.statusbar.notification.row.ChannelEditorDialogController;
import com.android.systemui.statusbar.notification.row.NotificationBlockingHelperManager;
import com.android.systemui.statusbar.notification.row.NotificationGutsManager;
import com.android.systemui.statusbar.notification.row.PriorityOnboardingDialogController;
import com.android.systemui.statusbar.phone.NotificationGroupManager;
import com.android.systemui.statusbar.phone.StatusBar;
import com.android.systemui.util.leak.LeakDetector;
import dagger.Lazy;
import java.util.concurrent.Executor;
import javax.inject.Provider;

public interface NotificationsModule {
    static default NotificationEntryManager provideNotificationEntryManager(NotificationEntryManagerLogger notificationEntryManagerLogger, NotificationGroupManager notificationGroupManager, NotificationRankingManager notificationRankingManager, NotificationEntryManager.KeyguardEnvironment keyguardEnvironment, FeatureFlags featureFlags, Lazy<NotificationRowBinder> lazy, Lazy<NotificationRemoteInputManager> lazy2, LeakDetector leakDetector, ForegroundServiceDismissalFeatureController foregroundServiceDismissalFeatureController) {
        return new NotificationEntryManager(notificationEntryManagerLogger, notificationGroupManager, notificationRankingManager, keyguardEnvironment, featureFlags, lazy, lazy2, leakDetector, foregroundServiceDismissalFeatureController);
    }

    static default NotificationGutsManager provideNotificationGutsManager(Context context, VisualStabilityManager visualStabilityManager, Lazy<StatusBar> lazy, Handler handler, Handler handler2, AccessibilityManager accessibilityManager, HighPriorityProvider highPriorityProvider, INotificationManager iNotificationManager, LauncherApps launcherApps, ShortcutManager shortcutManager, ChannelEditorDialogController channelEditorDialogController, CurrentUserContextTracker currentUserContextTracker, Provider<PriorityOnboardingDialogController.Builder> provider, BubbleController bubbleController, UiEventLogger uiEventLogger) {
        return new NotificationGutsManager(context, visualStabilityManager, lazy, handler, handler2, accessibilityManager, highPriorityProvider, iNotificationManager, launcherApps, shortcutManager, channelEditorDialogController, currentUserContextTracker, provider, bubbleController, uiEventLogger);
    }

    static default VisualStabilityManager provideVisualStabilityManager(NotificationEntryManager notificationEntryManager, Handler handler) {
        return new VisualStabilityManager(notificationEntryManager, handler);
    }

    static default NotificationLogger provideNotificationLogger(NotificationListener notificationListener, Executor executor, NotificationEntryManager notificationEntryManager, StatusBarStateController statusBarStateController, NotificationLogger.ExpansionStateLogger expansionStateLogger, NotificationPanelLogger notificationPanelLogger) {
        return new NotificationLogger(notificationListener, executor, notificationEntryManager, statusBarStateController, expansionStateLogger, notificationPanelLogger);
    }

    static default NotificationPanelLogger provideNotificationPanelLogger() {
        return new NotificationPanelLoggerImpl();
    }

    static default NotificationBlockingHelperManager provideNotificationBlockingHelperManager(Context context, NotificationGutsManager notificationGutsManager, NotificationEntryManager notificationEntryManager, MetricsLogger metricsLogger) {
        return new NotificationBlockingHelperManager(context, notificationGutsManager, notificationEntryManager, metricsLogger);
    }

    static default NotificationsController provideNotificationsController(Context context, Lazy<NotificationsControllerImpl> lazy, Lazy<NotificationsControllerStub> lazy2) {
        if (context.getResources().getBoolean(C0010R$bool.config_renderNotifications)) {
            return lazy.get();
        }
        return lazy2.get();
    }

    static default CommonNotifCollection provideCommonNotifCollection(FeatureFlags featureFlags, Lazy<NotifPipeline> lazy, NotificationEntryManager notificationEntryManager) {
        return featureFlags.isNewNotifPipelineRenderingEnabled() ? lazy.get() : notificationEntryManager;
    }
}
