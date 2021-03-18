package com.android.systemui.dump;

import android.util.Log;

/* access modifiers changed from: package-private */
/* compiled from: LogBufferFreezer.kt */
public final class LogBufferFreezer$onBugreportStarted$1 implements Runnable {
    final /* synthetic */ LogBufferFreezer this$0;

    LogBufferFreezer$onBugreportStarted$1(LogBufferFreezer logBufferFreezer) {
        this.this$0 = logBufferFreezer;
    }

    public final void run() {
        Log.i("LogBufferFreezer", "Unfreezing log buffers");
        this.this$0.pendingToken = null;
        this.this$0.dumpManager.unfreezeBuffers();
    }
}
