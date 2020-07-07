package com.android.systemui.recents.misc;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import java.util.ArrayList;

public class ReferenceCountedTrigger {
    int mCount;
    Runnable mDecrementRunnable;
    Runnable mErrorRunnable;
    ArrayList<Runnable> mFirstIncRunnables;
    Runnable mIncrementRunnable;
    ArrayList<Runnable> mLastDecRunnables;

    public ReferenceCountedTrigger() {
        this((Runnable) null, (Runnable) null, (Runnable) null);
    }

    public ReferenceCountedTrigger(Runnable runnable, Runnable runnable2, Runnable runnable3) {
        this.mFirstIncRunnables = new ArrayList<>();
        this.mLastDecRunnables = new ArrayList<>();
        this.mIncrementRunnable = new Runnable() {
            public void run() {
                ReferenceCountedTrigger.this.increment();
            }
        };
        this.mDecrementRunnable = new Runnable() {
            public void run() {
                ReferenceCountedTrigger.this.decrement();
            }
        };
        if (runnable != null) {
            this.mFirstIncRunnables.add(runnable);
        }
        if (runnable2 != null) {
            this.mLastDecRunnables.add(runnable2);
        }
        this.mErrorRunnable = runnable3;
    }

    public void increment() {
        if (this.mCount == 0 && !this.mFirstIncRunnables.isEmpty()) {
            int size = this.mFirstIncRunnables.size();
            for (int i = 0; i < size; i++) {
                this.mFirstIncRunnables.get(i).run();
            }
        }
        this.mCount++;
    }

    public void addLastDecrementRunnable(Runnable runnable) {
        this.mLastDecRunnables.add(runnable);
    }

    public void decrement() {
        this.mCount--;
        int i = this.mCount;
        if (i == 0) {
            flushLastDecrementRunnables();
        } else if (i < 0) {
            Runnable runnable = this.mErrorRunnable;
            if (runnable != null) {
                runnable.run();
                return;
            }
            throw new RuntimeException("Invalid ref count");
        }
    }

    public void flushLastDecrementRunnables() {
        if (!this.mLastDecRunnables.isEmpty()) {
            int size = this.mLastDecRunnables.size();
            for (int i = 0; i < size; i++) {
                this.mLastDecRunnables.get(i).run();
            }
        }
        this.mLastDecRunnables.clear();
    }

    public Animator.AnimatorListener decrementOnAnimationEnd() {
        return new AnimatorListenerAdapter() {
            private boolean hasEnded;

            public void onAnimationEnd(Animator animator) {
                if (!this.hasEnded) {
                    ReferenceCountedTrigger.this.decrement();
                    this.hasEnded = true;
                }
            }
        };
    }
}
