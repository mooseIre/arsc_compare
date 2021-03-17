package com.android.systemui.stackdivider;

import android.os.Handler;
import android.util.Slog;
import android.view.SurfaceControl;
import android.window.WindowContainerTransaction;
import android.window.WindowContainerTransactionCallback;
import android.window.WindowOrganizer;
import com.android.systemui.TransactionPool;
import com.android.systemui.stackdivider.SyncTransactionQueue;
import java.util.ArrayList;

/* access modifiers changed from: package-private */
public class SyncTransactionQueue {
    private final Handler mHandler;
    private SyncCallback mInFlight = null;
    private final Runnable mOnReplyTimeout = new Runnable() {
        /* class com.android.systemui.stackdivider.$$Lambda$SyncTransactionQueue$3R1SopzfkzcMF9bQ5SW13TqpBDA */

        public final void run() {
            SyncTransactionQueue.this.lambda$new$0$SyncTransactionQueue();
        }
    };
    private final ArrayList<SyncCallback> mQueue = new ArrayList<>();
    private final ArrayList<TransactionRunnable> mRunnables = new ArrayList<>();
    private final TransactionPool mTransactionPool;

    /* access modifiers changed from: package-private */
    public interface TransactionRunnable {
        void runWithTransaction(SurfaceControl.Transaction transaction);
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$new$0 */
    public /* synthetic */ void lambda$new$0$SyncTransactionQueue() {
        synchronized (this.mQueue) {
            if (this.mInFlight != null && this.mQueue.contains(this.mInFlight)) {
                Slog.w("SyncTransactionQueue", "Sync Transaction timed-out: " + this.mInFlight.mWCT);
                this.mInFlight.onTransactionReady(this.mInFlight.mId, new SurfaceControl.Transaction());
            }
        }
    }

    SyncTransactionQueue(TransactionPool transactionPool, Handler handler) {
        this.mTransactionPool = transactionPool;
        this.mHandler = handler;
    }

    /* access modifiers changed from: package-private */
    public void queue(WindowContainerTransaction windowContainerTransaction) {
        SyncCallback syncCallback = new SyncCallback(windowContainerTransaction);
        synchronized (this.mQueue) {
            this.mQueue.add(syncCallback);
            if (this.mQueue.size() == 1) {
                syncCallback.send();
            }
        }
    }

    /* access modifiers changed from: package-private */
    public boolean queueIfWaiting(WindowContainerTransaction windowContainerTransaction) {
        synchronized (this.mQueue) {
            if (this.mQueue.isEmpty()) {
                return false;
            }
            SyncCallback syncCallback = new SyncCallback(windowContainerTransaction);
            this.mQueue.add(syncCallback);
            if (this.mQueue.size() == 1) {
                syncCallback.send();
            }
            return true;
        }
    }

    /* access modifiers changed from: package-private */
    public void runInSync(TransactionRunnable transactionRunnable) {
        synchronized (this.mQueue) {
            if (this.mInFlight != null) {
                this.mRunnables.add(transactionRunnable);
                return;
            }
            SurfaceControl.Transaction acquire = this.mTransactionPool.acquire();
            transactionRunnable.runWithTransaction(acquire);
            acquire.apply();
            this.mTransactionPool.release(acquire);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void onTransactionReceived(SurfaceControl.Transaction transaction) {
        int size = this.mRunnables.size();
        for (int i = 0; i < size; i++) {
            this.mRunnables.get(i).runWithTransaction(transaction);
        }
        this.mRunnables.clear();
        transaction.apply();
        transaction.close();
    }

    /* access modifiers changed from: private */
    public class SyncCallback extends WindowContainerTransactionCallback {
        int mId = -1;
        final WindowContainerTransaction mWCT;

        SyncCallback(WindowContainerTransaction windowContainerTransaction) {
            this.mWCT = windowContainerTransaction;
        }

        /* access modifiers changed from: package-private */
        public void send() {
            if (SyncTransactionQueue.this.mInFlight == null) {
                SyncTransactionQueue.this.mInFlight = this;
                this.mId = new WindowOrganizer().applySyncTransaction(this.mWCT, this);
                SyncTransactionQueue.this.mHandler.postDelayed(SyncTransactionQueue.this.mOnReplyTimeout, 5300);
                return;
            }
            throw new IllegalStateException("Sync Transactions must be serialized. In Flight: " + SyncTransactionQueue.this.mInFlight.mId + " - " + SyncTransactionQueue.this.mInFlight.mWCT);
        }

        public void onTransactionReady(int i, SurfaceControl.Transaction transaction) {
            SyncTransactionQueue.this.mHandler.post(new Runnable(i, transaction) {
                /* class com.android.systemui.stackdivider.$$Lambda$SyncTransactionQueue$SyncCallback$6QadB1gd31ucfrIts4w_5mHJHE */
                public final /* synthetic */ int f$1;
                public final /* synthetic */ SurfaceControl.Transaction f$2;

                {
                    this.f$1 = r2;
                    this.f$2 = r3;
                }

                public final void run() {
                    SyncTransactionQueue.SyncCallback.this.lambda$onTransactionReady$0$SyncTransactionQueue$SyncCallback(this.f$1, this.f$2);
                }
            });
        }

        /* access modifiers changed from: private */
        /* renamed from: lambda$onTransactionReady$0 */
        public /* synthetic */ void lambda$onTransactionReady$0$SyncTransactionQueue$SyncCallback(int i, SurfaceControl.Transaction transaction) {
            synchronized (SyncTransactionQueue.this.mQueue) {
                if (this.mId != i) {
                    Slog.e("SyncTransactionQueue", "Got an unexpected onTransactionReady. Expected " + this.mId + " but got " + i);
                    return;
                }
                SyncTransactionQueue.this.mInFlight = null;
                SyncTransactionQueue.this.mHandler.removeCallbacks(SyncTransactionQueue.this.mOnReplyTimeout);
                SyncTransactionQueue.this.mQueue.remove(this);
                SyncTransactionQueue.this.onTransactionReceived(transaction);
                if (!SyncTransactionQueue.this.mQueue.isEmpty()) {
                    ((SyncCallback) SyncTransactionQueue.this.mQueue.get(0)).send();
                }
            }
        }
    }
}
