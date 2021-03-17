package com.android.systemui.statusbar.notification.collection.coalescer;

import com.android.systemui.log.LogMessage;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.internal.Intrinsics;
import kotlin.jvm.internal.Lambda;
import org.jetbrains.annotations.NotNull;

/* access modifiers changed from: package-private */
/* compiled from: GroupCoalescerLogger.kt */
public final class GroupCoalescerLogger$logEmitBatch$2 extends Lambda implements Function1<LogMessage, String> {
    public static final GroupCoalescerLogger$logEmitBatch$2 INSTANCE = new GroupCoalescerLogger$logEmitBatch$2();

    GroupCoalescerLogger$logEmitBatch$2() {
        super(1);
    }

    @NotNull
    public final String invoke(@NotNull LogMessage logMessage) {
        Intrinsics.checkParameterIsNotNull(logMessage, "$receiver");
        return "Emitting event batch for group " + logMessage.getStr1();
    }
}
