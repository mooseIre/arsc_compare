package com.android.systemui.statusbar.notification.collection;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.UserHandle;
import android.service.notification.StatusBarNotification;
import android.util.Log;
import com.android.systemui.statusbar.notification.collection.notifcollection.CommonNotifCollection;
import com.android.systemui.statusbar.phone.StatusBar;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;

/* compiled from: TargetSdkResolver.kt */
public final class TargetSdkResolver {
    private final String TAG = "TargetSdkResolver";
    private final Context context;

    public TargetSdkResolver(@NotNull Context context2) {
        Intrinsics.checkParameterIsNotNull(context2, "context");
        this.context = context2;
    }

    public final void initialize(@NotNull CommonNotifCollection commonNotifCollection) {
        Intrinsics.checkParameterIsNotNull(commonNotifCollection, "collection");
        commonNotifCollection.addCollectionListener(new TargetSdkResolver$initialize$1(this));
    }

    /* access modifiers changed from: private */
    public final int resolveNotificationSdk(StatusBarNotification statusBarNotification) {
        Context context2 = this.context;
        UserHandle user = statusBarNotification.getUser();
        Intrinsics.checkExpressionValueIsNotNull(user, "sbn.user");
        try {
            return StatusBar.getPackageManagerForUser(context2, user.getIdentifier()).getApplicationInfo(statusBarNotification.getPackageName(), 0).targetSdkVersion;
        } catch (PackageManager.NameNotFoundException e) {
            String str = this.TAG;
            Log.e(str, "Failed looking up ApplicationInfo for " + statusBarNotification.getPackageName(), e);
            return 0;
        }
    }
}
