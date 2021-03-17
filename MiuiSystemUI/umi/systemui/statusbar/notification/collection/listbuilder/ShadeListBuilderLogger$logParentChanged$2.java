package com.android.systemui.statusbar.notification.collection.listbuilder;

import com.android.systemui.log.LogMessage;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.internal.Intrinsics;
import kotlin.jvm.internal.Lambda;
import org.jetbrains.annotations.NotNull;

/* access modifiers changed from: package-private */
/* compiled from: ShadeListBuilderLogger.kt */
public final class ShadeListBuilderLogger$logParentChanged$2 extends Lambda implements Function1<LogMessage, String> {
    public static final ShadeListBuilderLogger$logParentChanged$2 INSTANCE = new ShadeListBuilderLogger$logParentChanged$2();

    ShadeListBuilderLogger$logParentChanged$2() {
        super(1);
    }

    @NotNull
    public final String invoke(@NotNull LogMessage logMessage) {
        Intrinsics.checkParameterIsNotNull(logMessage, "$receiver");
        if (logMessage.getStr1() == null && logMessage.getStr2() != null) {
            return "(Build " + logMessage.getInt1() + ")     Parent is {" + logMessage.getStr2() + '}';
        } else if (logMessage.getStr1() == null || logMessage.getStr2() != null) {
            return "(Build " + logMessage.getInt1() + ")     Reparent: {" + logMessage.getStr2() + "} -> {" + logMessage.getStr3() + '}';
        } else {
            return "(Build " + logMessage.getInt1() + ")     Parent was {" + logMessage.getStr1() + '}';
        }
    }
}
