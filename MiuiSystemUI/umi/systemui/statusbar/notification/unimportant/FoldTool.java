package com.android.systemui.statusbar.notification.unimportant;

import android.app.Notification;
import android.content.Context;
import android.service.notification.StatusBarNotification;
import android.util.Log;
import androidx.constraintlayout.widget.R$styleable;
import com.android.systemui.Dependency;
import com.android.systemui.statusbar.notification.MiuiNotificationEntryManager;
import com.android.systemui.statusbar.notification.NotificationSettingsHelper;
import com.android.systemui.statusbar.notification.NotificationUtil;
import com.miui.systemui.CloudDataManager;
import com.miui.systemui.SettingsManager;
import kotlin.Lazy;
import kotlin.LazyKt__LazyJVMKt;
import kotlin.jvm.internal.Intrinsics;
import kotlin.jvm.internal.PropertyReference1Impl;
import kotlin.jvm.internal.Reflection;
import kotlin.reflect.KProperty;
import org.jetbrains.annotations.NotNull;

/* compiled from: FoldTool.kt */
public final class FoldTool {
    static final /* synthetic */ KProperty[] $$delegatedProperties;
    public static final FoldTool INSTANCE = new FoldTool();
    @NotNull
    private static final Lazy entryManager$delegate = LazyKt__LazyJVMKt.lazy(FoldTool$entryManager$2.INSTANCE);
    private static boolean isInit;
    private static Context mContext;

    @NotNull
    public final MiuiNotificationEntryManager getEntryManager() {
        Lazy lazy = entryManager$delegate;
        KProperty kProperty = $$delegatedProperties[0];
        return (MiuiNotificationEntryManager) lazy.getValue();
    }

    public final void setUnfoldLimit(int i) {
    }

    public final boolean shouldFold(int i) {
        return i == 302 || i == 306 || i == 310;
    }

    static {
        PropertyReference1Impl propertyReference1Impl = new PropertyReference1Impl(Reflection.getOrCreateKotlinClass(FoldTool.class), "entryManager", "getEntryManager()Lcom/android/systemui/statusbar/notification/MiuiNotificationEntryManager;");
        Reflection.property1(propertyReference1Impl);
        $$delegatedProperties = new KProperty[]{propertyReference1Impl};
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
        if (!getEntryManager().isSameUser(statusBarNotification)) {
            return R$styleable.Constraint_transitionPathRotate;
        }
        if (!((SettingsManager) Dependency.get(SettingsManager.class)).getMiuiOptimizationEnabled()) {
            return R$styleable.Constraint_transitionEasing;
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
}
