package com.android.systemui.util.concurrency;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;

public class ExecutorImpl implements DelayableExecutor {
    private final Handler mHandler;

    ExecutorImpl(Looper looper) {
        this.mHandler = new Handler(looper, new Handler.Callback() {
            /* class com.android.systemui.util.concurrency.$$Lambda$ExecutorImpl$vXdc7rv1NdEmVmxIWaGxknUGa10 */

            public final boolean handleMessage(Message message) {
                return ExecutorImpl.this.onHandleMessage(message);
            }
        });
    }

    public void execute(Runnable runnable) {
        if (!this.mHandler.post(runnable)) {
            throw new RejectedExecutionException(this.mHandler + " is shutting down");
        }
    }

    @Override // com.android.systemui.util.concurrency.DelayableExecutor
    public Runnable executeDelayed(Runnable runnable, long j, TimeUnit timeUnit) {
        ExecutionToken executionToken = new ExecutionToken(runnable);
        this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(0, executionToken), timeUnit.toMillis(j));
        return executionToken;
    }

    /* access modifiers changed from: private */
    public boolean onHandleMessage(Message message) {
        if (message.what == 0) {
            ((ExecutionToken) message.obj).runnable.run();
            return true;
        }
        throw new IllegalStateException("Unrecognized message: " + message.what);
    }

    /* access modifiers changed from: private */
    public class ExecutionToken implements Runnable {
        public final Runnable runnable;

        private ExecutionToken(Runnable runnable2) {
            this.runnable = runnable2;
        }

        public void run() {
            ExecutorImpl.this.mHandler.removeCallbacksAndMessages(this);
        }
    }
}
