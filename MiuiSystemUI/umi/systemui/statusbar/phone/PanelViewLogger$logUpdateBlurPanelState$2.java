package com.android.systemui.statusbar.phone;

import com.android.systemui.log.LogMessage;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.internal.Intrinsics;
import kotlin.jvm.internal.Lambda;
import org.jetbrains.annotations.NotNull;

/* access modifiers changed from: package-private */
/* compiled from: PanelViewLogger.kt */
public final class PanelViewLogger$logUpdateBlurPanelState$2 extends Lambda implements Function1<LogMessage, String> {
    public static final PanelViewLogger$logUpdateBlurPanelState$2 INSTANCE = new PanelViewLogger$logUpdateBlurPanelState$2();

    PanelViewLogger$logUpdateBlurPanelState$2() {
        super(1);
    }

    @NotNull
    public final String invoke(@NotNull LogMessage logMessage) {
        Intrinsics.checkParameterIsNotNull(logMessage, "$receiver");
        return "update blur panel state: " + logMessage.getStr1() + ' ' + "mKeyguardBouncerFraction=" + logMessage.getDouble1() + ' ' + "mPanelOpening=" + logMessage.getBool1() + " mPanelCollapsing=" + logMessage.getBool2() + ' ' + "mPanelStretching=" + logMessage.getBool3() + " mPanelAppeared=" + logMessage.getBool4() + ' ' + "mStretchLength=" + logMessage.getInt1() + " mSpringLength=" + logMessage.getInt2() + ' ';
    }
}
