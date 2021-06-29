package com.android.systemui.statusbar.phone;

import com.android.systemui.log.LogMessage;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.internal.Intrinsics;
import kotlin.jvm.internal.Lambda;
import org.jetbrains.annotations.NotNull;

/* access modifiers changed from: package-private */
/* compiled from: PanelViewLogger.kt */
public final class PanelViewLogger$logBlurAnimStart$2 extends Lambda implements Function1<LogMessage, String> {
    public static final PanelViewLogger$logBlurAnimStart$2 INSTANCE = new PanelViewLogger$logBlurAnimStart$2();

    PanelViewLogger$logBlurAnimStart$2() {
        super(1);
    }

    @NotNull
    public final String invoke(@NotNull LogMessage logMessage) {
        Intrinsics.checkParameterIsNotNull(logMessage, "$receiver");
        return "blur anim start: curBlur=" + logMessage.getStr1();
    }
}
