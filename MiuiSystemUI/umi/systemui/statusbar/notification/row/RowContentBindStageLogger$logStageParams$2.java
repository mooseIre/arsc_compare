package com.android.systemui.statusbar.notification.row;

import com.android.systemui.log.LogMessage;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.internal.Intrinsics;
import kotlin.jvm.internal.Lambda;
import org.jetbrains.annotations.NotNull;

/* access modifiers changed from: package-private */
/* compiled from: RowContentBindStageLogger.kt */
public final class RowContentBindStageLogger$logStageParams$2 extends Lambda implements Function1<LogMessage, String> {
    public static final RowContentBindStageLogger$logStageParams$2 INSTANCE = new RowContentBindStageLogger$logStageParams$2();

    RowContentBindStageLogger$logStageParams$2() {
        super(1);
    }

    @NotNull
    public final String invoke(@NotNull LogMessage logMessage) {
        Intrinsics.checkParameterIsNotNull(logMessage, "$receiver");
        return "Invalidated notif " + logMessage.getStr1() + " with params: \n" + logMessage.getStr2();
    }
}
