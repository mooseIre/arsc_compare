package com.android.systemui.statusbar.notification.stack;

import com.android.systemui.log.LogMessage;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.internal.Intrinsics;
import kotlin.jvm.internal.Lambda;
import org.jetbrains.annotations.NotNull;

/* access modifiers changed from: package-private */
/* compiled from: NotificationSectionsLogger.kt */
public final class NotificationSectionsLogger$logStartSectionUpdate$2 extends Lambda implements Function1<LogMessage, String> {
    final /* synthetic */ String $reason;

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    NotificationSectionsLogger$logStartSectionUpdate$2(String str) {
        super(1);
        this.$reason = str;
    }

    @NotNull
    public final String invoke(@NotNull LogMessage logMessage) {
        Intrinsics.checkParameterIsNotNull(logMessage, "$receiver");
        return "Updating section boundaries: " + this.$reason;
    }
}
