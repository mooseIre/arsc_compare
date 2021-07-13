package com.android.systemui.statusbar.notification.mediacontrol;

import com.android.systemui.log.LogMessage;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.internal.Intrinsics;
import kotlin.jvm.internal.Lambda;
import org.jetbrains.annotations.NotNull;

/* access modifiers changed from: package-private */
/* compiled from: MediaControlLogger.kt */
public final class MediaControlLogger$logDataNotCurrentUser$2 extends Lambda implements Function1<LogMessage, String> {
    public static final MediaControlLogger$logDataNotCurrentUser$2 INSTANCE = new MediaControlLogger$logDataNotCurrentUser$2();

    MediaControlLogger$logDataNotCurrentUser$2() {
        super(1);
    }

    @NotNull
    public final String invoke(@NotNull LogMessage logMessage) {
        Intrinsics.checkParameterIsNotNull(logMessage, "$receiver");
        return "MediaData not this user: key=" + logMessage.getStr1() + ", userId=" + logMessage.getStr2();
    }
}
