package com.android.systemui.statusbar.notification.unimportant;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.UserHandle;
import android.widget.RemoteViews;
import com.android.systemui.C0013R$drawable;
import com.android.systemui.C0015R$id;
import com.android.systemui.C0017R$layout;
import com.android.systemui.C0021R$string;
import com.android.systemui.Dependency;
import com.android.systemui.plugins.ActivityStarter;
import com.android.systemui.statusbar.NotificationLockscreenUserManager;
import com.android.systemui.statusbar.notification.ExpandedNotification;
import com.android.systemui.statusbar.notification.MiuiNotificationEntryManager;
import com.android.systemui.statusbar.notification.NotificationProvider;
import com.android.systemui.statusbar.notification.NotificationUtil;
import com.android.systemui.statusbar.policy.ConfigurationController;
import com.miui.systemui.NotificationSettings;
import com.miui.systemui.SettingsManager;
import com.miui.systemui.graphics.BitmapUtils;
import java.util.Map;
import kotlin.Lazy;
import kotlin.LazyKt__LazyJVMKt;
import kotlin.jvm.internal.Intrinsics;
import kotlin.jvm.internal.PropertyReference1Impl;
import kotlin.jvm.internal.Reflection;
import kotlin.reflect.KProperty;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/* compiled from: FoldNotifController.kt */
public final class FoldNotifController implements ConfigurationController.ConfigurationListener {
    static final /* synthetic */ KProperty[] $$delegatedProperties;
    private final Lazy activityStarter$delegate = LazyKt__LazyJVMKt.lazy(FoldNotifController$activityStarter$2.INSTANCE);
    @NotNull
    private final Lazy cache$delegate = LazyKt__LazyJVMKt.lazy(new FoldNotifController$cache$2(this));
    @NotNull
    private final Context context;
    private int count;
    @NotNull
    private final Lazy entryManager$delegate = LazyKt__LazyJVMKt.lazy(FoldNotifController$entryManager$2.INSTANCE);
    private final Lazy iconMargin$delegate = LazyKt__LazyJVMKt.lazy(new FoldNotifController$iconMargin$2(this));
    private final Lazy iconSize$delegate = LazyKt__LazyJVMKt.lazy(new FoldNotifController$iconSize$2(this));
    private final Lazy mNm$delegate = LazyKt__LazyJVMKt.lazy(new FoldNotifController$mNm$2(this));
    private boolean showNotifFoldFooterIcon = NotificationSettings.Companion.isNotifFoldFooterIconEnabled(this.context);

    static {
        PropertyReference1Impl propertyReference1Impl = new PropertyReference1Impl(Reflection.getOrCreateKotlinClass(FoldNotifController.class), "iconSize", "getIconSize()I");
        Reflection.property1(propertyReference1Impl);
        PropertyReference1Impl propertyReference1Impl2 = new PropertyReference1Impl(Reflection.getOrCreateKotlinClass(FoldNotifController.class), "iconMargin", "getIconMargin()I");
        Reflection.property1(propertyReference1Impl2);
        PropertyReference1Impl propertyReference1Impl3 = new PropertyReference1Impl(Reflection.getOrCreateKotlinClass(FoldNotifController.class), "mNm", "getMNm()Landroid/app/NotificationManager;");
        Reflection.property1(propertyReference1Impl3);
        PropertyReference1Impl propertyReference1Impl4 = new PropertyReference1Impl(Reflection.getOrCreateKotlinClass(FoldNotifController.class), "activityStarter", "getActivityStarter()Lcom/android/systemui/plugins/ActivityStarter;");
        Reflection.property1(propertyReference1Impl4);
        PropertyReference1Impl propertyReference1Impl5 = new PropertyReference1Impl(Reflection.getOrCreateKotlinClass(FoldNotifController.class), "cache", "getCache()Lcom/android/systemui/statusbar/notification/unimportant/PackageScoreCache;");
        Reflection.property1(propertyReference1Impl5);
        PropertyReference1Impl propertyReference1Impl6 = new PropertyReference1Impl(Reflection.getOrCreateKotlinClass(FoldNotifController.class), "entryManager", "getEntryManager()Lcom/android/systemui/statusbar/notification/MiuiNotificationEntryManager;");
        Reflection.property1(propertyReference1Impl6);
        $$delegatedProperties = new KProperty[]{propertyReference1Impl, propertyReference1Impl2, propertyReference1Impl3, propertyReference1Impl4, propertyReference1Impl5, propertyReference1Impl6};
    }

    private final ActivityStarter getActivityStarter() {
        Lazy lazy = this.activityStarter$delegate;
        KProperty kProperty = $$delegatedProperties[3];
        return (ActivityStarter) lazy.getValue();
    }

    private final int getIconMargin() {
        Lazy lazy = this.iconMargin$delegate;
        KProperty kProperty = $$delegatedProperties[1];
        return ((Number) lazy.getValue()).intValue();
    }

