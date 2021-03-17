package com.android.systemui.doze;

import com.android.systemui.log.LogMessage;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.internal.Intrinsics;
import kotlin.jvm.internal.Lambda;
import org.jetbrains.annotations.NotNull;

/* access modifiers changed from: package-private */
/* compiled from: DozeLogger.kt */
public final class DozeLogger$logKeyguardVisibilityChange$2 extends Lambda implements Function1<LogMessage, String> {
    public static final DozeLogger$logKeyguardVisibilityChange$2 INSTANCE = new DozeLogger$logKeyguardVisibilityChange$2();

    DozeLogger$logKeyguardVisibilityChange$2() {
        super(1);
    }

    @NotNull
    public final String invoke(@NotNull LogMessage logMessage) {
        Intrinsics.checkParameterIsNotNull(logMessage, "$receiver");
        return "Keyguard visibility change, isShowing=" + logMessage.getBool1();
    }
}
