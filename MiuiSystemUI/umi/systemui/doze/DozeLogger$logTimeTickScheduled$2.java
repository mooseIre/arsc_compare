package com.android.systemui.doze;

import com.android.systemui.log.LogMessage;
import java.util.Date;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.internal.Intrinsics;
import kotlin.jvm.internal.Lambda;
import org.jetbrains.annotations.NotNull;

/* access modifiers changed from: package-private */
/* compiled from: DozeLogger.kt */
public final class DozeLogger$logTimeTickScheduled$2 extends Lambda implements Function1<LogMessage, String> {
    public static final DozeLogger$logTimeTickScheduled$2 INSTANCE = new DozeLogger$logTimeTickScheduled$2();

    DozeLogger$logTimeTickScheduled$2() {
        super(1);
    }

    @NotNull
    public final String invoke(@NotNull LogMessage logMessage) {
        Intrinsics.checkParameterIsNotNull(logMessage, "$receiver");
        return "Time tick scheduledAt=" + DozeLoggerKt.getDATE_FORMAT().format(new Date(logMessage.getLong1())) + ' ' + "triggerAt=" + DozeLoggerKt.getDATE_FORMAT().format(new Date(logMessage.getLong2()));
    }
}
