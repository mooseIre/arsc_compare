package com.android.systemui.media;

import android.os.SystemProperties;
import java.util.concurrent.TimeUnit;

/* compiled from: MediaTimeoutListener.kt */
public final class MediaTimeoutListenerKt {
    /* access modifiers changed from: private */
    public static final long PAUSED_MEDIA_TIMEOUT = SystemProperties.getLong("debug.sysui.media_timeout", TimeUnit.MINUTES.toMillis(10));
}
