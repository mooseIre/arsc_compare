package com.android.systemui.dump;

import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public final class LogBufferEulogizerKt {
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("MM-dd HH:mm:ss.SSS", Locale.US);
    private static final long MAX_AGE_TO_DUMP = TimeUnit.HOURS.toMillis(48);
    private static final long MIN_WRITE_GAP = TimeUnit.MINUTES.toMillis(5);
}
