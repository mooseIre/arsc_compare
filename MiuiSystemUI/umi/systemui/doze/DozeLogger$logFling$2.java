package com.android.systemui.doze;

import com.android.systemui.log.LogMessage;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.internal.Intrinsics;
import kotlin.jvm.internal.Lambda;
import org.jetbrains.annotations.NotNull;

/* access modifiers changed from: package-private */
/* compiled from: DozeLogger.kt */
public final class DozeLogger$logFling$2 extends Lambda implements Function1<LogMessage, String> {
    public static final DozeLogger$logFling$2 INSTANCE = new DozeLogger$logFling$2();

    DozeLogger$logFling$2() {
        super(1);
    }

    @NotNull
    public final String invoke(@NotNull LogMessage logMessage) {
        Intrinsics.checkParameterIsNotNull(logMessage, "$receiver");
        return "Fling expand=" + logMessage.getBool1() + " aboveThreshold=" + logMessage.getBool2() + " thresholdNeeded=" + logMessage.getBool3() + ' ' + "screenOnFromTouch=" + logMessage.getBool4();
    }
}
