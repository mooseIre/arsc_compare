package com.android.systemui.util.concurrency;

import com.android.systemui.util.concurrency.RepeatableExecutorImpl;
import java.util.concurrent.TimeUnit;

/* access modifiers changed from: package-private */
public class RepeatableExecutorImpl implements RepeatableExecutor {
    private final DelayableExecutor mExecutor;

    RepeatableExecutorImpl(DelayableExecutor delayableExecutor) {
        this.mExecutor = delayableExecutor;
    }

    public void execute(Runnable runnable) {
        this.mExecutor.execute(runnable);
    }

    @Override // com.android.systemui.util.concurrency.RepeatableExecutor
    public Runnable executeRepeatedly(Runnable runnable, long j, long j2, TimeUnit timeUnit) {
        ExecutionToken executionToken = new ExecutionToken(runnable, j2, timeUnit);
        executionToken.start(j, timeUnit);
        return new Runnable() {
            /* class com.android.systemui.util.concurrency.$$Lambda$ilFrHmNxnUJ5ovyV0TYCbwRC8uo */

            public final void run() {
                RepeatableExecutorImpl.ExecutionToken.this.cancel();
            }
        };
    }

    /* access modifiers changed from: private */
    public class ExecutionToken implements Runnable {
        private Runnable mCancel;
        private final Runnable mCommand;
        private final long mDelay;
        private final Object mLock = new Object();
        private final TimeUnit mUnit;

        ExecutionToken(Runnable runnable, long j, TimeUnit timeUnit) {
            this.mCommand = runnable;
            this.mDelay = j;
            this.mUnit = timeUnit;
        }

        public void run() {
            this.mCommand.run();
            synchronized (this.mLock) {
                if (this.mCancel != null) {
                    this.mCancel = RepeatableExecutorImpl.this.mExecutor.executeDelayed(this, this.mDelay, this.mUnit);
                }
            }
        }

        public void start(long j, TimeUnit timeUnit) {
            synchronized (this.mLock) {
                this.mCancel = RepeatableExecutorImpl.this.mExecutor.executeDelayed(this, j, timeUnit);
            }
        }

        public void cancel() {
            synchronized (this.mLock) {
                if (this.mCancel != null) {
                    this.mCancel.run();
                    this.mCancel = null;
                }
            }
        }
    }
}
