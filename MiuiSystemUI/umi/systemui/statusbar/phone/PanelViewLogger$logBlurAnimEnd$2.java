package com.android.systemui.statusbar.phone;

import com.android.systemui.log.LogMessage;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.internal.Intrinsics;
import kotlin.jvm.internal.Lambda;
import org.jetbrains.annotations.NotNull;

/* access modifiers changed from: package-private */
/* compiled from: PanelViewLogger.kt */
public final class PanelViewLogger$logBlurAnimEnd$2 extends Lambda implements Function1<LogMessage, String> {
    public static final PanelViewLogger$logBlurAnimEnd$2 INSTANCE = new PanelViewLogger$logBlurAnimEnd$2();

    PanelViewLogger$logBlurAnimEnd$2() {
        super(1);
    }

    @NotNull
    public final String invoke(@NotNull LogMessage logMessage) {
        Intrinsics.checkParameterIsNotNull(logMessage, "$receiver");
        return "blur anim end: curBlur=" + logMessage.getStr1() + " cancel=" + logMessage.getBool1() + ' ';
    }
}
