package com.android.systemui.stackdivider;

import android.app.ActivityManager;
import android.graphics.Rect;
import android.os.RemoteException;
import android.util.Log;
import android.view.SurfaceControl;
import android.view.SurfaceSession;
import android.window.TaskOrganizer;

/* access modifiers changed from: package-private */
public class SplitScreenTaskOrganizer extends TaskOrganizer {
    final Divider mDivider;
    Rect mHomeBounds = new Rect();
    ActivityManager.RunningTaskInfo mPrimary;
    SurfaceControl mPrimaryDim;
    SurfaceControl mPrimarySurface;
    ActivityManager.RunningTaskInfo mSecondary;
    SurfaceControl mSecondaryDim;
    SurfaceControl mSecondarySurface;
    private boolean mSplitScreenSupported = false;
    final SurfaceSession mSurfaceSession = new SurfaceSession();

    SplitScreenTaskOrganizer(Divider divider) {
        this.mDivider = divider;
    }

    /* access modifiers changed from: package-private */
    public void init() throws RemoteException {
        registerOrganizer(3);
        registerOrganizer(4);
        synchronized (this) {
            try {
                this.mPrimary = TaskOrganizer.createRootTask(0, 3);
                this.mSecondary = TaskOrganizer.createRootTask(0, 4);
            } catch (Exception e) {
                unregisterOrganizer();
                throw e;
            } catch (Throwable th) {
                throw th;
            }
        }
    }

    /* access modifiers changed from: package-private */
    public boolean isSplitScreenSupported() {
        return this.mSplitScreenSupported;
    }

    /* access modifiers changed from: package-private */
    public SurfaceControl.Transaction getTransaction() {
        return this.mDivider.mTransactionPool.acquire();
    }

    /* access modifiers changed from: package-private */
    public void releaseTransaction(SurfaceControl.Transaction transaction) {
        this.mDivider.mTransactionPool.release(transaction);
    }

    public void onTaskAppeared(ActivityManager.RunningTaskInfo runningTaskInfo, SurfaceControl surfaceControl) {
        synchronized (this) {
            if (this.mPrimary != null) {
                if (this.mSecondary != null) {
                    if (runningTaskInfo.token.equals(this.mPrimary.token)) {
                        this.mPrimarySurface = surfaceControl;
                    } else if (runningTaskInfo.token.equals(this.mSecondary.token)) {
                        this.mSecondarySurface = surfaceControl;
                    }
                    if (!(this.mSplitScreenSupported || this.mPrimarySurface == null || this.mSecondarySurface == null)) {
                        this.mSplitScreenSupported = true;
                        this.mPrimaryDim = new SurfaceControl.Builder(this.mSurfaceSession).setParent(this.mPrimarySurface).setColorLayer().setName("Primary Divider Dim").setCallsite("SplitScreenTaskOrganizer.onTaskAppeared").build();
                        this.mSecondaryDim = new SurfaceControl.Builder(this.mSurfaceSession).setParent(this.mSecondarySurface).setColorLayer().setName("Secondary Divider Dim").setCallsite("SplitScreenTaskOrganizer.onTaskAppeared").build();
                        SurfaceControl.Transaction transaction = getTransaction();
                        transaction.setLayer(this.mPrimaryDim, Integer.MAX_VALUE);
                        transaction.setColor(this.mPrimaryDim, new float[]{0.0f, 0.0f, 0.0f});
                        transaction.setLayer(this.mSecondaryDim, Integer.MAX_VALUE);
                        transaction.setColor(this.mSecondaryDim, new float[]{0.0f, 0.0f, 0.0f});
                        transaction.apply();
                        releaseTransaction(transaction);
                    }
                    return;
                }
            }
            Log.w("SplitScreenTaskOrg", "Received onTaskAppeared before creating root tasks " + runningTaskInfo);
        }
    }

    public void onTaskVanished(ActivityManager.RunningTaskInfo runningTaskInfo) {
        synchronized (this) {
            boolean z = true;
            boolean z2 = this.mPrimary != null && runningTaskInfo.token.equals(this.mPrimary.token);
            if (this.mSecondary == null || !runningTaskInfo.token.equals(this.mSecondary.token)) {
                z = false;
            }
            if (this.mSplitScreenSupported && (z2 || z)) {
                this.mSplitScreenSupported = false;
                SurfaceControl.Transaction transaction = getTransaction();
                transaction.remove(this.mPrimaryDim);
                transaction.remove(this.mSecondaryDim);
                transaction.remove(this.mPrimarySurface);
                transaction.remove(this.mSecondarySurface);
                transaction.apply();
                releaseTransaction(transaction);
                this.mDivider.onTaskVanished();
            }
        }
    }

    public void onTaskInfoChanged(ActivityManager.RunningTaskInfo runningTaskInfo) {
        if (runningTaskInfo.displayId == 0) {
            this.mDivider.getHandler().post(new Runnable(runningTaskInfo) {
                /* class com.android.systemui.stackdivider.$$Lambda$SplitScreenTaskOrganizer$VFKjLFziXUrC1SQQoEI4rRScXR8 */
                public final /* synthetic */ ActivityManager.RunningTaskInfo f$1;

                {
                    this.f$1 = r2;
                }

                public final void run() {
                    SplitScreenTaskOrganizer.this.lambda$onTaskInfoChanged$0$SplitScreenTaskOrganizer(this.f$1);
                }
            });
        }
    }

    /* access modifiers changed from: private */
    /* renamed from: handleTaskInfoChanged */
    public void lambda$onTaskInfoChanged$0(ActivityManager.RunningTaskInfo runningTaskInfo) {
        if (!this.mSplitScreenSupported) {
            Log.e("SplitScreenTaskOrg", "Got handleTaskInfoChanged when not initialized: " + runningTaskInfo);
            return;
        }
        int i = this.mSecondary.topActivityType;
        boolean z = false;
        boolean z2 = i == 2 || (i == 3 && this.mDivider.isHomeStackResizable());
        boolean z3 = this.mPrimary.topActivityType == 0;
        boolean z4 = this.mSecondary.topActivityType == 0;
        if (runningTaskInfo.token.asBinder() == this.mPrimary.token.asBinder()) {
            this.mPrimary = runningTaskInfo;
        } else if (runningTaskInfo.token.asBinder() == this.mSecondary.token.asBinder()) {
            this.mSecondary = runningTaskInfo;
        }
        boolean z5 = this.mPrimary.topActivityType == 0;
        boolean z6 = this.mSecondary.topActivityType == 0;
        int i2 = this.mSecondary.topActivityType;
        if (i2 == 2 || (i2 == 3 && this.mDivider.isHomeStackResizable())) {
            z = true;
        }
        if (z5 != z3 || z4 != z6 || z2 != z) {
            if (z5 || z6) {
                if (this.mDivider.isDividerVisible()) {
                    this.mDivider.startDismissSplit();
                } else if (!z5 && z3 && z4) {
                    this.mDivider.startEnterSplit();
                }
            } else if (z) {
                this.mDivider.ensureMinimizedSplit();
            } else {
                this.mDivider.ensureNormalSplit();
            }
        }
    }
}
