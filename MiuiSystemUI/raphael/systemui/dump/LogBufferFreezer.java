package com.android.systemui.dump;

import android.content.IntentFilter;
import android.os.UserHandle;
import android.util.Log;
import com.android.systemui.broadcast.BroadcastDispatcher;
import com.android.systemui.util.concurrency.DelayableExecutor;
import java.util.concurrent.TimeUnit;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;

/* compiled from: LogBufferFreezer.kt */
public final class LogBufferFreezer {
    private final DumpManager dumpManager;
    private final DelayableExecutor executor;
    private final long freezeDuration;
    private Runnable pendingToken;

    public LogBufferFreezer(@NotNull DumpManager dumpManager2, @NotNull DelayableExecutor delayableExecutor, long j) {
        Intrinsics.checkParameterIsNotNull(dumpManager2, "dumpManager");
        Intrinsics.checkParameterIsNotNull(delayableExecutor, "executor");
        this.dumpManager = dumpManager2;
        this.executor = delayableExecutor;
        this.freezeDuration = j;
    }

    /* JADX INFO: this call moved to the top of the method (can break code semantics) */
    public LogBufferFreezer(@NotNull DumpManager dumpManager2, @NotNull DelayableExecutor delayableExecutor) {
        this(dumpManager2, delayableExecutor, TimeUnit.MINUTES.toMillis(5));
        Intrinsics.checkParameterIsNotNull(dumpManager2, "dumpManager");
        Intrinsics.checkParameterIsNotNull(delayableExecutor, "executor");
    }

    public final void attach(@NotNull BroadcastDispatcher broadcastDispatcher) {
        Intrinsics.checkParameterIsNotNull(broadcastDispatcher, "broadcastDispatcher");
        LogBufferFreezer$attach$1 logBufferFreezer$attach$1 = new LogBufferFreezer$attach$1(this);
        IntentFilter intentFilter = new IntentFilter("com.android.internal.intent.action.BUGREPORT_STARTED");
        DelayableExecutor delayableExecutor = this.executor;
        UserHandle userHandle = UserHandle.ALL;
        Intrinsics.checkExpressionValueIsNotNull(userHandle, "UserHandle.ALL");
        broadcastDispatcher.registerReceiver(logBufferFreezer$attach$1, intentFilter, delayableExecutor, userHandle);
    }

    /* access modifiers changed from: private */
    public final void onBugreportStarted() {
        Runnable runnable = this.pendingToken;
        if (runnable != null) {
            runnable.run();
        }
        Log.i("LogBufferFreezer", "Freezing log buffers");
        this.dumpManager.freezeBuffers();
        this.pendingToken = this.executor.executeDelayed(new LogBufferFreezer$onBugreportStarted$1(this), this.freezeDuration);
    }
}