    private final int getIconSize() {
        Lazy lazy = this.iconSize$delegate;
        KProperty kProperty = $$delegatedProperties[0];
        return ((Number) lazy.getValue()).intValue();
    }

    private final NotificationManager getMNm() {
        Lazy lazy = this.mNm$delegate;
        KProperty kProperty = $$delegatedProperties[2];
        return (NotificationManager) lazy.getValue();
    }

    @NotNull
    public final PackageScoreCache getCache() {
        Lazy lazy = this.cache$delegate;
        KProperty kProperty = $$delegatedProperties[4];
        return (PackageScoreCache) lazy.getValue();
    }

    @NotNull
    public final MiuiNotificationEntryManager getEntryManager() {
        Lazy lazy = this.entryManager$delegate;
        KProperty kProperty = $$delegatedProperties[5];
        return (MiuiNotificationEntryManager) lazy.getValue();
    }

    public FoldNotifController(@NotNull Context context2) {
        Intrinsics.checkParameterIsNotNull(context2, "context");
        this.context = context2;
        ((SettingsManager) Dependency.get(SettingsManager.class)).registerNotifFoldListener(new NotificationSettings.FoldListener(this) {
            /* class com.android.systemui.statusbar.notification.unimportant.FoldNotifController.AnonymousClass1 */
            final /* synthetic */ FoldNotifController this$0;

            /* JADX WARN: Incorrect args count in method signature: ()V */
            {
                this.this$0 = r1;
            }

            @Override // com.miui.systemui.NotificationSettings.FoldListener
            public void onChanged(boolean z) {
                this.this$0.getEntryManager().changeFoldEnabled(z);
            }
        });
        ((NotificationLockscreenUserManager) Dependency.get(NotificationLockscreenUserManager.class)).addUserChangedListener(new NotificationLockscreenUserManager.UserChangedListener(this) {
            /* class com.android.systemui.statusbar.notification.unimportant.FoldNotifController.AnonymousClass2 */
            final /* synthetic */ FoldNotifController this$0;

            /* JADX WARN: Incorrect args count in method signature: ()V */
            {
                this.this$0 = r1;
            }

            @Override // com.android.systemui.statusbar.NotificationLockscreenUserManager.UserChangedListener
            public void onUserChanged(int i) {
                this.this$0.getEntryManager().onUserChanged(i, ((SettingsManager) Dependency.get(SettingsManager.class)).getNotifFold());
            }
        });
        this.context.getContentResolver().registerContentObserver(NotificationProvider.URI_FOLD_IMPORTANCE, false, new ContentObserver(this, (Handler) Dependency.get(Dependency.MAIN_HANDLER)) {
            /* class com.android.systemui.statusbar.notification.unimportant.FoldNotifController.AnonymousClass3 */
            final /* synthetic */ FoldNotifController this$0;

            {
                this.this$0 = r1;
            }

            public void onChange(boolean z, @Nullable Uri uri) {
                String queryParameter;
                Integer num = null;
                String queryParameter2 = uri != null ? uri.getQueryParameter("package") : null;
                if (!(uri == null || (queryParameter = uri.getQueryParameter("foldImportance")) == null)) {
                    num = Integer.valueOf(Integer.parseInt(queryParameter));
                }
                if (num != null && num.intValue() == 1) {
                    this.this$0.recoverPackage(queryParameter2);
                } else if (num != null && num.intValue() == -1) {
                    this.this$0.foldPackage(queryParameter2);
                } else {
                    this.this$0.getEntryManager().changeFold2SysCommend(queryParameter2);
                }
            }
        }, -1);
        ((ConfigurationController) Dependency.get(ConfigurationController.class)).addCallback(this);
    }

    @NotNull
    public final Context getContext() {
        return this.context;
    }

