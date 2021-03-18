package com.android.systemui.statusbar.notification;

import com.android.systemui.log.LogMessage;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.internal.Intrinsics;
import kotlin.jvm.internal.Lambda;
import org.jetbrains.annotations.NotNull;

/* access modifiers changed from: package-private */
/* compiled from: NotificationEntryManagerLogger.kt */
public final class NotificationEntryManagerLogger$logInflationAborted$2 extends Lambda implements Function1<LogMessage, String> {
    public static final NotificationEntryManagerLogger$logInflationAborted$2 INSTANCE = new NotificationEntryManagerLogger$logInflationAborted$2();

    NotificationEntryManagerLogger$logInflationAborted$2() {
        super(1);
    }

    @NotNull
    public final String invoke(@NotNull LogMessage logMessage) {
        Intrinsics.checkParameterIsNotNull(logMessage, "$receiver");
        return "NOTIF INFLATION ABORTED " + logMessage.getStr1() + " notifStatus=" + logMessage.getStr2() + " reason=" + logMessage.getStr3();
    }
}
