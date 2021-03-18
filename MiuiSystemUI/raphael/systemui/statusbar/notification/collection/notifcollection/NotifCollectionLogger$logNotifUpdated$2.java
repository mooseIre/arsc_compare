package com.android.systemui.statusbar.notification.collection.notifcollection;

import com.android.systemui.log.LogMessage;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.internal.Intrinsics;
import kotlin.jvm.internal.Lambda;
import org.jetbrains.annotations.NotNull;

/* access modifiers changed from: package-private */
/* compiled from: NotifCollectionLogger.kt */
public final class NotifCollectionLogger$logNotifUpdated$2 extends Lambda implements Function1<LogMessage, String> {
    public static final NotifCollectionLogger$logNotifUpdated$2 INSTANCE = new NotifCollectionLogger$logNotifUpdated$2();

    NotifCollectionLogger$logNotifUpdated$2() {
        super(1);
    }

    @NotNull
    public final String invoke(@NotNull LogMessage logMessage) {
        Intrinsics.checkParameterIsNotNull(logMessage, "$receiver");
        return "UPDATED " + logMessage.getStr1();
    }
}
