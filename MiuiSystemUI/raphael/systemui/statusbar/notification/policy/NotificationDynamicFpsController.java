package com.android.systemui.statusbar.notification.policy;

import android.content.Context;
import android.os.SystemProperties;
import com.android.systemui.keyguard.ScreenLifecycle;
import com.android.systemui.plugins.statusbar.StatusBarStateController;
import com.android.systemui.statusbar.notification.NotificationEntryListener;
import com.android.systemui.statusbar.notification.NotificationEntryManager;
import com.android.systemui.statusbar.notification.collection.NotificationEntry;
import com.android.systemui.statusbar.phone.StatusBar;
import com.android.systemui.statusbar.policy.HeadsUpManager;
import com.android.systemui.statusbar.policy.OnHeadsUpChangedListener;
import miui.hardware.display.DisplayFeatureManager;

public class NotificationDynamicFpsController {
    private static final boolean SUPPORT_FPS_DYNAMIC = SystemProperties.getBoolean("ro.vendor.smart_dfps.enable", false);
    private NotificationEntryManager mEntryManager;
    private HeadsUpManager mHeadsUpManager;
    private ScreenLifecycle mScreenLifecycle;
    private StatusBar mStatusBar;
    private StatusBarStateController mStatusBarStateController;

    public NotificationDynamicFpsController(Context context, NotificationEntryManager notificationEntryManager, HeadsUpManager headsUpManager, StatusBar statusBar, StatusBarStateController statusBarStateController, ScreenLifecycle screenLifecycle) {
        this.mEntryManager = notificationEntryManager;
        this.mHeadsUpManager = headsUpManager;
        this.mStatusBar = statusBar;
        this.mStatusBarStateController = statusBarStateController;
        this.mScreenLifecycle = screenLifecycle;
    }

    public void start() {
        this.mEntryManager.addNotificationEntryListener(new NotificationEntryListener() {
            /* class com.android.systemui.statusbar.notification.policy.NotificationDynamicFpsController.AnonymousClass1 */

            @Override // com.android.systemui.statusbar.notification.NotificationEntryListener
            public void onNotificationAdded(NotificationEntry notificationEntry) {
                if (NotificationDynamicFpsController.this.mScreenLifecycle.getScreenState() == 2 && notificationEntry.getImportance() >= 3) {
                    if (NotificationDynamicFpsController.this.mStatusBar.isExpandedVisible() || NotificationDynamicFpsController.this.mStatusBarStateController.getState() == 1) {
                        NotificationDynamicFpsController.requestScreenFpsDynamic();
                    }
                }
            }
        });
        this.mHeadsUpManager.addListener(new OnHeadsUpChangedListener(this) {
            /* class com.android.systemui.statusbar.notification.policy.NotificationDynamicFpsController.AnonymousClass2 */

            @Override // com.android.systemui.statusbar.policy.OnHeadsUpChangedListener
            public void onHeadsUpPinnedModeChanged(boolean z) {
                NotificationDynamicFpsController.requestScreenFpsDynamic();
            }
        });
    }

    public static void requestScreenFpsDynamic() {
        if (SUPPORT_FPS_DYNAMIC) {
            DisplayFeatureManager.getInstance().setScreenEffect(24, 255, 256);
        }
    }
}
