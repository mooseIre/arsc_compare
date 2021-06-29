package com.android.systemui.statusbar.phone;

import com.android.systemui.log.LogMessage;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.internal.Intrinsics;
import kotlin.jvm.internal.Lambda;
import org.jetbrains.annotations.NotNull;

/* access modifiers changed from: package-private */
/* compiled from: PanelViewLogger.kt */
public final class PanelViewLogger$logBlurRequest$2 extends Lambda implements Function1<LogMessage, String> {
    final /* synthetic */ boolean $anim;

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    PanelViewLogger$logBlurRequest$2(boolean z) {
        super(1);
        this.$anim = z;
    }

    @NotNull
    public final String invoke(@NotNull LogMessage logMessage) {
        Intrinsics.checkParameterIsNotNull(logMessage, "$receiver");
        return "blur request : anim=" + this.$anim + " reason=" + logMessage.getStr1() + ' ' + logMessage.getStr2() + " -> " + logMessage.getStr3();
    }
}
