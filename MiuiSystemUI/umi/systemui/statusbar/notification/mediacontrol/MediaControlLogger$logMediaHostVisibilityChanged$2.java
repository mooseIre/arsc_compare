package com.android.systemui.statusbar.notification.mediacontrol;

import com.android.systemui.log.LogMessage;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.internal.Intrinsics;
import kotlin.jvm.internal.Lambda;
import org.jetbrains.annotations.NotNull;

/* access modifiers changed from: package-private */
/* compiled from: MediaControlLogger.kt */
public final class MediaControlLogger$logMediaHostVisibilityChanged$2 extends Lambda implements Function1<LogMessage, String> {
    public static final MediaControlLogger$logMediaHostVisibilityChanged$2 INSTANCE = new MediaControlLogger$logMediaHostVisibilityChanged$2();

    MediaControlLogger$logMediaHostVisibilityChanged$2() {
        super(1);
    }

    @NotNull
    public final String invoke(@NotNull LogMessage logMessage) {
        Intrinsics.checkParameterIsNotNull(logMessage, "$receiver");
        return "MediaHost visibility changed: visible=" + logMessage.getBool1() + ", visibility=" + logMessage.getInt1();
    }
}
