package com.android.systemui.pip.phone;

import android.app.AppOpsManager;
import android.app.IActivityManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.drawable.Icon;
import android.net.Uri;
import android.util.Log;
import com.android.systemui.SystemUI;
import com.android.systemui.plugins.R;
import com.android.systemui.util.NotificationChannels;
import miui.view.MiuiHapticFeedbackConstants;

public class PipNotificationController {
    private static final String NOTIFICATION_TAG = "com.android.systemui.pip.phone.PipNotificationController";
    private static final String TAG = "PipNotificationController";
    private AppOpsManager.OnOpChangedListener mAppOpsChangedListener = new AppOpsManager.OnOpChangedListener() {
        public void onOpChanged(String str, String str2) {
            try {
                if (PipNotificationController.this.mAppOpsManager.checkOpNoThrow(67, PipNotificationController.this.mContext.getPackageManager().getApplicationInfo(str2, 0).uid, str2) != 0) {
                    PipNotificationController.this.mMotionHelper.dismissPip();
                }
            } catch (PackageManager.NameNotFoundException unused) {
                PipNotificationController.this.unregisterAppOpsListener();
            }
        }
    };
    /* access modifiers changed from: private */
    public AppOpsManager mAppOpsManager;
    /* access modifiers changed from: private */
    public Context mContext;
    private String mDeferredNotificationPackageName;
    /* access modifiers changed from: private */
    public PipMotionHelper mMotionHelper;
    private NotificationManager mNotificationManager;

    public PipNotificationController(Context context, IActivityManager iActivityManager, PipMotionHelper pipMotionHelper) {
        this.mContext = context;
        this.mAppOpsManager = (AppOpsManager) context.getSystemService("appops");
        this.mNotificationManager = NotificationManager.from(context);
        this.mMotionHelper = pipMotionHelper;
    }

    public void onActivityPinned(String str, boolean z) {
        this.mNotificationManager.cancel(NOTIFICATION_TAG, 0);
        if (z) {
            this.mDeferredNotificationPackageName = str;
        } else {
            showNotificationForApp(this.mDeferredNotificationPackageName);
        }
        registerAppOpsListener(str);
    }

    public void onPinnedStackAnimationEnded() {
        String str = this.mDeferredNotificationPackageName;
        if (str != null) {
            showNotificationForApp(str);
            this.mDeferredNotificationPackageName = null;
        }
    }

    public void onActivityUnpinned(ComponentName componentName) {
        unregisterAppOpsListener();
        this.mDeferredNotificationPackageName = null;
        if (componentName != null) {
            onActivityPinned(componentName.getPackageName(), false);
        } else {
            this.mNotificationManager.cancel(NOTIFICATION_TAG, 0);
        }
    }

    private void showNotificationForApp(String str) {
        Notification.Builder color = new Notification.Builder(this.mContext, NotificationChannels.GENERAL).setLocalOnly(true).setOngoing(true).setSmallIcon(R.drawable.pip_notification_icon).setColor(this.mContext.getColor(17170460));
        if (updateNotificationForApp(color, str)) {
            SystemUI.overrideNotificationAppName(this.mContext, color);
            this.mNotificationManager.notify(NOTIFICATION_TAG, 0, color.build());
        }
    }

    private boolean updateNotificationForApp(Notification.Builder builder, String str) {
        Icon icon;
        PackageManager packageManager = this.mContext.getPackageManager();
        try {
            ApplicationInfo applicationInfo = packageManager.getApplicationInfo(str, 0);
            if (applicationInfo == null) {
                return false;
            }
            String charSequence = packageManager.getApplicationLabel(applicationInfo).toString();
            String string = this.mContext.getString(R.string.pip_notification_message, new Object[]{charSequence});
            Intent intent = new Intent("android.settings.PICTURE_IN_PICTURE_SETTINGS", Uri.fromParts("package", str, (String) null));
            intent.setFlags(268468224);
            int i = applicationInfo.icon;
            if (i != 0) {
                icon = Icon.createWithResource(str, i);
            } else {
                icon = Icon.createWithResource(Resources.getSystem(), 17301651);
            }
            builder.setContentTitle(this.mContext.getString(R.string.pip_notification_title, new Object[]{charSequence})).setContentText(string).setContentIntent(PendingIntent.getActivity(this.mContext, str.hashCode(), intent, MiuiHapticFeedbackConstants.FLAG_MIUI_HAPTIC_TAP_NORMAL)).setStyle(new Notification.BigTextStyle().bigText(string)).setLargeIcon(icon);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, "Could not update notification for application", e);
            return false;
        }
    }

    private void registerAppOpsListener(String str) {
        this.mAppOpsManager.startWatchingMode(67, str, this.mAppOpsChangedListener);
    }

    /* access modifiers changed from: private */
    public void unregisterAppOpsListener() {
        this.mAppOpsManager.stopWatchingMode(this.mAppOpsChangedListener);
    }
}
