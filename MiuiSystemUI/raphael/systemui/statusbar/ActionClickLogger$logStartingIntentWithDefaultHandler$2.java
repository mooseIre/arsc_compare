package com.android.systemui.statusbar;

import com.android.systemui.log.LogMessage;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.internal.Intrinsics;
import kotlin.jvm.internal.Lambda;
import org.jetbrains.annotations.NotNull;

/* access modifiers changed from: package-private */
/* compiled from: ActionClickLogger.kt */
public final class ActionClickLogger$logStartingIntentWithDefaultHandler$2 extends Lambda implements Function1<LogMessage, String> {
    public static final ActionClickLogger$logStartingIntentWithDefaultHandler$2 INSTANCE = new ActionClickLogger$logStartingIntentWithDefaultHandler$2();

    ActionClickLogger$logStartingIntentWithDefaultHandler$2() {
        super(1);
    }

    @NotNull
    public final String invoke(@NotNull LogMessage logMessage) {
        Intrinsics.checkParameterIsNotNull(logMessage, "$receiver");
        return "  [Action click] Launching intent " + logMessage.getStr2() + " via default handler (for " + logMessage.getStr1() + ')';
    }
}
