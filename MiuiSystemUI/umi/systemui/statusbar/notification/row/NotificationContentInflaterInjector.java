package com.android.systemui.statusbar.notification.row;

import android.app.Notification;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.Icon;
import android.os.Parcel;
import android.os.SystemClock;
import android.text.TextUtils;
import android.widget.RemoteViews;
import com.android.internal.util.ContrastColorUtil;
import com.android.systemui.C0011R$color;
import com.android.systemui.C0015R$id;
import com.android.systemui.C0017R$layout;
import com.android.systemui.C0021R$string;
import com.android.systemui.Dependency;
import com.android.systemui.controlcenter.phone.ControlPanelController;
import com.android.systemui.statusbar.notification.ExpandedNotification;
import com.android.systemui.statusbar.notification.InCallUtils;
import com.android.systemui.statusbar.notification.MiuiNotificationCompat;
import com.android.systemui.statusbar.notification.NotificationSettingsHelper;
import com.android.systemui.statusbar.notification.NotificationUtil;
import com.android.systemui.statusbar.notification.NotificationUtils;
import com.android.systemui.statusbar.notification.collection.NotificationEntry;
import com.android.systemui.statusbar.notification.row.NotificationContentInflater;
import com.android.systemui.statusbar.phone.StatusBar;
import com.miui.systemui.SettingsManager;
import com.miui.systemui.graphics.AppIconsManager;
import java.util.ArrayList;

public class NotificationContentInflaterInjector {
    public static void initAppInfo(NotificationEntry notificationEntry, Context context) {
        ExpandedNotification sbn = notificationEntry.getSbn();
        int identifier = sbn.getUser().getIdentifier();
        PackageManager packageManagerForUser = StatusBar.getPackageManagerForUser(context, identifier);
        try {
            ApplicationInfo applicationInfo = packageManagerForUser.getApplicationInfo(sbn.getPackageName(), 795136);
            if (applicationInfo != null) {
                sbn.setAppUid(identifier == 999 ? packageManagerForUser.getPackageUidAsUser(sbn.getPackageName(), identifier) : applicationInfo.uid);
                sbn.setTargetSdk(applicationInfo.targetSdkVersion);
                sbn.setAppName(String.valueOf(packageManagerForUser.getApplicationLabel(applicationInfo)));
                sbn.setAppIcon(((AppIconsManager) Dependency.get(AppIconsManager.class)).getAppIcon(applicationInfo, packageManagerForUser, identifier));
            }
        } catch (PackageManager.NameNotFoundException unused) {
            sbn.setAppIcon(packageManagerForUser.getDefaultActivityIcon());
        }
    }

    public static NotificationContentInflater.InflationProgress createRemoteViews(int i, Notification.Builder builder, boolean z, boolean z2, boolean z3, Context context, Context context2) {
        if (shouldInject(builder)) {
            return createMiuiRemoteViews(i, builder, z, z2, z3, context, context2);
        }
        return NotificationContentInflater.createRemoteViews(i, builder, z, z2, z3, context);
    }

    private static boolean shouldInject(Notification.Builder builder) {
        if (!NotificationSettingsHelper.showMiuiStyle()) {
            return false;
        }
        Notification.Style style = builder.getStyle();
        return style == null || (style instanceof Notification.BigPictureStyle) || (style instanceof Notification.BigTextStyle) || (style instanceof Notification.InboxStyle);
    }

    static NotificationContentInflater.InflationProgress createMiuiRemoteViews(int i, Notification.Builder builder, boolean z, boolean z2, boolean z3, Context context, Context context2) {
        NotificationContentInflater.InflationProgress inflationProgress = new NotificationContentInflater.InflationProgress();
        if ((i & 1) != 0) {
            inflationProgress.newContentView = createMiuiContentView(builder, z2, context2);
        }
        if ((i & 2) != 0) {
            inflationProgress.newExpandedView = createMiuiExpandedView(builder, context2);
        }
        if ((i & 4) != 0) {
            inflationProgress.newHeadsUpView = createMiuiHeadsUpView(builder, z3, context, context2);
        }
        if ((i & 8) != 0) {
            inflationProgress.newPublicView = createMiuiPublicView(builder, context2);
        }
        inflationProgress.packageContext = context2;
        inflationProgress.headsUpStatusBarText = builder.getHeadsUpStatusBarText(false);
        inflationProgress.headsUpStatusBarTextPublic = builder.getHeadsUpStatusBarText(true);
        return inflationProgress;
    }

