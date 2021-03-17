package com.android.systemui.qs.logging;

import com.android.systemui.log.LogMessage;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.internal.Intrinsics;
import kotlin.jvm.internal.Lambda;
import org.jetbrains.annotations.NotNull;

/* access modifiers changed from: package-private */
/* compiled from: QSLogger.kt */
public final class QSLogger$logPanelExpanded$2 extends Lambda implements Function1<LogMessage, String> {
    public static final QSLogger$logPanelExpanded$2 INSTANCE = new QSLogger$logPanelExpanded$2();

    QSLogger$logPanelExpanded$2() {
        super(1);
    }

    @NotNull
    public final String invoke(@NotNull LogMessage logMessage) {
        Intrinsics.checkParameterIsNotNull(logMessage, "$receiver");
        return logMessage.getStr1() + " expanded=" + logMessage.getBool1();
    }
}
