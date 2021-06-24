package com.android.systemui;

import android.content.Context;
import android.util.Log;
import com.miui.systemui.DebugConfig;
import java.lang.Thread;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;

/* access modifiers changed from: package-private */
/* compiled from: CodeBlue.kt */
public final class CodeBlue$Companion$startMonitoring$1 implements Thread.UncaughtExceptionHandler {
    final /* synthetic */ Context $context;
    final /* synthetic */ Thread.UncaughtExceptionHandler $preHandler;

    CodeBlue$Companion$startMonitoring$1(Context context, Thread.UncaughtExceptionHandler uncaughtExceptionHandler) {
        this.$context = context;
        this.$preHandler = uncaughtExceptionHandler;
    }

    public final void uncaughtException(@NotNull Thread thread, @NotNull Throwable th) {
        Intrinsics.checkParameterIsNotNull(thread, "thread");
        Intrinsics.checkParameterIsNotNull(th, "throwable");
        if (DebugConfig.DEBUG_CODE_BLUE) {
            Log.e("CodeBlue", "uncaughtException", th);
        }
        if (Intrinsics.areEqual(Thread.currentThread(), thread)) {
            try {
                CodeBlue.Companion.updateCrashInfo(this.$context);
                CodeBlue.Companion.updateCrashHandler(this.$context, th);
            } catch (Exception e) {
                Log.e("CodeBlue", "startMonitoring", e);
            }
        }
        Thread.UncaughtExceptionHandler uncaughtExceptionHandler = this.$preHandler;
        if (uncaughtExceptionHandler != null) {
            uncaughtExceptionHandler.uncaughtException(thread, th);
        }
    }
}