    static RemoteViews createMiuiContentView(Notification.Builder builder, boolean z, Context context) {
        Notification buildUnstyled = builder.buildUnstyled();
        RemoteViews remoteViews = buildUnstyled.contentView;
        if (remoteViews != null) {
            return remoteViews;
        }
        if (showMiuiContentOneLine(buildUnstyled)) {
            return buildOneLineContent(buildUnstyled, false, context);
        }
        return buildBaseContent(buildUnstyled, context);
    }

    private static boolean showMiuiContentOneLine(Notification notification) {
        if (!TextUtils.isEmpty(notification.extras.getCharSequence("android.text"))) {
            return false;
        }
        int i = notification.extras.getInt("android.progressMax", 0);
        boolean z = notification.extras.getBoolean("android.progressIndeterminate");
        if (i != 0 || z) {
            return false;
        }
        Notification.Action[] actionArr = notification.actions;
        if (actionArr == null || actionArr.length <= 0) {
            return true;
        }
        return false;
    }

    static RemoteViews createMiuiExpandedView(Notification.Builder builder, Context context) {
        Notification buildUnstyled = builder.buildUnstyled();
        RemoteViews remoteViews = buildUnstyled.bigContentView;
        if (remoteViews != null) {
            return remoteViews;
        }
        if (builder.getStyle() != null) {
            Notification.Style style = builder.getStyle();
            if (style instanceof Notification.BigPictureStyle) {
                return buildBigPictureContent(buildUnstyled, ((Notification.BigPictureStyle) style).getBigPicture(), context);
            }
            if (style instanceof Notification.BigTextStyle) {
                return buildBigTextContent(buildUnstyled, false, context);
            }
            if (style instanceof Notification.InboxStyle) {
                return buildInboxContent(buildUnstyled, ((Notification.InboxStyle) style).getLines(), context);
            }
            return null;
        }
        Notification.Action[] actionArr = buildUnstyled.actions;
        if (actionArr == null || actionArr.length <= 0) {
            return null;
        }
        return buildBigBaseContent(buildUnstyled, true, false, context);
    }

    static RemoteViews createMiuiHeadsUpView(Notification.Builder builder, boolean z, Context context, Context context2) {
        Notification buildUnstyled = builder.buildUnstyled();
        Notification.Action[] actionArr = buildUnstyled.actions;
        boolean z2 = actionArr != null && actionArr.length > 0;
        RemoteViews remoteViews = buildUnstyled.headsUpContentView;
        if (remoteViews != null) {
            return remoteViews;
        }
        if (useOneLine(context2, context, buildUnstyled)) {
            return buildOneLineContent(buildUnstyled, true, context2);
        }
        if (builder.getStyle() != null) {
            if (!(builder.getStyle() instanceof Notification.BigTextStyle) || !z || !z2) {
                return buildBigBaseContent(buildUnstyled, z, true, context2);
            }
            return buildBigTextContent(buildUnstyled, true, context2);
        } else if (z2) {
            return buildBigBaseContent(buildUnstyled, z, true, context2);
        } else {
            return null;
        }
    }

    public static boolean isLandscape(Context context) {
        return context.getResources().getConfiguration().orientation == 2;
    }

    static RemoteViews createMiuiPublicView(Notification.Builder builder, Context context) {
        Notification buildUnstyled = builder.buildUnstyled();
        RemoteViews buildBaseContent = buildBaseContent(buildUnstyled, context);
        resetStandardTemplate(buildBaseContent);
        buildBaseContent.setTextViewText(C0015R$id.title, builder.loadHeaderAppName());
        buildBaseContent.setViewVisibility(C0015R$id.title, 0);
        buildBaseContent.setTextViewText(C0015R$id.text, context.getString(C0021R$string.notification_hidden_text));
        buildBaseContent.setViewVisibility(C0015R$id.text, 0);
        handleChronometerAndTime(buildBaseContent, buildUnstyled);
        return buildBaseContent;
    }

