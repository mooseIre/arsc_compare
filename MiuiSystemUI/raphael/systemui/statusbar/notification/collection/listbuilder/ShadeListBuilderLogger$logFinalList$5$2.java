package com.android.systemui.statusbar.notification.collection.listbuilder;

import com.android.systemui.log.LogMessage;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.internal.Intrinsics;
import kotlin.jvm.internal.Lambda;
import org.jetbrains.annotations.NotNull;

/* access modifiers changed from: package-private */
/* compiled from: ShadeListBuilderLogger.kt */
public final class ShadeListBuilderLogger$logFinalList$5$2 extends Lambda implements Function1<LogMessage, String> {
    public static final ShadeListBuilderLogger$logFinalList$5$2 INSTANCE = new ShadeListBuilderLogger$logFinalList$5$2();

    ShadeListBuilderLogger$logFinalList$5$2() {
        super(1);
    }

    @NotNull
    public final String invoke(@NotNull LogMessage logMessage) {
        Intrinsics.checkParameterIsNotNull(logMessage, "$receiver");
        return "  [*] " + logMessage.getStr1() + " (summary)";
    }
}
