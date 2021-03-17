package com.android.systemui.statusbar.notification.collection.listbuilder;

import com.android.systemui.log.LogMessage;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.internal.Intrinsics;
import kotlin.jvm.internal.Lambda;
import org.jetbrains.annotations.NotNull;

/* access modifiers changed from: package-private */
/* compiled from: ShadeListBuilderLogger.kt */
public final class ShadeListBuilderLogger$logEntryAttachStateChanged$2 extends Lambda implements Function1<LogMessage, String> {
    public static final ShadeListBuilderLogger$logEntryAttachStateChanged$2 INSTANCE = new ShadeListBuilderLogger$logEntryAttachStateChanged$2();

    ShadeListBuilderLogger$logEntryAttachStateChanged$2() {
        super(1);
    }

    @NotNull
    public final String invoke(@NotNull LogMessage logMessage) {
        Intrinsics.checkParameterIsNotNull(logMessage, "$receiver");
        if (logMessage.getStr2() == null && logMessage.getStr3() != null) {
            return "(Build " + logMessage.getInt1() + ") ATTACHED {" + logMessage.getStr1() + '}';
        } else if (logMessage.getStr2() == null || logMessage.getStr3() != null) {
            return "(Build " + logMessage.getInt1() + ") MODIFIED {" + logMessage.getStr1() + '}';
        } else {
            return "(Build " + logMessage.getInt1() + ") DETACHED {" + logMessage.getStr1() + '}';
        }
    }
}
