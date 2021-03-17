package com.android.systemui.bubbles.dagger;

import android.app.INotificationManager;
import android.content.Context;
import android.content.pm.LauncherApps;
import android.view.WindowManager;
import com.android.internal.statusbar.IStatusBarService;
import com.android.systemui.bubbles.BubbleController;
import com.android.systemui.bubbles.BubbleData;
import com.android.systemui.bubbles.BubbleDataRepository;
import com.android.systemui.dump.DumpManager;
import com.android.systemui.model.SysUiState;
import com.android.systemui.plugins.statusbar.StatusBarStateController;
import com.android.systemui.statusbar.FeatureFlags;
import com.android.systemui.statusbar.NotificationLockscreenUserManager;
import com.android.systemui.statusbar.notification.NotificationEntryManager;
import com.android.systemui.statusbar.notification.collection.NotifPipeline;
import com.android.systemui.statusbar.notification.interruption.NotificationInterruptStateProvider;
import com.android.systemui.statusbar.phone.NotificationGroupManager;
import com.android.systemui.statusbar.phone.NotificationShadeWindowController;
import com.android.systemui.statusbar.phone.ShadeController;
import com.android.systemui.statusbar.policy.ConfigurationController;
import com.android.systemui.statusbar.policy.ZenModeController;
import com.android.systemui.util.FloatingContentCoordinator;

public interface BubbleModule {
    static default BubbleController newBubbleController(Context context, NotificationShadeWindowController notificationShadeWindowController, StatusBarStateController statusBarStateController, ShadeController shadeController, BubbleData bubbleData, ConfigurationController configurationController, NotificationInterruptStateProvider notificationInterruptStateProvider, ZenModeController zenModeController, NotificationLockscreenUserManager notificationLockscreenUserManager, NotificationGroupManager notificationGroupManager, NotificationEntryManager notificationEntryManager, NotifPipeline notifPipeline, FeatureFlags featureFlags, DumpManager dumpManager, FloatingContentCoordinator floatingContentCoordinator, BubbleDataRepository bubbleDataRepository, SysUiState sysUiState, INotificationManager iNotificationManager, IStatusBarService iStatusBarService, WindowManager windowManager, LauncherApps launcherApps) {
        return new BubbleController(context, notificationShadeWindowController, statusBarStateController, shadeController, bubbleData, null, configurationController, notificationInterruptStateProvider, zenModeController, notificationLockscreenUserManager, notificationGroupManager, notificationEntryManager, notifPipeline, featureFlags, dumpManager, floatingContentCoordinator, bubbleDataRepository, sysUiState, iNotificationManager, iStatusBarService, windowManager, launcherApps);
    }
}
