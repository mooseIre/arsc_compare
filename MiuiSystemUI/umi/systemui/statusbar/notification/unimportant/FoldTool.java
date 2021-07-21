package com.android.systemui.statusbar.notification.unimportant;

import android.app.Notification;
import android.content.Context;
import android.os.UserHandle;
import android.service.notification.StatusBarNotification;
import android.util.Log;
import androidx.constraintlayout.widget.R$styleable;
import com.android.systemui.Dependency;
import com.android.systemui.statusbar.NotificationLockscreenUserManager;
import com.android.systemui.statusbar.notification.NotificationSettingsHelper;
import com.android.systemui.statusbar.notification.NotificationUtil;
import com.miui.systemui.CloudDataManager;
import com.miui.systemui.SettingsManager;
import kotlin.jvm.internal.Intrinsics;
import kotlin.text.StringsKt__StringsKt;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/* compiled from: FoldTool.kt */
public final class FoldTool {
    public static final FoldTool INSTANCE = new FoldTool();
    private static boolean isInit;
    private static Context mContext;

    public final void setUnfoldLimit(int i) {
    }

    public final boolean shouldFold(int i) {
        return i == 302 || i == 306 || i == 310;
    }

    private FoldTool() {
    }

    public final void init(@NotNull Context context) {
        Intrinsics.checkParameterIsNotNull(context, "context");
        if (!isInit) {
            mContext = context;
            FoldCloudDataHelper.INSTANCE.init(context);
            registerWhiteListObserver();
            isInit = true;
        }
    }

    private final void registerWhiteListObserver() {
        ((CloudDataManager) Dependency.get(CloudDataManager.class)).registerListener(new FoldTool$registerWhiteListObserver$1());
    }

    public final int canFold(@NotNull StatusBarNotification statusBarNotification, boolean z, int i) {
        Intrinsics.checkParameterIsNotNull(statusBarNotification, "sbn");
        if (!isInit) {
            return 303;
        }
        if (skip(statusBarNotification) != 100) {
            return 312;
        }
        String packageName = Intrinsics.areEqual(statusBarNotification.getOpPkg(), "com.xiaomi.xmsf") ? "com.xiaomi.xmsf" : statusBarNotification.getPackageName();
        String targetPkg = NotificationUtil.getTargetPkg(statusBarNotification);
        if (!z) {
            i = NotificationSettingsHelper.getFoldImportance(targetPkg);
        }
        if (i == -1) {
            return 302;
        }
        if (i == 1) {
            return 301;
        }
        if (NotificationUtil.isSystemNotification(statusBarNotification)) {
            return 311;
        }
        if (Intrinsics.areEqual(packageName, "com.miui.systemAdSolution")) {
            return 308;
        }
        Notification notification = statusBarNotification.getNotification();
        Intrinsics.checkExpressionValueIsNotNull(notification, "sbn.notification");
        if (FoldToolKt.access$isXmsfNotificationChannel(targetPkg, notification.getChannelId())) {
            return 309;
        }
        if (Intrinsics.areEqual(packageName, "com.xiaomi.xmsf")) {
            Intrinsics.checkExpressionValueIsNotNull(targetPkg, "targetPkg");
            return fold(statusBarNotification, targetPkg);
        } else if (!FoldToolKt.access$isLocalWhitelist(targetPkg)) {
            return 310;
        } else {
            Intrinsics.checkExpressionValueIsNotNull(targetPkg, "targetPkg");
            return fold(statusBarNotification, targetPkg);
        }
    }

    private final int fold(StatusBarNotification statusBarNotification, String str) {
        return ((UnimportantSdk) Dependency.get(UnimportantSdk.class)).foldReason(statusBarNotification, str, ((FoldNotifController) Dependency.get(FoldNotifController.class)).getAllCount(str));
    }

    public final boolean canFoldByAnalyze(@NotNull StatusBarNotification statusBarNotification) {
        Intrinsics.checkParameterIsNotNull(statusBarNotification, "sbn");
        return isInit && skip(statusBarNotification) == 100;
    }

    private final int skip(StatusBarNotification statusBarNotification) {
        if (statusBarNotification == null) {
            return R$styleable.Constraint_layout_goneMarginLeft;
        }
        if (!isSameUser$default(this, statusBarNotification, 0, 2, null)) {
            return R$styleable.Constraint_transitionPathRotate;
        }
        if (!((SettingsManager) Dependency.get(SettingsManager.class)).getMiuiOptimizationEnabled()) {
            return R$styleable.Constraint_transitionEasing;
        }
        if ((statusBarNotification.getNotification().flags & 1122) != 0) {
            return R$styleable.Constraint_layout_goneMarginTop;
        }
        if ((statusBarNotification.getNotification().flags & 512) != 0) {
            CharSequence resolveTitle = NotificationUtil.resolveTitle(statusBarNotification.getNotification());
            Intrinsics.checkExpressionValueIsNotNull(resolveTitle, "resolveTitle(sbn.notification)");
            if (StringsKt__StringsKt.contains$default(resolveTitle, (CharSequence) "GroupSummary", false, 2, (Object) null)) {
                return R$styleable.Constraint_motionProgress;
            }
        }
        Boolean isFoldEntrance = NotificationUtil.isFoldEntrance(statusBarNotification);
        Intrinsics.checkExpressionValueIsNotNull(isFoldEntrance, "isFoldEntrance(sbn)");
        if (isFoldEntrance.booleanValue()) {
            return R$styleable.Constraint_layout_goneMarginStart;
        }
        if (!NotificationSettingsHelper.isFoldable(mContext, statusBarNotification.getPackageName())) {
            return R$styleable.Constraint_visibilityMode;
        }
        if (NotificationUtil.hasProgressbar(statusBarNotification)) {
            return R$styleable.Constraint_motionStagger;
        }
        if (NotificationUtil.isMediaNotification(statusBarNotification)) {
            return R$styleable.Constraint_pathMotionArc;
        }
        if (NotificationUtil.isCustomViewNotification(statusBarNotification.getNotification()) && FoldToolKt.access$isLocalWhitelist(statusBarNotification.getPackageName())) {
            return 108;
        }
        if (!NotificationSettingsHelper.isNotificationsBanned(mContext, NotificationUtil.getTargetPkg(statusBarNotification))) {
            return 100;
        }
        Log.e("UnimportantNotificationFoldTool", "no permission but has notification");
        return 109;
    }

    public static /* synthetic */ boolean isSameUser$default(FoldTool foldTool, StatusBarNotification statusBarNotification, int i, int i2, Object obj) {
        if ((i2 & 2) != 0) {
            i = -100;
        }
        return foldTool.isSameUser(statusBarNotification, i);
    }

    public final boolean isSameUser(@Nullable StatusBarNotification statusBarNotification, int i) {
        int i2;
        if (statusBarNotification == null) {
            return false;
        }
        if (i == -100) {
            Object obj = Dependency.get(NotificationLockscreenUserManager.class);
            Intrinsics.checkExpressionValueIsNotNull(obj, "Dependency.get(Notificat…nUserManager::class.java)");
            i2 = ((NotificationLockscreenUserManager) obj).getCurrentUserId();
        } else {
            i2 = i;
        }
        if (i == -100) {
            i = UserHandle.myUserId();
        }
        boolean z = statusBarNotification.getUserId() == -1 || statusBarNotification.getUserId() == i2;
        boolean z2 = i == 0 && statusBarNotification.getUserId() == 999;
        if (z || z2) {
            return true;
        }
        return false;
    }
}
