package com.android.systemui.statusbar.notification.collection.listbuilder;

import com.android.systemui.log.LogMessage;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.internal.Intrinsics;
import kotlin.jvm.internal.Lambda;
import org.jetbrains.annotations.NotNull;

/* access modifiers changed from: package-private */
/* compiled from: ShadeListBuilderLogger.kt */
public final class ShadeListBuilderLogger$logFinalList$4 extends Lambda implements Function1<LogMessage, String> {
    public static final ShadeListBuilderLogger$logFinalList$4 INSTANCE = new ShadeListBuilderLogger$logFinalList$4();

    ShadeListBuilderLogger$logFinalList$4() {
        super(1);
    }

    @NotNull
    public final String invoke(@NotNull LogMessage logMessage) {
        Intrinsics.checkParameterIsNotNull(logMessage, "$receiver");
        return '[' + logMessage.getInt1() + "] " + logMessage.getStr1();
    }
}
