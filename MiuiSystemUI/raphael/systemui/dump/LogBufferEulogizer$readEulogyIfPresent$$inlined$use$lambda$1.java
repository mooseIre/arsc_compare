package com.android.systemui.dump;

import java.io.PrintWriter;
import java.util.function.Consumer;

/* compiled from: LogBufferEulogizer.kt */
final class LogBufferEulogizer$readEulogyIfPresent$$inlined$use$lambda$1<T> implements Consumer<String> {
    final /* synthetic */ PrintWriter $pw$inlined;

    LogBufferEulogizer$readEulogyIfPresent$$inlined$use$lambda$1(PrintWriter printWriter) {
        this.$pw$inlined = printWriter;
    }

    public final void accept(String str) {
        this.$pw$inlined.println(str);
    }
}
