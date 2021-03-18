package com.android.systemui.statusbar.notification.collection.coordinator;

import com.android.systemui.log.LogMessage;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.internal.Intrinsics;
import kotlin.jvm.internal.Lambda;
import org.jetbrains.annotations.NotNull;

/* access modifiers changed from: package-private */
/* compiled from: PreparationCoordinatorLogger.kt */
public final class PreparationCoordinatorLogger$logInflationAborted$2 extends Lambda implements Function1<LogMessage, String> {
    public static final PreparationCoordinatorLogger$logInflationAborted$2 INSTANCE = new PreparationCoordinatorLogger$logInflationAborted$2();

    PreparationCoordinatorLogger$logInflationAborted$2() {
        super(1);
    }

    @NotNull
    public final String invoke(@NotNull LogMessage logMessage) {
        Intrinsics.checkParameterIsNotNull(logMessage, "$receiver");
        return "NOTIF INFLATION ABORTED " + logMessage.getStr1() + " reason=" + logMessage.getStr2();
    }
}
