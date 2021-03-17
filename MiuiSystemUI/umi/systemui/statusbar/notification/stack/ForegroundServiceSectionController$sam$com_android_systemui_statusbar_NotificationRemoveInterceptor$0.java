package com.android.systemui.statusbar.notification.stack;

import com.android.systemui.statusbar.NotificationRemoveInterceptor;
import com.android.systemui.statusbar.notification.collection.NotificationEntry;
import kotlin.jvm.functions.Function3;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/* access modifiers changed from: package-private */
/* compiled from: ForegroundServiceSectionController.kt */
public final class ForegroundServiceSectionController$sam$com_android_systemui_statusbar_NotificationRemoveInterceptor$0 implements NotificationRemoveInterceptor {
    private final /* synthetic */ Function3 function;

    ForegroundServiceSectionController$sam$com_android_systemui_statusbar_NotificationRemoveInterceptor$0(Function3 function3) {
        this.function = function3;
    }

    @Override // com.android.systemui.statusbar.NotificationRemoveInterceptor
    public final /* synthetic */ boolean onNotificationRemoveRequested(@NotNull String str, @Nullable NotificationEntry notificationEntry, int i) {
        Intrinsics.checkParameterIsNotNull(str, "key");
        Object invoke = this.function.invoke(str, notificationEntry, Integer.valueOf(i));
        Intrinsics.checkExpressionValueIsNotNull(invoke, "invoke(...)");
        return ((Boolean) invoke).booleanValue();
    }
}