    private static RemoteViews buildBaseContent(Notification notification, Context context) {
        BuilderRemoteViews builderRemoteViews = new BuilderRemoteViews(context.getApplicationInfo(), C0017R$layout.miui_notification_template_material_base);
        resetStandardTemplate(builderRemoteViews);
        boolean handleMiuiAction = handleMiuiAction(builderRemoteViews, notification);
        if (!handleMiuiAction) {
            handleLargeIcon(builderRemoteViews, notification);
        }
        boolean handleProgressBar = handleProgressBar(builderRemoteViews, notification);
        handleTitle(builderRemoteViews, notification, handleProgressBar, context);
        handleText(builderRemoteViews, notification, handleProgressBar, context);
        if (!handleMiuiAction && !handleProgressBar) {
            handleChronometerAndTime(builderRemoteViews, notification);
        }
        return builderRemoteViews;
    }

    private static RemoteViews buildBigBaseContent(Notification notification, boolean z, boolean z2, Context context) {
        BuilderRemoteViews builderRemoteViews = new BuilderRemoteViews(context.getApplicationInfo(), C0017R$layout.miui_notification_template_material_big_base);
        resetStandardTemplateWithActions(builderRemoteViews);
        handleLargeIcon(builderRemoteViews, notification);
        boolean handleProgressBar = handleProgressBar(builderRemoteViews, notification);
        if (!handleProgressBar) {
            handleChronometerAndTime(builderRemoteViews, notification);
        }
        handleTitle(builderRemoteViews, notification, handleProgressBar, context);
        handleText(builderRemoteViews, notification, handleProgressBar, context);
        handleActions(builderRemoteViews, notification, context);
        return builderRemoteViews;
    }

    private static RemoteViews buildBigPictureContent(Notification notification, Bitmap bitmap, Context context) {
        BuilderRemoteViews builderRemoteViews = new BuilderRemoteViews(context.getApplicationInfo(), C0017R$layout.miui_notification_template_material_big_picture);
        resetStandardTemplateWithActions(builderRemoteViews);
        boolean handleProgressBar = handleProgressBar(builderRemoteViews, notification);
        if (!handleProgressBar) {
            handleChronometerAndTime(builderRemoteViews, notification);
        }
        handleBigContentTitle(builderRemoteViews, notification, handleProgressBar, context);
        handleText(builderRemoteViews, notification, handleProgressBar, context);
        if (notification.extras.containsKey("android.summaryText")) {
            builderRemoteViews.setTextViewText(C0015R$id.text, processTextSpans(notification.extras.getCharSequence("android.summaryText"), context));
            builderRemoteViews.setViewVisibility(C0015R$id.text, 0);
        }
        builderRemoteViews.setImageViewBitmap(C0015R$id.big_picture, bitmap);
        handleActions(builderRemoteViews, notification, context);
        return builderRemoteViews;
    }

    private static RemoteViews buildBigTextContent(Notification notification, boolean z, Context context) {
        BuilderRemoteViews builderRemoteViews = new BuilderRemoteViews(context.getApplicationInfo(), C0017R$layout.miui_notification_template_material_big_text);
        resetStandardTemplateWithActions(builderRemoteViews);
        handleLargeIcon(builderRemoteViews, notification);
        boolean handleProgressBar = handleProgressBar(builderRemoteViews, notification);
        if (!handleProgressBar) {
            handleChronometerAndTime(builderRemoteViews, notification);
        }
        handleBigContentTitle(builderRemoteViews, notification, handleProgressBar, context);
        handleText(builderRemoteViews, notification, handleProgressBar, context);
        handleBigText(builderRemoteViews, notification, context);
        handleActions(builderRemoteViews, notification, context);
        return builderRemoteViews;
    }

    private static RemoteViews buildInboxContent(Notification notification, ArrayList<CharSequence> arrayList, Context context) {
        BuilderRemoteViews builderRemoteViews = new BuilderRemoteViews(context.getApplicationInfo(), C0017R$layout.miui_notification_template_material_inbox);
        resetStandardTemplateWithActions(builderRemoteViews);
        boolean handleProgressBar = handleProgressBar(builderRemoteViews, notification);
        if (!handleProgressBar) {
            handleChronometerAndTime(builderRemoteViews, notification);
        }
        handleBigContentTitle(builderRemoteViews, notification, handleProgressBar, context);
        handleInboxText(builderRemoteViews, notification, arrayList, context);
        handleActions(builderRemoteViews, notification, context);
        return builderRemoteViews;
    }

