package com.android.systemui;

import android.util.Pools;
import android.view.SurfaceControl;

public class TransactionPool {
    private final Pools.SynchronizedPool<SurfaceControl.Transaction> mTransactionPool = new Pools.SynchronizedPool<>(4);

    public SurfaceControl.Transaction acquire() {
        SurfaceControl.Transaction transaction = (SurfaceControl.Transaction) this.mTransactionPool.acquire();
        return transaction == null ? new SurfaceControl.Transaction() : transaction;
    }

    public void release(SurfaceControl.Transaction transaction) {
        if (!this.mTransactionPool.release(transaction)) {
            transaction.close();
        }
    }
}