    public final void setShowNotifFoldFooterIcon(boolean z) {
        this.showNotifFoldFooterIcon = z;
        FoldManager.Companion.checkFoldNotification(MiuiNotificationEntryManager.shouldShow$default(getEntryManager(), 0, 1, null), getEntryManager().getCurrentUser());
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private final void recoverPackage(String str) {
        FoldManager.Companion.notify(1, str);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private final void foldPackage(String str) {
        FoldManager.Companion.notify(2, str);
    }

    @NotNull
    public final Map<String, Integer> getAllCount(@NotNull String str) {
        Intrinsics.checkParameterIsNotNull(str, "targetPkg");
        Map<String, Integer> allCount = getCache().getAllCount(str);
        Intrinsics.checkExpressionValueIsNotNull(allCount, "cache.getAllCount(targetPkg)");
        return allCount;
    }

    public final void addShowCount(@NotNull String str) {
        Intrinsics.checkParameterIsNotNull(str, "packageName");
        if (!FoldManager.Companion.shouldSuppressFold()) {
            getCache().addShow(str);
            int i = this.count + 1;
            this.count = i;
            if (i >= 5) {
                getCache().asyncUpdate();
                this.count = 0;
            }
        }
    }

    public final void addClickCount(@NotNull String str) {
        Intrinsics.checkParameterIsNotNull(str, "packageName");
        if (!FoldManager.Companion.shouldSuppressFold()) {
            getCache().addClick(str);
            int i = this.count + 1;
            this.count = i;
            if (i >= 5) {
                getCache().asyncUpdate();
                this.count = 0;
            }
        }
    }

    public final void sendFoldNotification(@NotNull UserHandle userHandle) {
        Intrinsics.checkParameterIsNotNull(userHandle, "user");
        PendingIntent broadcast = PendingIntent.getBroadcast(this.context, 0, new Intent(), 134217728);
        PendingIntent activity = PendingIntent.getActivity(this.context, 1001, new Intent(), 201326592);
        RemoteViews remoteViews = new RemoteViews(this.context.getPackageName(), C0017R$layout.unimportant_notification);
        remoteViews.setTextViewText(C0015R$id.aggregate_title, this.context.getResources().getString(C0021R$string.miui_unimportant_notifications));
        if (this.showNotifFoldFooterIcon) {
            remoteViews.setImageViewBitmap(C0015R$id.aggregate_title_icons, BitmapUtils.drawables2Bitmap(getEntryManager().getIconList(), getIconSize(), getIconMargin()));
            remoteViews.setViewVisibility(C0015R$id.aggregate_title_icons, 0);
            remoteViews.setViewVisibility(C0015R$id.aggregate_title_icon_more, 0);
        } else {
            remoteViews.setViewVisibility(C0015R$id.aggregate_title_icons, 4);
            remoteViews.setViewVisibility(C0015R$id.aggregate_title_icon_more, 4);
        }
        NotificationChannel notificationChannel = new NotificationChannel("id_aggregate", "unimportant_entrance", 3);
        notificationChannel.enableVibration(false);
        notificationChannel.setSound(null, null);
        getMNm().createNotificationChannel(notificationChannel);
        Bundle bundle = new Bundle();
        bundle.putBoolean("miui.showAtTail", true);
        bundle.putBoolean("miui.isPersistent", true);
        bundle.putBoolean("miui.customHeight", true);
        bundle.putBoolean("miui.noCustomViewDecoration", false);
        bundle.putBoolean("miui.showDivider", true);
        bundle.putBoolean("miui.enableKeyguard", false);
        bundle.putBoolean("miui_unimportant", true);
        bundle.putParcelable("miui.longPressIntent", activity);
        getMNm().notifyAsUser("UNIMPORTANT", 2012875145, new Notification.Builder(this.context, "id_aggregate").setSmallIcon(C0013R$drawable.icon).addExtras(bundle).setCustomContentView(remoteViews).setContentIntent(broadcast).build(), userHandle);
    }

    public final void cancelFoldNotification(@NotNull UserHandle userHandle) {
        Intrinsics.checkParameterIsNotNull(userHandle, "user");
        getMNm().cancelAsUser("UNIMPORTANT", 2012875145, userHandle);
    }

    public final boolean jump2Fold(@Nullable ExpandedNotification expandedNotification) {
        if (expandedNotification == null) {
            return false;
        }
        Boolean isFoldEntrance = NotificationUtil.isFoldEntrance(expandedNotification);
        Intrinsics.checkExpressionValueIsNotNull(isFoldEntrance, "isFold");
        if (!isFoldEntrance.booleanValue()) {
            return false;
        }
        FoldManager.Companion.notifyListeners(0);
        return true;
    }

    public final boolean jump2FoldSettings() {
        Intent foldSettingsIntent = getFoldSettingsIntent();
        foldSettingsIntent.addFlags(268435456);
        getActivityStarter().startActivity(foldSettingsIntent, true);
        return true;
    }

    @NotNull
    public final Intent getFoldSettingsIntent() {
        Intent intent = new Intent();
        intent.addFlags(67108864);
        intent.setComponent(new ComponentName("com.miui.notification", "miui.notification.management.activity.settings.AggregateSettingActivity"));
        intent.putExtra("fold_or_aggregate_settings", "fold");
        intent.addFlags(268435456);
        return intent;
    }

    @Override // com.android.systemui.statusbar.policy.ConfigurationController.ConfigurationListener
    public void onMiuiThemeChanged(boolean z) {
        super.onMiuiThemeChanged(z);
        getEntryManager().recheckFoldNotificationDelayed();
    }

    @Override // com.android.systemui.statusbar.policy.ConfigurationController.ConfigurationListener
    public void onLocaleListChanged() {
        super.onLocaleListChanged();
        getEntryManager().recheckFoldNotificationDelayed();
    }
}
