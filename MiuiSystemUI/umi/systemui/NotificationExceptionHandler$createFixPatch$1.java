package com.android.systemui;

import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import com.android.systemui.plugins.NotificationListenerController;
import java.util.ArrayList;
import java.util.List;
import kotlin.TypeCastException;
import kotlin.collections.ArraysKt___ArraysKt;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/* compiled from: ExceptionHandler.kt */
public final class NotificationExceptionHandler$createFixPatch$1 implements NotificationListenerController {
    final /* synthetic */ boolean $filterAll;
    final /* synthetic */ String $pkgName;

    @Override // com.android.systemui.plugins.NotificationListenerController
    public void onListenerConnected(@Nullable NotificationListenerController.NotificationProvider notificationProvider) {
    }

    NotificationExceptionHandler$createFixPatch$1(boolean z, String str) {
        this.$filterAll = z;
        this.$pkgName = str;
    }

    @Override // com.android.systemui.plugins.NotificationListenerController
    public boolean onNotificationPosted(@NotNull StatusBarNotification statusBarNotification, @NotNull NotificationListenerService.RankingMap rankingMap) {
        Intrinsics.checkParameterIsNotNull(statusBarNotification, "sbn");
        Intrinsics.checkParameterIsNotNull(rankingMap, "rankingMap");
        return this.$filterAll || Intrinsics.areEqual(statusBarNotification.getPackageName(), this.$pkgName);
    }

    @Override // com.android.systemui.plugins.NotificationListenerController
    @NotNull
    public StatusBarNotification[] getActiveNotifications(@Nullable StatusBarNotification[] statusBarNotificationArr) {
        List list;
        if (this.$filterAll) {
            return new StatusBarNotification[0];
        }
        if (!(statusBarNotificationArr == null || (list = ArraysKt___ArraysKt.toList(statusBarNotificationArr)) == null)) {
            ArrayList arrayList = new ArrayList();
            for (Object obj : list) {
                if (!Intrinsics.areEqual(((StatusBarNotification) obj).getPackageName(), this.$pkgName)) {
                    arrayList.add(obj);
                }
            }
            Object[] array = arrayList.toArray(new StatusBarNotification[0]);
            if (array != null) {
                StatusBarNotification[] statusBarNotificationArr2 = (StatusBarNotification[]) array;
                if (statusBarNotificationArr2 != null) {
                    return statusBarNotificationArr2;
                }
            } else {
                throw new TypeCastException("null cannot be cast to non-null type kotlin.Array<T>");
            }
        }
        return new StatusBarNotification[0];
    }
}
