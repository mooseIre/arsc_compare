package com.android.systemui.miui.statusbar.notification;

import android.app.Notification;
import android.app.NotificationCompat;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.Icon;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.service.notification.StatusBarNotification;
import android.text.TextUtils;
import android.widget.ImageView;
import com.android.keyguard.KeyguardUpdateMonitor;
import com.android.keyguard.faceunlock.FaceUnlockManager;
import com.android.systemui.Constants;
import com.android.systemui.Dependency;
import com.android.systemui.Util;
import com.android.systemui.miui.AppIconsManager;
import com.android.systemui.miui.statusbar.ExpandedNotification;
import com.android.systemui.miui.statusbar.policy.UsbNotificationController;
import com.android.systemui.plugins.R;
import com.android.systemui.statusbar.NotificationData;
import com.android.systemui.statusbar.notification.MiuiNotificationCompat;
import com.xiaomi.stat.c.c;
import miui.R$style;
import miui.content.res.IconCustomizer;
import miui.securityspace.CrossUserUtils;
import miui.securityspace.XSpaceUserHandle;

public class NotificationUtil {
    private static int sNotificationStyle = (Constants.IS_INTERNATIONAL ? 1 : 0);

    public static int getUserFold(Context context) {
        return Settings.Global.getInt(context.getContentResolver(), "user_fold", 0);
    }

    public static int getUserAggregate(Context context) {
        return Settings.Global.getInt(context.getContentResolver(), "user_aggregate", 0);
    }

    public static boolean isHybrid(ExpandedNotification expandedNotification) {
        return expandedNotification != null && "com.miui.hybrid".equals(expandedNotification.getBasePkg());
    }

    public static boolean isCts(ExpandedNotification expandedNotification) {
        return expandedNotification != null && "com.android.cts.verifier".equals(expandedNotification.getBasePkg());
    }

    public static String getHybridAppName(ExpandedNotification expandedNotification) {
        if (expandedNotification.getNotification() == null || expandedNotification.getNotification().extras == null) {
            return null;
        }
        return expandedNotification.getNotification().extras.getString("miui.substName");
    }

    public static String getMessageId(ExpandedNotification expandedNotification) {
        if (expandedNotification.getNotification() == null || expandedNotification.getNotification().extras == null) {
            return null;
        }
        String string = expandedNotification.getNotification().extras.getString("message_id");
        if (!TextUtils.isEmpty(string)) {
            return string;
        }
        long j = expandedNotification.getNotification().extras.getLong("adid");
        return j != 0 ? String.valueOf(j) : string;
    }

    public static String getCategory(ExpandedNotification expandedNotification) {
        if (expandedNotification.getNotification() == null || expandedNotification.getNotification().extras == null) {
            return null;
        }
        return expandedNotification.getNotification().extras.getString("miui.category");
    }

    public static boolean hasSmallIcon(Notification notification) {
        return notification.getSmallIcon() != null;
    }

    private static boolean hasLargeIcon(Notification notification) {
        return (notification.largeIcon == null && notification.getLargeIcon() == null) ? false : true;
    }

    public static Drawable getLargeIconDrawable(Context context, Notification notification) {
        if (notification.getLargeIcon() != null) {
            return notification.getLargeIcon().loadDrawable(context);
        }
        if (notification.largeIcon != null) {
            return new BitmapDrawable(context.getResources(), notification.largeIcon);
        }
        return null;
    }

    public static void applyAppIconAllowCustom(Context context, ExpandedNotification expandedNotification, ImageView imageView) {
        if (imageView != null) {
            Drawable customAppIcon = getCustomAppIcon(context, expandedNotification);
            if (customAppIcon == null) {
                customAppIcon = getAppIcon(context, expandedNotification);
            }
            if (customAppIcon != null) {
                imageView.setImageDrawable(XSpaceUserHandle.getXSpaceIcon(context, customAppIcon, expandedNotification.getUser()));
            }
        }
    }

    public static void applyAppIcon(Context context, ExpandedNotification expandedNotification, ImageView imageView) {
        Drawable appIcon;
        if (imageView != null && (appIcon = getAppIcon(context, expandedNotification)) != null) {
            imageView.setImageDrawable(XSpaceUserHandle.getXSpaceIcon(context, appIcon, expandedNotification.getUser()));
        }
    }

    private static Drawable getAppIcon(Context context, ExpandedNotification expandedNotification) {
        Drawable appIcon = expandedNotification.getAppIcon();
        if (isHybrid(expandedNotification) && hasLargeIcon(expandedNotification.getNotification())) {
            appIcon = getLargeIconDrawable(context, expandedNotification.getNotification());
        }
        return appIcon == null ? expandedNotification.getNotification().getSmallIcon().loadDrawable(context) : appIcon;
    }

