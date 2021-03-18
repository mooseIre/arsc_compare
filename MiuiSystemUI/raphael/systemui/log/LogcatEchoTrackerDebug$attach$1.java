package com.android.systemui.log;

import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;

/* compiled from: LogcatEchoTrackerDebug.kt */
public final class LogcatEchoTrackerDebug$attach$1 extends ContentObserver {
    final /* synthetic */ LogcatEchoTrackerDebug this$0;

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    LogcatEchoTrackerDebug$attach$1(LogcatEchoTrackerDebug logcatEchoTrackerDebug, Looper looper, Handler handler) {
        super(handler);
        this.this$0 = logcatEchoTrackerDebug;
    }

    public void onChange(boolean z, @NotNull Uri uri) {
        Intrinsics.checkParameterIsNotNull(uri, "uri");
        super.onChange(z, uri);
        LogcatEchoTrackerDebug.access$getCachedBufferLevels$p(this.this$0).clear();
    }
}
