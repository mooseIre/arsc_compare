package com.android.systemui;

import android.content.ContentResolver;
import android.content.Context;
import android.provider.Settings;
import com.android.keyguard.fod.policy.MiuiGxzwPolicy;
import com.android.systemui.recents.MiuiFullScreenGestureProxy;
import com.android.systemui.recents.MiuiRecentProxy;
import com.android.systemui.statusbar.notification.NotificationPanelNavigationBarCoordinator;
import com.android.systemui.statusbar.notification.policy.NotificationAlertController;
import com.android.systemui.statusbar.notification.policy.NotificationCountLimitPolicy;
import com.android.systemui.statusbar.notification.policy.NotificationDynamicFpsController;
import com.android.systemui.statusbar.policy.MiuiHeadsUpPolicy;
import com.android.systemui.statusbar.policy.MiuiNotificationShadePolicy;
import com.android.systemui.vendor.HeadsetPolicy;
import com.android.systemui.vendor.OrientationPolicy;

public class MiuiVendorServices extends SystemUI {
    HeadsetPolicy mHeadsetPolicy;
    MiuiFullScreenGestureProxy mMiuiFullScreenGestureProxy;
    MiuiGxzwPolicy mMiuiGxzwPolicy;
    MiuiHeadsUpPolicy mMiuiHeadsUpPolicy;
    MiuiNotificationShadePolicy mMiuiNotificationShadePolicy;
    MiuiRecentProxy mMiuiRecentProxy;
    NotificationAlertController mNotifAlertController;
    NotificationCountLimitPolicy mNotifCountLimitPolicy;
    NotificationDynamicFpsController mNotifDynamicFpsController;
    NotificationPanelNavigationBarCoordinator mNotificationNavigationCoordinator;
    OrientationPolicy mOrientationPolicy;
    MiuiWallpaperZoomOutService mWallpaperZoomOutService;

    public MiuiVendorServices(Context context) {
        super(context);
    }

    public void start() {
        setSettingsDefault();
        this.mWallpaperZoomOutService.start();
        this.mMiuiHeadsUpPolicy.start();
        this.mMiuiGxzwPolicy.start();
        this.mNotifAlertController.start();
        this.mNotifDynamicFpsController.start();
        this.mNotifCountLimitPolicy.start();
        this.mMiuiNotificationShadePolicy.start();
        this.mMiuiRecentProxy.start();
        this.mOrientationPolicy.start();
        this.mNotificationNavigationCoordinator.start();
        this.mHeadsetPolicy.start();
        this.mMiuiFullScreenGestureProxy.start();
    }

    private void setSettingsDefault() {
        ContentResolver contentResolver = this.mContext.getContentResolver();
        Settings.Global.putInt(contentResolver, "sysui_powerui_enabled", 0);
        Settings.Secure.putIntForUser(contentResolver, "charging_sounds_enabled", 0, 0);
        Settings.Secure.putIntForUser(contentResolver, "charging_sounds_enabled", 0, 10);
        Settings.Global.putInt(contentResolver, "music_in_white_list", 0);
        Settings.Secure.putInt(contentResolver, "in_call_notification_enabled", this.mContext.getResources().getBoolean(C0010R$bool.play_incall_notification) ? 1 : 0);
    }
}