    public static RemoteViews buildOneLineContent(Notification notification, boolean z, Context context) {
        int i;
        boolean isTransparentAble = isTransparentAble();
        ApplicationInfo applicationInfo = context.getApplicationInfo();
        if (isTransparentAble) {
            i = C0017R$layout.miui_notification_transparent_template_material_one_line;
        } else {
            i = C0017R$layout.miui_notification_template_material_one_line;
        }
        BuilderRemoteViews builderRemoteViews = new BuilderRemoteViews(applicationInfo, i);
        resetStandardTemplate(builderRemoteViews);
        handleMiuiAction(builderRemoteViews, notification);
        if (z) {
            builderRemoteViews.setTextColor(C0015R$id.miui_action, context.getColor(isTransparentAble ? C0011R$color.optimized_game_heads_up_notification_action_text : C0011R$color.optimized_heads_up_notification_action_text));
        }
        CharSequence charSequence = notification.extras.getCharSequence("android.title");
        if (charSequence != null) {
            builderRemoteViews.setViewVisibility(C0015R$id.title, 0);
            builderRemoteViews.setTextViewText(C0015R$id.title, processTextSpans(charSequence, context));
            if (z) {
                builderRemoteViews.setTextColor(C0015R$id.title, context.getColor(isTransparentAble ? C0011R$color.optimized_game_heads_up_notification_text : C0011R$color.optimized_heads_up_notification_text));
            }
        }
        CharSequence charSequence2 = notification.extras.getCharSequence("android.text");
        if (charSequence2 == null) {
            charSequence2 = notification.extras.getCharSequence("android.bigText");
        }
        if (charSequence2 != null) {
            builderRemoteViews.setTextViewText(C0015R$id.text, processTextSpans(charSequence2, context));
            builderRemoteViews.setViewVisibility(C0015R$id.text, 0);
            if (z) {
                builderRemoteViews.setTextColor(C0015R$id.text, context.getColor(isTransparentAble ? C0011R$color.optimized_game_heads_up_notification_text : C0011R$color.optimized_heads_up_notification_text));
            }
        }
        return builderRemoteViews;
    }

    private static void resetStandardTemplate(RemoteViews remoteViews) {
        remoteViews.setImageViewBitmap(C0015R$id.app_icon, null);
        remoteViews.setViewVisibility(C0015R$id.time_line_1, 8);
        remoteViews.setViewVisibility(C0015R$id.chronometer, 8);
        remoteViews.setViewVisibility(C0015R$id.time, 8);
        remoteViews.setViewVisibility(C0015R$id.right_icon, 8);
        remoteViews.setViewVisibility(C0015R$id.title, 8);
        remoteViews.setTextViewText(C0015R$id.title, null);
        remoteViews.setViewVisibility(C0015R$id.text, 8);
        remoteViews.setTextViewText(C0015R$id.text, null);
        remoteViews.setViewVisibility(C0015R$id.text_line_1, 8);
        remoteViews.setTextViewText(C0015R$id.text_line_1, null);
        remoteViews.setViewVisibility(C0015R$id.miui_action, 8);
        remoteViews.setTextViewText(C0015R$id.miui_action, null);
    }

    private static void resetStandardTemplateWithActions(RemoteViews remoteViews) {
        remoteViews.setImageViewBitmap(C0015R$id.app_icon, null);
        remoteViews.setViewVisibility(C0015R$id.time_line_1, 8);
        remoteViews.setViewVisibility(C0015R$id.chronometer, 8);
        remoteViews.setViewVisibility(C0015R$id.time, 8);
        remoteViews.setViewVisibility(C0015R$id.right_icon, 8);
        remoteViews.setViewVisibility(C0015R$id.title, 8);
        remoteViews.setTextViewText(C0015R$id.title, null);
        remoteViews.setViewVisibility(C0015R$id.text, 8);
        remoteViews.setTextViewText(C0015R$id.text, null);
        remoteViews.setViewVisibility(C0015R$id.text_line_1, 8);
        remoteViews.setTextViewText(C0015R$id.text_line_1, null);
        remoteViews.setViewVisibility(C0015R$id.actions, 8);
        remoteViews.removeAllViews(C0015R$id.actions);
    }

    private static boolean handleLargeIcon(RemoteViews remoteViews, Notification notification) {
        Bitmap bitmap;
        Icon largeIcon = notification.getLargeIcon();
        if (largeIcon == null && (bitmap = notification.largeIcon) != null) {
            largeIcon = Icon.createWithBitmap(bitmap);
        }
        boolean z = largeIcon != null;
        if (z) {
            remoteViews.setViewVisibility(C0015R$id.right_icon, 0);
            remoteViews.setImageViewIcon(C0015R$id.right_icon, largeIcon);
        }
        return z;
    }

