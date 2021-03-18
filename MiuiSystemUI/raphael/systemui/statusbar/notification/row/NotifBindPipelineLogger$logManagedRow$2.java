package com.android.systemui.statusbar.notification.row;

import com.android.systemui.log.LogMessage;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.internal.Intrinsics;
import kotlin.jvm.internal.Lambda;
import org.jetbrains.annotations.NotNull;

/* access modifiers changed from: package-private */
/* compiled from: NotifBindPipelineLogger.kt */
public final class NotifBindPipelineLogger$logManagedRow$2 extends Lambda implements Function1<LogMessage, String> {
    public static final NotifBindPipelineLogger$logManagedRow$2 INSTANCE = new NotifBindPipelineLogger$logManagedRow$2();

    NotifBindPipelineLogger$logManagedRow$2() {
        super(1);
    }

    @NotNull
    public final String invoke(@NotNull LogMessage logMessage) {
        Intrinsics.checkParameterIsNotNull(logMessage, "$receiver");
        return "Row set for notif: " + logMessage.getStr1();
    }
}
