package com.android.systemui;

import android.content.ContentResolver;
import android.content.Context;
import android.provider.DeviceConfig;
import android.provider.Settings;
import com.android.keyguard.fod.policy.MiuiGxzwPolicy;
import com.android.systemui.statusbar.notification.policy.NotificationAlertController;
import com.android.systemui.statusbar.notification.policy.NotificationCountLimitPolicy;
import com.android.systemui.statusbar.notification.policy.NotificationDynamicFpsController;
import com.android.systemui.statusbar.policy.MiuiHeadsUpPolicy;
import miui.util.Log;

public class MiuiVendorServices extends SystemUI {
    MiuiGxzwPolicy mMiuiGxzwPolicy;
    MiuiHeadsUpPolicy mMiuiHeadsUpPolicy;
    NotificationAlertController mNotifAlertController;
    NotificationCountLimitPolicy mNotifCountLimitPolicy;
    NotificationDynamicFpsController mNotifDynamicFpsController;
    MiuiWallpaperZoomOutService mWallpaperZoomOutService;

    public MiuiVendorServices(Context context) {
        super(context);
    }

    public void start() {
        setSettingsDefault();
        setDeviceConfigDefault();
        this.mWallpaperZoomOutService.start();
        this.mMiuiHeadsUpPolicy.start();
        this.mMiuiGxzwPolicy.start();
        this.mNotifAlertController.start();
        this.mNotifDynamicFpsController.start();
        this.mNotifCountLimitPolicy.start();
    }

    private void setSettingsDefault() {
        ContentResolver contentResolver = this.mContext.getContentResolver();
        Settings.Global.putInt(contentResolver, "sysui_powerui_enabled", 0);
        Settings.Secure.putIntForUser(contentResolver, "charging_sounds_enabled", 0, 0);
        Settings.Secure.putIntForUser(contentResolver, "charging_sounds_enabled", 0, 10);
        Settings.Global.putInt(contentResolver, "music_in_white_list", 0);
        Settings.Secure.putInt(contentResolver, "in_call_notification_enabled", this.mContext.getResources().getBoolean(C0007R$bool.play_incall_notification) ? 1 : 0);
    }

    private void setDeviceConfigDefault() {
        try {
            DeviceConfig.setProperty("systemui", "nav_bar_handle_show_over_lockscreen", Boolean.toString(false), true);
            DeviceConfig.setProperty("systemui", "assist_handles_suppress_on_lockscreen", Boolean.toString(true), true);
            DeviceConfig.setProperty("systemui", "assist_handles_suppress_on_launcher", Boolean.toString(true), true);
        } catch (Exception e) {
            Log.e("MiuiVendorServices", "setDefaultDeviceConfig ", e);
        }
    }
}