    private static boolean handleProgressBar(RemoteViews remoteViews, Notification notification) {
        int i = notification.extras.getInt("android.progressMax", 0);
        int i2 = notification.extras.getInt("android.progress", 0);
        boolean z = notification.extras.getBoolean("android.progressIndeterminate");
        boolean z2 = i != 0 || z;
        if (z2) {
            remoteViews.setViewVisibility(C0015R$id.progress, 0);
            remoteViews.setProgressBar(C0015R$id.progress, i, i2, z);
            remoteViews.setProgressTintList(C0015R$id.progress, null);
            remoteViews.setProgressIndeterminateTintList(C0015R$id.progress, null);
        } else {
            remoteViews.setViewVisibility(C0015R$id.progress, 8);
        }
        return z2;
    }

    private static void handleTitle(RemoteViews remoteViews, Notification notification, boolean z, Context context) {
        handleTitle(remoteViews, notification.extras.getCharSequence("android.title"), z, context);
    }

    private static void handleBigContentTitle(RemoteViews remoteViews, Notification notification, boolean z, Context context) {
        CharSequence charSequence = notification.extras.getCharSequence("android.title");
        CharSequence charSequence2 = notification.extras.getCharSequence("android.title.big");
        if (charSequence2 != null) {
            charSequence = charSequence2;
        }
        handleTitle(remoteViews, charSequence, z, context);
        if (charSequence2 == null || !charSequence2.equals("")) {
            remoteViews.setViewVisibility(C0015R$id.line1, 0);
        } else {
            remoteViews.setViewVisibility(C0015R$id.line1, 8);
        }
    }

    private static void handleTitle(RemoteViews remoteViews, CharSequence charSequence, boolean z, Context context) {
        if (charSequence != null) {
            remoteViews.setViewVisibility(C0015R$id.title, 0);
            remoteViews.setTextViewText(C0015R$id.title, processTextSpans(charSequence, context));
            remoteViews.setViewLayoutWidth(C0015R$id.title, z ? -2 : -1);
        }
    }

    private static void handleText(RemoteViews remoteViews, Notification notification, boolean z, Context context) {
        CharSequence charSequence = notification.extras.getCharSequence("android.text");
        if (charSequence != null) {
            int i = z ? C0015R$id.text_line_1 : C0015R$id.text;
            remoteViews.setTextViewText(i, processTextSpans(charSequence, context));
            remoteViews.setViewVisibility(i, 0);
        }
    }

    private static void handleBigText(RemoteViews remoteViews, Notification notification, Context context) {
        CharSequence charSequence = notification.extras.getCharSequence("android.bigText");
        if (TextUtils.isEmpty(charSequence)) {
            charSequence = notification.extras.getCharSequence("android.text");
        }
        remoteViews.setTextViewText(C0015R$id.big_text, processTextSpans(charSequence, context));
        remoteViews.setViewVisibility(C0015R$id.big_text, 0);
    }

    private static void handleInboxText(RemoteViews remoteViews, Notification notification, ArrayList<CharSequence> arrayList, Context context) {
        int i;
        int i2 = 7;
        int[] iArr = {C0015R$id.inbox_text0, C0015R$id.inbox_text1, C0015R$id.inbox_text2, C0015R$id.inbox_text3, C0015R$id.inbox_text4, C0015R$id.inbox_text5, C0015R$id.inbox_text6};
        for (int i3 = 0; i3 < 7; i3++) {
            remoteViews.setViewVisibility(iArr[i3], 8);
        }
        Notification.Action[] actionArr = notification.actions;
        if (actionArr == null || actionArr.length <= 0) {
            i = 0;
        } else {
            i = 0;
            i2 = 6;
        }
        while (i < arrayList.size() && i < i2) {
            CharSequence charSequence = arrayList.get(i);
            if (!TextUtils.isEmpty(charSequence)) {
                remoteViews.setViewVisibility(iArr[i], 0);
                remoteViews.setTextViewText(iArr[i], processTextSpans(charSequence, context));
            }
            i++;
        }
    }

    private static boolean handleMiuiAction(RemoteViews remoteViews, Notification notification) {
        boolean isShowMiuiAction = MiuiNotificationCompat.isShowMiuiAction(notification);
        if (isShowMiuiAction) {
            remoteViews.setViewVisibility(C0015R$id.miui_action, 0);
            remoteViews.setTextViewText(C0015R$id.miui_action, MiuiNotificationCompat.getMiuiActionTitle(notification));
            remoteViews.setOnClickPendingIntent(C0015R$id.miui_action, notification.actions[0].actionIntent);
        }
        return isShowMiuiAction;
    }

