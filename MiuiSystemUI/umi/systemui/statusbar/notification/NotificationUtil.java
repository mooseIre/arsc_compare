package com.android.systemui.statusbar.notification;

import android.app.Notification;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Outline;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.Icon;
import android.os.Bundle;
import android.os.UserHandle;
import android.service.notification.StatusBarNotification;
import android.text.TextUtils;
import android.util.Slog;
import android.view.View;
import android.view.ViewOutlineProvider;
import android.widget.ImageView;
import codeinjection.CodeInjection;
import com.android.systemui.C0021R$string;
import com.android.systemui.Dependency;
import com.android.systemui.SystemUIApplication;
import com.android.systemui.media.MediaDataManagerKt;
import com.android.systemui.statusbar.notification.row.ExpandableNotificationRow;
import com.android.systemui.statusbar.notification.row.ExpandableView;
import com.android.systemui.statusbar.notification.row.NotificationContentView;
import com.android.systemui.statusbar.notification.row.wrapper.MiuiNotificationOneLineViewWrapper;
import com.android.systemui.statusbar.notification.row.wrapper.NotificationViewWrapper;
import com.miui.systemui.BuildConfig;
import com.miui.systemui.DebugConfig;
import com.miui.systemui.SettingsManager;
import com.miui.systemui.graphics.AppIconsManager;
import miui.os.SystemProperties;
import miui.securityspace.CrossUserUtils;
import miui.securityspace.XSpaceUserHandle;
import miui.util.Log;

public class NotificationUtil {
    public static String getTargetPkg(StatusBarNotification statusBarNotification) {
        if (statusBarNotification == null) {
            return CodeInjection.MD5;
        }
        String packageName = statusBarNotification.getPackageName();
        if ("com.miui.hybrid".equals(packageName) && statusBarNotification.getNotification().extras != null) {
            String string = statusBarNotification.getNotification().extras.getString("miui.category");
            if (!TextUtils.isEmpty(string)) {
                return string;
            }
        }
        if (!((NotificationSettingsManager) Dependency.get(NotificationSettingsManager.class)).canSendSubstituteNotification(packageName)) {
            return packageName;
        }
        CharSequence targetPkg = MiuiNotificationCompat.getTargetPkg(statusBarNotification.getNotification());
        return !TextUtils.isEmpty(targetPkg) ? targetPkg.toString() : packageName;
    }

    public static boolean isInCallNotification(ExpandedNotification expandedNotification) {
        return InCallUtils.isInCallNotification(expandedNotification);
    }

    public static boolean isGlobalInCallNotification(Context context, ExpandedNotification expandedNotification) {
        return InCallUtils.isGlobalInCallNotification(context, expandedNotification.getPackageName(), expandedNotification.getNotification());
    }

    public static boolean containsVerifyCode(ExpandedNotification expandedNotification) {
        return expandedNotification.getNotification().extras.containsKey("verify_code");
    }

    public static boolean needCustomHeight(ExpandedNotification expandedNotification, boolean z) {
        if (expandedNotification == null) {
            return false;
        }
        return (z && expandedNotification.isCustomHeight()) || isMediaNotification(expandedNotification);
    }

    public static boolean isXmsfChannel(String str, String str2) {
        if (!TextUtils.isEmpty(str2)) {
            if (str2.startsWith(String.format("mipush|%s|pre", str))) {
                return true;
            }
        }
        return false;
    }

    public static boolean isHybrid(ExpandedNotification expandedNotification) {
        return expandedNotification != null && "com.miui.hybrid".equals(expandedNotification.getOpPkg());
    }

    public static String getHybridAppName(ExpandedNotification expandedNotification) {
        return expandedNotification.getNotification().extras.getString("miui.substName");
    }

    public static String getCategory(ExpandedNotification expandedNotification) {
        return expandedNotification.getNotification().extras.getString("miui.category");
    }

    public static boolean isFold(StatusBarNotification statusBarNotification) {
        return statusBarNotification.getNotification().extras.getBoolean("miui.unimportant", false);
    }

    public static void setFold(StatusBarNotification statusBarNotification, boolean z) {
        statusBarNotification.getNotification().extras.putBoolean("miui.unimportant", z);
    }

    public static Boolean isFoldEntrance(StatusBarNotification statusBarNotification) {
        return Boolean.valueOf(statusBarNotification.getNotification().extras.getBoolean("miui_unimportant", false));
    }

    public static int getFoldReason(StatusBarNotification statusBarNotification) {
        return statusBarNotification.getNotification().extras.getInt("fold_reason", 0);
    }

    public static void setFoldReason(StatusBarNotification statusBarNotification, int i) {
        statusBarNotification.getNotification().extras.putInt("fold_reason", i);
    }

    public static int getClickType(StatusBarNotification statusBarNotification) {
        return statusBarNotification.getNotification().extras.getInt("miui_unimportant_click_type", 0);
    }

