package com.android.systemui.statusbar.notification.collection.notifcollection;

import com.android.systemui.log.LogMessage;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.internal.Intrinsics;
import kotlin.jvm.internal.Lambda;
import org.jetbrains.annotations.NotNull;

/* access modifiers changed from: package-private */
/* compiled from: NotifCollectionLogger.kt */
public final class NotifCollectionLogger$logDismissAll$2 extends Lambda implements Function1<LogMessage, String> {
    public static final NotifCollectionLogger$logDismissAll$2 INSTANCE = new NotifCollectionLogger$logDismissAll$2();

    NotifCollectionLogger$logDismissAll$2() {
        super(1);
    }

    @NotNull
    public final String invoke(@NotNull LogMessage logMessage) {
        Intrinsics.checkParameterIsNotNull(logMessage, "$receiver");
        return "DISMISS ALL notifications for user " + logMessage.getInt1();
    }
}
