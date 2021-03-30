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

    @Override // com.android.systemui.statusbar.notification.NotificationSectionsFeatureManager
    @NotNull
    public int[] getNotificationBuckets() {
        if (isFilteringEnabled() && isMediaControlsEnabled()) {
            return new int[]{2, 3, 1, 5, 4, 6, 8};
        }
        if (!isFilteringEnabled() && isMediaControlsEnabled()) {
            return new int[]{2, 3, 1, 5, 6, 8};
        }
        if (isFilteringEnabled() && !isMediaControlsEnabled()) {
            return new int[]{2, 3, 5, 4, 6, 8};
        }
        if (NotificationUtils.useNewInterruptionModel(getContext())) {
            return new int[]{5, 6, 8};
        }
        return new int[]{5, 6, 8};
    }
}
