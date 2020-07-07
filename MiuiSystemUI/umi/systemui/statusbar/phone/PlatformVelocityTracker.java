package com.android.systemui.statusbar.phone;

import android.util.Pools;
import android.view.MotionEvent;
import android.view.VelocityTracker;

public class PlatformVelocityTracker implements VelocityTrackerInterface {
    private static final Pools.SynchronizedPool<PlatformVelocityTracker> sPool = new Pools.SynchronizedPool<>(2);
    private VelocityTracker mTracker;

    public static PlatformVelocityTracker obtain() {
        PlatformVelocityTracker platformVelocityTracker = (PlatformVelocityTracker) sPool.acquire();
        if (platformVelocityTracker == null) {
            platformVelocityTracker = new PlatformVelocityTracker();
        }
        platformVelocityTracker.setTracker(VelocityTracker.obtain());
        return platformVelocityTracker;
    }

    public void setTracker(VelocityTracker velocityTracker) {
        this.mTracker = velocityTracker;
    }

    public void addMovement(MotionEvent motionEvent) {
        this.mTracker.addMovement(motionEvent);
    }

    public void computeCurrentVelocity(int i) {
        this.mTracker.computeCurrentVelocity(i);
    }

    public float getXVelocity() {
        return this.mTracker.getXVelocity();
    }

    public float getYVelocity() {
        return this.mTracker.getYVelocity();
    }

    public void recycle() {
        this.mTracker.recycle();
        sPool.release(this);
    }
}
