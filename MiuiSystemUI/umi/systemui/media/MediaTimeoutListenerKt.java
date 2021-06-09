package com.android.systemui.media;

import android.os.SystemProperties;
import java.util.concurrent.TimeUnit;

public final class MediaTimeoutListenerKt {
    private static final long PAUSED_MEDIA_TIMEOUT = SystemProperties.getLong("debug.sysui.media_timeout", TimeUnit.MINUTES.toMillis(10));
}
