package com.android.systemui.statusbar;

import com.android.systemui.log.LogMessage;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.internal.Intrinsics;
import kotlin.jvm.internal.Lambda;
import org.jetbrains.annotations.NotNull;

/* access modifiers changed from: package-private */
/* compiled from: ActionClickLogger.kt */
public final class ActionClickLogger$logWaitingToCloseKeyguard$2 extends Lambda implements Function1<LogMessage, String> {
    public static final ActionClickLogger$logWaitingToCloseKeyguard$2 INSTANCE = new ActionClickLogger$logWaitingToCloseKeyguard$2();

    ActionClickLogger$logWaitingToCloseKeyguard$2() {
        super(1);
    }

    @NotNull
    public final String invoke(@NotNull LogMessage logMessage) {
        Intrinsics.checkParameterIsNotNull(logMessage, "$receiver");
        return "  [Action click] Intent " + logMessage.getStr1() + " launches an activity, dismissing keyguard first...";
    }
}
