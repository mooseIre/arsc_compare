package com.android.systemui.doze;

import com.android.systemui.log.LogMessage;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.internal.Intrinsics;
import kotlin.jvm.internal.Lambda;
import org.jetbrains.annotations.NotNull;

/* access modifiers changed from: package-private */
/* compiled from: DozeLogger.kt */
public final class DozeLogger$logDozing$2 extends Lambda implements Function1<LogMessage, String> {
    public static final DozeLogger$logDozing$2 INSTANCE = new DozeLogger$logDozing$2();

    DozeLogger$logDozing$2() {
        super(1);
    }

    @NotNull
    public final String invoke(@NotNull LogMessage logMessage) {
        Intrinsics.checkParameterIsNotNull(logMessage, "$receiver");
        return "Dozing=" + logMessage.getBool1();
    }
}
