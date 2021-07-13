package com.android.systemui.statusbar.phone;

import com.android.systemui.log.LogMessage;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.internal.Intrinsics;
import kotlin.jvm.internal.Lambda;
import org.jetbrains.annotations.NotNull;

/* access modifiers changed from: package-private */
/* compiled from: PanelViewLogger.kt */
public final class PanelViewLogger$logBlurAnimUpdate$2 extends Lambda implements Function1<LogMessage, String> {
    public static final PanelViewLogger$logBlurAnimUpdate$2 INSTANCE = new PanelViewLogger$logBlurAnimUpdate$2();

    PanelViewLogger$logBlurAnimUpdate$2() {
        super(1);
    }

    @NotNull
    public final String invoke(@NotNull LogMessage logMessage) {
        Intrinsics.checkParameterIsNotNull(logMessage, "$receiver");
        return "blur anim update: curBlur=" + logMessage.getStr1() + " curAnimValue=" + logMessage.getDouble1();
    }
}
