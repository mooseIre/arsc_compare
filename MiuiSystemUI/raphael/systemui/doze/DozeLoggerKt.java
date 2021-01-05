package com.android.systemui.doze;

import java.text.SimpleDateFormat;
import java.util.Locale;
import org.jetbrains.annotations.NotNull;

/* compiled from: DozeLogger.kt */
public final class DozeLoggerKt {
    @NotNull
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("MM-dd HH:mm:ss.S", Locale.US);

    @NotNull
    public static final SimpleDateFormat getDATE_FORMAT() {
        return DATE_FORMAT;
    }
}
