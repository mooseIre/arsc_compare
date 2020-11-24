package com.android.systemui.statusbar.notification;

import android.util.FloatProperty;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/* compiled from: NotificationWakeUpCoordinator.kt */
public final class NotificationWakeUpCoordinator$mNotificationVisibility$1 extends FloatProperty<NotificationWakeUpCoordinator> {
    NotificationWakeUpCoordinator$mNotificationVisibility$1(String str) {
        super(str);
    }

    public void setValue(@NotNull NotificationWakeUpCoordinator notificationWakeUpCoordinator, float f) {
        Intrinsics.checkParameterIsNotNull(notificationWakeUpCoordinator, "coordinator");
        notificationWakeUpCoordinator.setVisibilityAmount(f);
    }

    @Nullable
    public Float get(@NotNull NotificationWakeUpCoordinator notificationWakeUpCoordinator) {
        Intrinsics.checkParameterIsNotNull(notificationWakeUpCoordinator, "coordinator");
        return Float.valueOf(notificationWakeUpCoordinator.mLinearVisibilityAmount);
    }
}
