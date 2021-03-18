package com.android.systemui.statusbar.phone;

import com.android.systemui.log.LogMessage;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.internal.Intrinsics;
import kotlin.jvm.internal.Lambda;
import org.jetbrains.annotations.NotNull;

/* access modifiers changed from: package-private */
/* compiled from: StatusBarNotificationActivityStarterLogger.kt */
public final class StatusBarNotificationActivityStarterLogger$logStartingActivityFromClick$2 extends Lambda implements Function1<LogMessage, String> {
    public static final StatusBarNotificationActivityStarterLogger$logStartingActivityFromClick$2 INSTANCE = new StatusBarNotificationActivityStarterLogger$logStartingActivityFromClick$2();

    StatusBarNotificationActivityStarterLogger$logStartingActivityFromClick$2() {
        super(1);
    }

    @NotNull
    public final String invoke(@NotNull LogMessage logMessage) {
        Intrinsics.checkParameterIsNotNull(logMessage, "$receiver");
        return "(1/4) onNotificationClicked: " + logMessage.getStr1();
    }
}
