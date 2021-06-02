package com.android.systemui.screenshot;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.admin.DevicePolicyManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.UserHandle;
import android.util.DisplayMetrics;
import android.view.WindowManager;
import com.android.systemui.C0012R$dimen;
import com.android.systemui.C0013R$drawable;
import com.android.systemui.C0021R$string;
import com.android.systemui.SystemUI;
import com.android.systemui.util.NotificationChannels;

public class ScreenshotNotificationsController {
    private final Context mContext;
    private final NotificationManager mNotificationManager;
    private final Resources mResources;

    ScreenshotNotificationsController(Context context, WindowManager windowManager) {
        this.mContext = context;
        this.mResources = context.getResources();
        this.mNotificationManager = (NotificationManager) context.getSystemService("notification");
        this.mResources.getDimensionPixelSize(17104902);
        windowManager.getDefaultDisplay().getRealMetrics(new DisplayMetrics());
        try {
            this.mResources.getDimensionPixelSize(C0012R$dimen.notification_panel_width);
        } catch (Resources.NotFoundException unused) {
        }
        this.mResources.getDimensionPixelSize(C0012R$dimen.notification_max_height);
        new Notification.BigPictureStyle();
    }

    public void notifyScreenshotError(int i) {
        Resources resources = this.mContext.getResources();
        String string = resources.getString(i);
        Notification.Builder color = new Notification.Builder(this.mContext, NotificationChannels.ALERTS).setTicker(resources.getString(C0021R$string.screenshot_failed_title)).setContentTitle(resources.getString(C0021R$string.screenshot_failed_title)).setContentText(string).setSmallIcon(C0013R$drawable.stat_notify_image_error).setWhen(System.currentTimeMillis()).setVisibility(1).setCategory("err").setAutoCancel(true).setColor(this.mContext.getColor(17170460));
        Intent createAdminSupportIntent = ((DevicePolicyManager) this.mContext.getSystemService("device_policy")).createAdminSupportIntent("policy_disable_screen_capture");
        if (createAdminSupportIntent != null) {
            color.setContentIntent(PendingIntent.getActivityAsUser(this.mContext, 0, createAdminSupportIntent, 0, null, UserHandle.CURRENT));
        }
        SystemUI.overrideNotificationAppName(this.mContext, color, true);
        this.mNotificationManager.notify(1, new Notification.BigTextStyle(color).bigText(string).build());
    }

    static void cancelScreenshotNotification(Context context) {
        ((NotificationManager) context.getSystemService("notification")).cancel(1);
    }
}
