package com.android.systemui.statusbar.notification;

import android.content.Context;
import com.android.systemui.util.DeviceConfigProxy;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;

/* compiled from: MiuiNotificationSectionsFeatureManager.kt */
public final class MiuiNotificationSectionsFeatureManager extends NotificationSectionsFeatureManager {
    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    public MiuiNotificationSectionsFeatureManager(@NotNull DeviceConfigProxy deviceConfigProxy, @NotNull Context context) {
        super(deviceConfigProxy, context);
        Intrinsics.checkParameterIsNotNull(deviceConfigProxy, "mProxy");
        Intrinsics.checkParameterIsNotNull(context, "mContext");
    }

    @NotNull
    public int[] getNotificationBuckets() {
        return ArraysKt___ArraysJvmKt.plus(super.getNotificationBuckets(), 7);
    }
}