    private static boolean handleChronometerAndTime(RemoteViews remoteViews, Notification notification) {
        boolean z = notification.showsTime() || notification.showsChronometer();
        if (z) {
            remoteViews.setViewVisibility(C0015R$id.time_line_1, 0);
            if (notification.extras.getBoolean("android.showChronometer")) {
                remoteViews.setViewVisibility(C0015R$id.chronometer, 0);
                remoteViews.setLong(C0015R$id.chronometer, "setBase", notification.when + (SystemClock.elapsedRealtime() - System.currentTimeMillis()));
                remoteViews.setBoolean(C0015R$id.chronometer, "setStarted", true);
                remoteViews.setChronometerCountDown(C0015R$id.chronometer, notification.extras.getBoolean("android.chronometerCountDown"));
            } else {
                remoteViews.setViewVisibility(C0015R$id.time, 0);
                remoteViews.setLong(C0015R$id.time, "setTime", notification.when);
            }
        } else {
            int i = C0015R$id.time;
            long j = notification.when;
            if (j == 0) {
                j = System.currentTimeMillis();
            }
            remoteViews.setLong(i, "setTime", j);
        }
        return z;
    }

    private static boolean handleActions(RemoteViews remoteViews, Notification notification, Context context) {
        Notification.Action[] actionArr = notification.actions;
        boolean z = actionArr != null && actionArr.length > 0;
        if (z) {
            remoteViews.setViewVisibility(C0015R$id.actions, 0);
            Notification.Action[] actionArr2 = notification.actions;
            for (Notification.Action action : actionArr2) {
                BuilderRemoteViews builderRemoteViews = new BuilderRemoteViews(context.getApplicationInfo(), C0017R$layout.notification_material_action);
                builderRemoteViews.setOnClickPendingIntent(C0015R$id.action0, action.actionIntent);
                builderRemoteViews.setContentDescription(C0015R$id.action0, action.title);
                if (action.getRemoteInputs() != null) {
                    builderRemoteViews.setRemoteInputs(C0015R$id.action0, action.getRemoteInputs());
                }
                builderRemoteViews.setTextViewText(C0015R$id.action0, processTextSpans(action.title, context));
                remoteViews.addView(C0015R$id.actions, builderRemoteViews);
            }
        }
        return z;
    }

    private static CharSequence processTextSpans(CharSequence charSequence, Context context) {
        return NotificationUtil.isNightMode(context) ? ContrastColorUtil.clearColorSpans(charSequence) : charSequence;
    }

    /* access modifiers changed from: private */
    public static class BuilderRemoteViews extends RemoteViews {
        /* access modifiers changed from: protected */
        public boolean shouldUseStaticFilter() {
            return true;
        }

        public BuilderRemoteViews(Parcel parcel) {
            super(parcel);
        }

        public BuilderRemoteViews(ApplicationInfo applicationInfo, int i) {
            super(applicationInfo, i);
        }

        @Override // android.widget.RemoteViews, android.widget.RemoteViews, java.lang.Object
        public BuilderRemoteViews clone() {
            Parcel obtain = Parcel.obtain();
            writeToParcel(obtain, 0);
            obtain.setDataPosition(0);
            BuilderRemoteViews builderRemoteViews = new BuilderRemoteViews(obtain);
            obtain.recycle();
            return builderRemoteViews;
        }
    }

    public static boolean useOneLine(Context context, Context context2, Notification notification) {
        return (((SettingsManager) Dependency.get(SettingsManager.class)).getGameModeEnabled() || isLandscape(context)) && !InCallUtils.isGlobalInCallNotification(context, context2.getPackageName(), notification) && !NotificationUtil.isCustomViewNotification(notification);
    }

    public static boolean isTransparentAble() {
        return ((SettingsManager) Dependency.get(SettingsManager.class)).getGameModeEnabled() || (((StatusBar) Dependency.get(StatusBar.class)).inFullscreenMode() && NotificationUtils.isScreenLandscape());
    }

    public static boolean isBlurAble(boolean z, boolean z2) {
        return ((ControlPanelController) Dependency.get(ControlPanelController.class)).isUseControlCenter() && !z && (!z2 || !isTransparentAble());
    }
}