    private static Drawable getCustomAppIcon(Context context, ExpandedNotification expandedNotification) {
        Icon miuiAppIcon;
        if (((NotificationSettingsManager) Dependency.get(NotificationSettingsManager.class)).canCustomAppIcon(expandedNotification.getBasePkg()) && (miuiAppIcon = MiuiNotificationCompat.getMiuiAppIcon(expandedNotification.getNotification())) != null) {
            return ((AppIconsManager) Dependency.get(AppIconsManager.class)).getIconStyleDrawable(miuiAppIcon.loadDrawable(context), true);
        }
        return null;
    }

    public static Drawable getRowIcon(Context context, ExpandedNotification expandedNotification) {
        if (isPhoneNotification(expandedNotification)) {
            return IconCustomizer.getCustomizedIcon(context, "com.android.contacts.activities.TwelveKeyDialer.png");
        }
        if (((UsbNotificationController) Dependency.get(UsbNotificationController.class)).isUsbNotification(expandedNotification)) {
            Drawable drawable = context.getResources().getDrawable(R.drawable.notification_usb);
            if (drawable != null) {
                return ((AppIconsManager) Dependency.get(AppIconsManager.class)).getIconStyleDrawable(drawable, true);
            }
            return null;
        } else if (isImeNotification(expandedNotification)) {
            Drawable drawable2 = context.getResources().getDrawable(R.drawable.notification_ime);
            if (drawable2 != null) {
                return ((AppIconsManager) Dependency.get(AppIconsManager.class)).getIconStyleDrawable(drawable2, true);
            }
            return null;
        } else if (!expandedNotification.isSubstituteNotification()) {
            return null;
        } else {
            int identifier = expandedNotification.getUser().getIdentifier();
            if (identifier < 0) {
                identifier = CrossUserUtils.getCurrentUserId();
            }
            return ((AppIconsManager) Dependency.get(AppIconsManager.class)).getAppIcon(context, expandedNotification.getPackageName(), identifier);
        }
    }

    public static boolean isSystemNotification(StatusBarNotification statusBarNotification) {
        String packageName = statusBarNotification.getPackageName();
        return "android".equals(packageName) || "com.android.systemui".equals(packageName);
    }

    public static boolean isInCallUINotification(ExpandedNotification expandedNotification) {
        return "com.android.incallui".equals(expandedNotification.getPackageName());
    }

    private static boolean isPhoneNotification(ExpandedNotification expandedNotification) {
        String packageName = expandedNotification.getPackageName();
        return "com.android.incallui".equals(packageName) || "com.android.phone".equals(packageName) || "com.android.server.telecom".equals(packageName);
    }

    private static boolean isImeNotification(ExpandedNotification expandedNotification) {
        int id = expandedNotification.getId();
        return "android".equals(expandedNotification.getPackageName()) && (id == 17041080 || id == 8);
    }

    public static boolean isMissedCallNotification(ExpandedNotification expandedNotification) {
        if (expandedNotification == null) {
            return false;
        }
        String basePkg = expandedNotification.getBasePkg();
        if (("com.android.phone".equals(basePkg) || "com.android.server.telecom".equals(basePkg) || "com.miui.voip".equals(basePkg)) && "missed_call".equals(expandedNotification.getTag())) {
            return true;
        }
        return false;
    }

    public static CharSequence getMessageClassName(ExpandedNotification expandedNotification) {
        CharSequence messageClassName = expandedNotification.getMessageClassName();
        return messageClassName == null ? "" : messageClassName;
    }

    public static boolean needStatBadgeNum(NotificationData.Entry entry) {
        return entry != null && needStatBadgeNum(entry.notification);
    }

    public static boolean needStatBadgeNum(ExpandedNotification expandedNotification) {
        return expandedNotification != null && !"com.android.systemui".equals(expandedNotification.getPackageName()) && !hasProgressbar(expandedNotification) && expandedNotification.isClearable();
    }

    public static boolean needRestatBadgeNum(ExpandedNotification expandedNotification, ExpandedNotification expandedNotification2) {
        if ((expandedNotification.getMessageCount() != expandedNotification2.getMessageCount()) || needStatBadgeNum(expandedNotification) != needStatBadgeNum(expandedNotification2) || !TextUtils.equals(expandedNotification.getTargetPackageName(), expandedNotification2.getTargetPackageName())) {
            return true;
        }
        return false;
    }

    public static boolean hasProgressbar(ExpandedNotification expandedNotification) {
        if (expandedNotification == null) {
            return false;
        }
        Bundle bundle = expandedNotification.getNotification().extras;
        int i = bundle.getInt("android.progressMax", 0);
        boolean z = bundle.getBoolean("android.progressIndeterminate");
        if (i != 0 || z) {
            return true;
        }
        return false;
    }

