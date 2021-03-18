package com.android.systemui.statusbar.phone;

import com.android.systemui.log.LogMessage;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.internal.Intrinsics;
import kotlin.jvm.internal.Lambda;
import org.jetbrains.annotations.NotNull;

/* access modifiers changed from: package-private */
/* compiled from: StatusBarNotificationActivityStarterLogger.kt */
public final class StatusBarNotificationActivityStarterLogger$logExpandingBubble$2 extends Lambda implements Function1<LogMessage, String> {
    public static final StatusBarNotificationActivityStarterLogger$logExpandingBubble$2 INSTANCE = new StatusBarNotificationActivityStarterLogger$logExpandingBubble$2();

    StatusBarNotificationActivityStarterLogger$logExpandingBubble$2() {
        super(1);
    }

    @NotNull
    public final String invoke(@NotNull LogMessage logMessage) {
        Intrinsics.checkParameterIsNotNull(logMessage, "$receiver");
        return "Expanding bubble for " + logMessage.getStr1() + " (rather than firing intent)";
    }
}
