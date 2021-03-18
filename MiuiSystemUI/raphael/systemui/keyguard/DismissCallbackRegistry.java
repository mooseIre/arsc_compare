package com.android.systemui.keyguard;

import com.android.internal.policy.IKeyguardDismissCallback;
import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.Executor;

public class DismissCallbackRegistry {
    private final ArrayList<DismissCallbackWrapper> mDismissCallbacks = new ArrayList<>();
    private final Executor mUiBgExecutor;

    public DismissCallbackRegistry(Executor executor) {
        this.mUiBgExecutor = executor;
    }

    public void addCallback(IKeyguardDismissCallback iKeyguardDismissCallback) {
        this.mDismissCallbacks.add(new DismissCallbackWrapper(iKeyguardDismissCallback));
    }

    public void notifyDismissCancelled() {
        for (int size = this.mDismissCallbacks.size() - 1; size >= 0; size--) {
            DismissCallbackWrapper dismissCallbackWrapper = this.mDismissCallbacks.get(size);
            Executor executor = this.mUiBgExecutor;
            Objects.requireNonNull(dismissCallbackWrapper);
            executor.execute(new Runnable() {
                /* class com.android.systemui.keyguard.$$Lambda$zM6bayhThdtgvBghgFXo519LeO0 */

                public final void run() {
                    DismissCallbackWrapper.this.notifyDismissCancelled();
                }
            });
        }
        this.mDismissCallbacks.clear();
    }

    public void notifyDismissSucceeded() {
        for (int size = this.mDismissCallbacks.size() - 1; size >= 0; size--) {
            DismissCallbackWrapper dismissCallbackWrapper = this.mDismissCallbacks.get(size);
            Executor executor = this.mUiBgExecutor;
            Objects.requireNonNull(dismissCallbackWrapper);
            executor.execute(new Runnable() {
                /* class com.android.systemui.keyguard.$$Lambda$2j_lq_QeR0jp4UUzPHOB_8BlctI */

                public final void run() {
                    DismissCallbackWrapper.this.notifyDismissSucceeded();
                }
            });
        }
        this.mDismissCallbacks.clear();
    }
}
