package com.android.systemui.statusbar.dagger;

import android.app.NotificationManager;
import android.content.Context;
import android.os.Handler;
import com.android.internal.statusbar.IStatusBarService;
import com.android.systemui.bubbles.BubbleController;
import com.android.systemui.media.MediaDataManager;
import com.android.systemui.plugins.statusbar.StatusBarStateController;
import com.android.systemui.statusbar.ActionClickLogger;
import com.android.systemui.statusbar.CommandQueue;
import com.android.systemui.statusbar.MediaArtworkProcessor;
import com.android.systemui.statusbar.NotificationClickNotifier;
import com.android.systemui.statusbar.NotificationListener;
import com.android.systemui.statusbar.NotificationLockscreenUserManager;
import com.android.systemui.statusbar.NotificationMediaManager;
import com.android.systemui.statusbar.NotificationRemoteInputManager;
import com.android.systemui.statusbar.NotificationViewHierarchyManager;
import com.android.systemui.statusbar.SmartReplyController;
import com.android.systemui.statusbar.notification.DynamicChildBindController;
import com.android.systemui.statusbar.notification.DynamicPrivacyController;
import com.android.systemui.statusbar.notification.NotificationEntryManager;
import com.android.systemui.statusbar.notification.VisualStabilityManager;
import com.android.systemui.statusbar.notification.collection.inflation.LowPriorityInflationHelper;
import com.android.systemui.statusbar.notification.stack.ForegroundServiceSectionController;
import com.android.systemui.statusbar.phone.KeyguardBypassController;
import com.android.systemui.statusbar.phone.NotificationGroupManager;
import com.android.systemui.statusbar.phone.NotificationShadeWindowController;
import com.android.systemui.statusbar.phone.StatusBar;
import com.android.systemui.statusbar.policy.RemoteInputUriController;
import com.android.systemui.tracing.ProtoTracer;
import com.android.systemui.util.DeviceConfigProxy;
import com.android.systemui.util.concurrency.DelayableExecutor;
import dagger.Lazy;

public interface StatusBarDependenciesModule {
    static default NotificationRemoteInputManager provideNotificationRemoteInputManager(Context context, NotificationLockscreenUserManager notificationLockscreenUserManager, SmartReplyController smartReplyController, NotificationEntryManager notificationEntryManager, Lazy<StatusBar> lazy, StatusBarStateController statusBarStateController, Handler handler, RemoteInputUriController remoteInputUriController, NotificationClickNotifier notificationClickNotifier, ActionClickLogger actionClickLogger) {
        return new NotificationRemoteInputManager(context, notificationLockscreenUserManager, smartReplyController, notificationEntryManager, lazy, statusBarStateController, handler, remoteInputUriController, notificationClickNotifier, actionClickLogger);
    }

    static default NotificationMediaManager provideNotificationMediaManager(Context context, Lazy<StatusBar> lazy, Lazy<NotificationShadeWindowController> lazy2, NotificationEntryManager notificationEntryManager, MediaArtworkProcessor mediaArtworkProcessor, KeyguardBypassController keyguardBypassController, DelayableExecutor delayableExecutor, DeviceConfigProxy deviceConfigProxy, MediaDataManager mediaDataManager) {
        return new NotificationMediaManager(context, lazy, lazy2, notificationEntryManager, mediaArtworkProcessor, keyguardBypassController, delayableExecutor, deviceConfigProxy, mediaDataManager);
    }

    static default NotificationListener provideNotificationListener(Context context, NotificationManager notificationManager, Handler handler) {
        return new NotificationListener(context, notificationManager, handler);
    }

    static default SmartReplyController provideSmartReplyController(NotificationEntryManager notificationEntryManager, IStatusBarService iStatusBarService, NotificationClickNotifier notificationClickNotifier) {
        return new SmartReplyController(notificationEntryManager, iStatusBarService, notificationClickNotifier);
    }

    static default NotificationViewHierarchyManager provideNotificationViewHierarchyManager(Context context, Handler handler, NotificationLockscreenUserManager notificationLockscreenUserManager, NotificationGroupManager notificationGroupManager, VisualStabilityManager visualStabilityManager, StatusBarStateController statusBarStateController, NotificationEntryManager notificationEntryManager, KeyguardBypassController keyguardBypassController, BubbleController bubbleController, DynamicPrivacyController dynamicPrivacyController, ForegroundServiceSectionController foregroundServiceSectionController, DynamicChildBindController dynamicChildBindController, LowPriorityInflationHelper lowPriorityInflationHelper) {
        return new NotificationViewHierarchyManager(context, handler, notificationLockscreenUserManager, notificationGroupManager, visualStabilityManager, statusBarStateController, notificationEntryManager, keyguardBypassController, bubbleController, dynamicPrivacyController, foregroundServiceSectionController, dynamicChildBindController, lowPriorityInflationHelper);
    }

    static default CommandQueue provideCommandQueue(Context context, ProtoTracer protoTracer) {
        return new CommandQueue(context, protoTracer);
    }
}
