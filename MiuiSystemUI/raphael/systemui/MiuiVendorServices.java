package com.android.systemui;

import android.content.ContentResolver;
import android.content.Context;
import android.provider.Settings;
import android.service.notification.StatusBarNotification;
import com.android.keyguard.charge.MiuiChargeManager;
import com.android.keyguard.faceunlock.MiuiFaceUnlockManager;
import com.android.keyguard.fod.policy.MiuiGxzwPolicy;
import com.android.systemui.recents.MiuiFullScreenGestureProxy;
import com.android.systemui.recents.MiuiRecentProxy;
import com.android.systemui.statusbar.notification.NotificationEntryManager;
import com.android.systemui.statusbar.notification.NotificationPanelNavigationBarCoordinator;
import com.android.systemui.statusbar.notification.collection.NotificationEntry;
import com.android.systemui.statusbar.notification.collection.notifcollection.NotifCollectionListener;
import com.android.systemui.statusbar.notification.policy.NotificationAlertController;
import com.android.systemui.statusbar.notification.policy.NotificationCountLimitPolicy;
import com.android.systemui.statusbar.notification.policy.NotificationDynamicFpsController;
import com.android.systemui.statusbar.notification.policy.NotificationFilterController;
import com.android.systemui.statusbar.policy.MiuiHeadsUpPolicy;
import com.android.systemui.statusbar.policy.MiuiNotificationShadePolicy;
import com.android.systemui.statusbar.policy.MiuiStatusBarConfigurationListener;
import com.android.systemui.vendor.HeadsetPolicy;
import com.android.systemui.vendor.OrientationPolicy;
import com.miui.systemui.display.OLEDScreenHelper;

public class MiuiVendorServices extends SystemUI {
    HeadsetPolicy mHeadsetPolicy;
    MiuiChargeManager mMiuiChargeManager;
    MiuiFaceUnlockManager mMiuiFaceUnlockManager;
    MiuiFullScreenGestureProxy mMiuiFullScreenGestureProxy;
    MiuiGxzwPolicy mMiuiGxzwPolicy;
    MiuiHeadsUpPolicy mMiuiHeadsUpPolicy;
    MiuiNotificationShadePolicy mMiuiNotificationShadePolicy;
    MiuiRecentProxy mMiuiRecentProxy;
    MiuiStatusBarConfigurationListener mMiuiStatusBarConfigurationListener;
    NotificationAlertController mNotifAlertController;
    NotificationCountLimitPolicy mNotifCountLimitPolicy;
    NotificationDynamicFpsController mNotifDynamicFpsController;
    NotificationEntryManager mNotificationEntryManager;
    NotificationFilterController mNotificationFilterController;
    NotificationPanelNavigationBarCoordinator mNotificationNavigationCoordinator;
    OLEDScreenHelper mOledScreenHelper;
    OrientationPolicy mOrientationPolicy;
    PerformanceTools mPerformanceTools;
    MiuiWallpaperZoomOutService mWallpaperZoomOutService;

    public MiuiVendorServices(Context context) {
        super(context);
    }

    @Override // com.android.systemui.SystemUI
    public void start() {
        setSettingsDefault();
        this.mWallpaperZoomOutService.start();
        this.mMiuiHeadsUpPolicy.start();
        this.mMiuiGxzwPolicy.start();
        this.mNotificationFilterController.start();
        this.mNotifAlertController.start();
        this.mNotifDynamicFpsController.start();
        this.mNotifCountLimitPolicy.start();
        this.mMiuiNotificationShadePolicy.start();
        this.mMiuiRecentProxy.start();
        this.mOrientationPolicy.start();
        this.mPerformanceTools.start();
        this.mNotificationNavigationCoordinator.start();
        this.mHeadsetPolicy.start();
        this.mMiuiFullScreenGestureProxy.start();
        this.mOledScreenHelper.start();
        this.mMiuiChargeManager.start();
        this.mMiuiStatusBarConfigurationListener.start();
        this.mNotificationEntryManager.addCollectionListener(new NotifCollectionListener(this) {
            /* class com.android.systemui.MiuiVendorServices.AnonymousClass1 */

            @Override // com.android.systemui.statusbar.notification.collection.notifcollection.NotifCollectionListener
            public void onEntryBind(NotificationEntry notificationEntry, StatusBarNotification statusBarNotification) {
                statusBarNotification.getNotification().extras.putBoolean("android.colorized", false);
            }
        });
        this.mMiuiFaceUnlockManager.start();
    }

    private void setSettingsDefault() {
        ContentResolver contentResolver = this.mContext.getContentResolver();
        Settings.Global.putInt(contentResolver, "sysui_powerui_enabled", 0);
        Settings.Secure.putIntForUser(contentResolver, "charging_sounds_enabled", 0, 0);
        Settings.Secure.putIntForUser(contentResolver, "charging_sounds_enabled", 0, 10);
        Settings.Global.putInt(contentResolver, "music_in_white_list", 0);
        Settings.Secure.putInt(contentResolver, "in_call_notification_enabled", this.mContext.getResources().getBoolean(C0010R$bool.play_incall_notification) ? 1 : 0);
        Settings.Secure.putInt(contentResolver, "systemui_fsgesture_support_superpower", 0);
    }
}