    public static Icon getSmallIcon(Context context, ExpandedNotification expandedNotification) {
        if (shouldSubstituteSmallIcon(expandedNotification)) {
            int identifier = expandedNotification.getUser().getIdentifier();
            if (identifier < 0) {
                identifier = CrossUserUtils.getCurrentUserId();
            }
            Bitmap appIconBitmap = ((AppIconsManager) Dependency.get(AppIconsManager.class)).getAppIconBitmap(context, expandedNotification.getPackageName(), identifier);
            if (appIconBitmap != null) {
                return Icon.createWithBitmap(appIconBitmap);
            }
        }
        return expandedNotification.getNotification().getSmallIcon();
    }

    public static boolean isGrayscaleIcon(Notification notification) {
        return notification.extras.getBoolean("miui.isGrayscaleIcon", false);
    }

    public static boolean shouldSubstituteSmallIcon(ExpandedNotification expandedNotification) {
        if (showMiuiStyle()) {
            return !Util.isMiuiOptimizationDisabled();
        }
        if (!expandedNotification.isSubstituteNotification() || isGrayscaleIcon(expandedNotification.getNotification())) {
            return false;
        }
        return true;
    }

    public static boolean isMediaNotification(ExpandedNotification expandedNotification) {
        return expandedNotification != null && NotificationCompat.isMediaNotification(expandedNotification.getNotification());
    }

    public static boolean isCustomViewNotification(ExpandedNotification expandedNotification) {
        return (expandedNotification == null || (expandedNotification.getNotification().contentView == null && expandedNotification.getNotification().bigContentView == null)) ? false : true;
    }

    public static Context getPackageContext(Context context, StatusBarNotification statusBarNotification) {
        Context packageContext = statusBarNotification.getPackageContext(context);
        if (packageContext != context) {
            packageContext.setTheme(R$style.Theme_DayNight_RemoteViews);
        }
        return packageContext;
    }

    public static boolean isExpandingEnabled(boolean z) {
        return Constants.IS_INTERNATIONAL && !z;
    }

    public static boolean showMiuiStyle() {
        return sNotificationStyle == 0;
    }

    public static boolean showGoogleStyle() {
        return sNotificationStyle == 1;
    }

    public static boolean isNotificationStyleChanged(int i) {
        boolean z = sNotificationStyle != i;
        sNotificationStyle = i;
        return z;
    }

    public static String getHiddenText(Context context) {
        return context.getString(Build.VERSION.SDK_INT >= 27 ? R.string.notification_hidden_text : R.string.notification_hidden_by_policy_text);
    }

    public static CharSequence resolveTitle(Notification notification) {
        CharSequence charSequence = notification.extras.getCharSequence("android.title");
        if (charSequence == null) {
            charSequence = notification.extras.getCharSequence("android.title.big");
        }
        return charSequence != null ? charSequence : "";
    }

    public static CharSequence resolveText(Notification notification) {
        CharSequence charSequence = notification.extras.getCharSequence("android.text");
        if (charSequence == null) {
            charSequence = notification.extras.getCharSequence("android.bigText");
        }
        return charSequence != null ? charSequence : "";
    }

    public static CharSequence resolveSubText(Notification notification) {
        CharSequence charSequence = notification.extras.getCharSequence("android.subText");
        return charSequence != null ? charSequence : "";
    }

    public static boolean isInboxStyle(Notification notification) {
        return Notification.InboxStyle.class.equals(notification.getNotificationStyle());
    }

    public static boolean isMessagingStyle(Notification notification) {
        return Notification.MessagingStyle.class.equals(notification.getNotificationStyle());
    }

    public static boolean showSingleLine(Notification notification) {
        return !TextUtils.isEmpty(resolveTitle(notification)) && TextUtils.isEmpty(resolveText(notification));
    }

    public static boolean hideNotificationsForFaceUnlock(Context context) {
        return !KeyguardUpdateMonitor.getInstance(context).isFaceUnlock() && FaceUnlockManager.getInstance().isShowMessageWhenFaceUnlockSuccess();
    }

    public static boolean isColorizedNotification(ExpandedNotification expandedNotification) {
        return expandedNotification.getNotification().isColorizedMedia();
    }

    public static int getOutlineRadius(Context context) {
        return context.getResources().getDimensionPixelSize(R.dimen.notification_item_bg_radius);
    }

    public static int getCustomViewMargin(Context context) {
        return context.getResources().getDimensionPixelSize(R.dimen.notification_custom_view_margin);
    }

    public static String resoveSendPkg(ExpandedNotification expandedNotification) {
        return c.a.equals(expandedNotification.getOpPkg()) ? c.a : expandedNotification.getBasePkg();
    }
}