    public static void setClickType(StatusBarNotification statusBarNotification, int i) {
        statusBarNotification.getNotification().extras.putInt("miui_unimportant_click_type", i);
    }

    private static boolean hasLargeIcon(Notification notification) {
        return (notification.largeIcon == null && notification.getLargeIcon() == null) ? false : true;
    }

    public static boolean containsFullScreenIntent(Notification notification) {
        return notification.fullScreenIntent != null;
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

    private static Drawable getAppIcon(Context context, ExpandedNotification expandedNotification) {
        Drawable appIcon = expandedNotification.getAppIcon();
        if (isHybrid(expandedNotification) && hasLargeIcon(expandedNotification.getNotification())) {
            appIcon = getLargeIconDrawable(context, expandedNotification.getNotification());
        }
        return appIcon == null ? expandedNotification.getNotification().getSmallIcon().loadDrawable(context) : appIcon;
    }

    private static Drawable getCustomAppIcon(Context context, ExpandedNotification expandedNotification) {
        Icon miuiAppIcon;
        if (((NotificationSettingsManager) Dependency.get(NotificationSettingsManager.class)).canCustomAppIcon(expandedNotification.getOpPkg()) && (miuiAppIcon = MiuiNotificationCompat.getMiuiAppIcon(expandedNotification.getNotification())) != null) {
            return ((AppIconsManager) Dependency.get(AppIconsManager.class)).getIconStyleDrawable(miuiAppIcon.loadDrawable(context), true);
        }
        return null;
    }

    public static boolean isSystemNotification(StatusBarNotification statusBarNotification) {
        String packageName = statusBarNotification.getPackageName();
        return "android".equals(packageName) || "com.android.systemui".equals(packageName);
    }

    public static boolean isMissedCallNotification(ExpandedNotification expandedNotification) {
        if (expandedNotification == null) {
            return false;
        }
        String opPkg = expandedNotification.getOpPkg();
        if (("com.android.phone".equals(opPkg) || "com.android.server.telecom".equals(opPkg)) && "missed_call".equals(expandedNotification.getTag())) {
            return true;
        }
        return false;
    }

    public static boolean hasProgressbar(StatusBarNotification statusBarNotification) {
        Bundle bundle = statusBarNotification.getNotification().extras;
        int i = bundle.getInt("android.progressMax", 0);
        boolean z = bundle.getBoolean("android.progressIndeterminate");
        if (i != 0 || z) {
            return true;
        }
        return false;
    }

    public static Icon getSmallIcon(ExpandedNotification expandedNotification) {
        if (shouldSubstituteSmallIcon(expandedNotification)) {
            Context context = SystemUIApplication.getContext();
            Icon hybridSmallIcon = getHybridSmallIcon(context, expandedNotification);
            if (hybridSmallIcon != null) {
                return hybridSmallIcon;
            }
            Icon customSmallIcon = getCustomSmallIcon(context, expandedNotification);
            if (customSmallIcon != null) {
                return customSmallIcon;
            }
            int identifier = expandedNotification.getUser().getIdentifier();
            if (identifier < 0) {
                identifier = CrossUserUtils.getCurrentUserId();
            }
            Bitmap appIconBitmap = ((AppIconsManager) Dependency.get(AppIconsManager.class)).getAppIconBitmap(expandedNotification.getPackageName(), identifier);
            if (appIconBitmap != null) {
                return Icon.createWithBitmap(appIconBitmap);
            }
        }
        return expandedNotification.getNotification().getSmallIcon();
    }

    private static Icon getHybridSmallIcon(Context context, ExpandedNotification expandedNotification) {
        if (!isHybrid(expandedNotification) || !hasLargeIcon(expandedNotification.getNotification())) {
            return null;
        }
        Drawable largeIconDrawable = getLargeIconDrawable(context, expandedNotification.getNotification());
        if (largeIconDrawable instanceof BitmapDrawable) {
            return Icon.createWithBitmap(((BitmapDrawable) largeIconDrawable).getBitmap());
        }
        return null;
    }

    private static Icon getCustomSmallIcon(Context context, ExpandedNotification expandedNotification) {
        Drawable customAppIcon = getCustomAppIcon(context, expandedNotification);
        if (customAppIcon instanceof BitmapDrawable) {
            return Icon.createWithBitmap(((BitmapDrawable) customAppIcon).getBitmap());
        }
        return null;
    }

    public static boolean shouldSubstituteSmallIcon(ExpandedNotification expandedNotification) {
        if (!BuildConfig.IS_INTERNATIONAL || NotificationSettingsHelper.showMiuiStyle()) {
            return ((SettingsManager) Dependency.get(SettingsManager.class)).getMiuiOptimizationEnabled();
        }
        return expandedNotification.isSubstituteNotification() && !MiuiNotificationCompat.isGrayscaleIcon(expandedNotification.getNotification());
    }

    public static boolean ignoreStatusBarIconColor(ExpandedNotification expandedNotification) {
        return expandedNotification != null && shouldSubstituteSmallIcon(expandedNotification);
    }

    @Deprecated
    public static boolean isMediaNotification(StatusBarNotification statusBarNotification) {
        return MediaDataManagerKt.isMediaNotification(statusBarNotification);
    }

    public static boolean isCustomViewNotification(ExpandedNotification expandedNotification) {
        if (expandedNotification == null) {
            return false;
        }
        return isCustomViewNotification(expandedNotification.getNotification());
    }

    public static boolean isCustomViewNotification(Notification notification) {
        return (notification == null || (notification.contentView == null && notification.bigContentView == null)) ? false : true;
    }

    public static String getHiddenText() {
        return ((NotificationSettingsManager) Dependency.get(NotificationSettingsManager.class)).getString(C0021R$string.notification_hidden_text);
    }

    public static CharSequence resolveTitle(Notification notification) {
        CharSequence charSequence = notification.extras.getCharSequence("android.title");
        if (charSequence == null) {
            charSequence = notification.extras.getCharSequence("android.title.big");
        }
        return charSequence != null ? charSequence : CodeInjection.MD5;
    }

    public static CharSequence resolveText(Notification notification) {
        CharSequence charSequence = notification.extras.getCharSequence("android.text");
        if (charSequence == null) {
            charSequence = notification.extras.getCharSequence("android.bigText");
        }
        return charSequence != null ? charSequence : CodeInjection.MD5;
    }

    public static CharSequence resolveSubText(Notification notification) {
        CharSequence charSequence = notification.extras.getCharSequence("android.subText");
        return charSequence != null ? charSequence : CodeInjection.MD5;
    }

    public static String resolvePushMsgId(Notification notification) {
        String string;
        return (notification == null || (string = notification.extras.getString("message_id")) == null) ? CodeInjection.MD5 : string;
    }

    public static boolean containsBigPic(Notification notification) {
        return (notification == null || notification.extras.getParcelable("android.picture") == null) ? false : true;
    }

    public static boolean containCustomView(Notification notification) {
        if (notification == null) {
            return false;
        }
        return notification.extras.getBoolean("android.contains.customView", false);
    }

    public static String getPushUid(Notification notification) {
        String string;
        return (notification == null || (string = notification.extras.getString("pushUid")) == null) ? CodeInjection.MD5 : string;
    }

    public static boolean isUserOwner(Context context) {
        return context.getUserId() == 0;
    }

    public static boolean isUidSystem(int i) {
        int appId = UserHandle.getAppId(i);
        return appId == 1000 || appId == 1001 || i == 0;
    }

    public static boolean isUidXmsf(Context context, int i) {
        Log.d("NotificationUtil", "isUidXmsf uid=" + i + ", xmsfuid=" + getPackageUid(context, "com.xiaomi.xmsf"));
        return DebugConfig.DEBUG || (i != 0 && i == getPackageUid(context, "com.xiaomi.xmsf"));
    }

    public static int getPackageUid(Context context, String str) {
        try {
            return context.getPackageManager().getPackageUid(str, 0);
        } catch (PackageManager.NameNotFoundException e) {
            Slog.e("NotificationUtil", "Error getPackageUid " + e);
            return 0;
        }
    }

    public static boolean isNightMode(Context context) {
        return (context.getResources().getConfiguration().uiMode & 48) == 32;
    }

    public static void setViewRoundCorner(View view, final float f) {
        view.setOutlineProvider(new ViewOutlineProvider() {
            /* class com.android.systemui.statusbar.notification.NotificationUtil.AnonymousClass1 */

            public void getOutline(View view, Outline outline) {
                outline.setRoundRect(new Rect(0, 0, view.getWidth(), view.getHeight()), f);
            }
        });
        view.setClipToOutline(true);
    }

    public static int getBucket() {
        return SystemProperties.getInt("persist.sys.notification_rank", 0);
    }

    public static boolean hasExpandingFeature() {
        return BuildConfig.IS_INTERNATIONAL;
    }

    public static boolean isTransparentBg(ExpandableView expandableView) {
        NotificationContentView showingLayout;
        if (!(expandableView instanceof ExpandableNotificationRow)) {
            return false;
        }
        ExpandableNotificationRow expandableNotificationRow = (ExpandableNotificationRow) expandableView;
        if ((!expandableNotificationRow.isHeadsUpState() && !expandableNotificationRow.isPinned()) || (showingLayout = expandableNotificationRow.getShowingLayout()) == null) {
            return false;
        }
        NotificationViewWrapper visibleWrapper = showingLayout.getVisibleWrapper(2);
        if (!(visibleWrapper instanceof MiuiNotificationOneLineViewWrapper) || !((MiuiNotificationOneLineViewWrapper) visibleWrapper).isTransparentBg()) {
            return false;
        }
        return true;
    }
}
