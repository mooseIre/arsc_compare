package com.android.systemui.statusbar.phone;

import com.android.systemui.log.LogMessage;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.internal.Intrinsics;
import kotlin.jvm.internal.Lambda;
import org.jetbrains.annotations.NotNull;

/* access modifiers changed from: package-private */
/* compiled from: StatusBarNotificationActivityStarterLogger.kt */
public final class StatusBarNotificationActivityStarterLogger$logHandleClickAfterKeyguardDismissed$2 extends Lambda implements Function1<LogMessage, String> {
    public static final StatusBarNotificationActivityStarterLogger$logHandleClickAfterKeyguardDismissed$2 INSTANCE = new StatusBarNotificationActivityStarterLogger$logHandleClickAfterKeyguardDismissed$2();

    StatusBarNotificationActivityStarterLogger$logHandleClickAfterKeyguardDismissed$2() {
        super(1);
    }

    @NotNull
    public final String invoke(@NotNull LogMessage logMessage) {
        Intrinsics.checkParameterIsNotNull(logMessage, "$receiver");
        return "(2/4) handleNotificationClickAfterKeyguardDismissed: " + logMessage.getStr1();
    }
}
