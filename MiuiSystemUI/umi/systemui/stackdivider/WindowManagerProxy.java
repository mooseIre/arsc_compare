package com.android.systemui.stackdivider;

import android.app.ActivityTaskManager;
import android.graphics.Rect;
import android.os.RemoteException;
import android.util.Log;
import android.view.WindowManagerGlobal;
import com.android.internal.annotations.GuardedBy;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class WindowManagerProxy {
    private static final WindowManagerProxy sInstance = new WindowManagerProxy();
    private final Runnable mDimLayerRunnable = new Runnable(this) {
        public void run() {
        }
    };
    private final Runnable mDismissRunnable = new Runnable(this) {
        public void run() {
        }
    };
    /* access modifiers changed from: private */
    @GuardedBy({"mDockedRect"})
    public final Rect mDockedRect = new Rect();
    private final ExecutorService mExecutor = Executors.newSingleThreadExecutor();
    private final Runnable mMaximizeRunnable = new Runnable(this) {
        public void run() {
        }
    };
    private final Runnable mResizeRunnable = new Runnable() {
        public void run() {
            synchronized (WindowManagerProxy.this.mDockedRect) {
                WindowManagerProxy.this.mTmpRect1.set(WindowManagerProxy.this.mDockedRect);
                WindowManagerProxy.this.mTmpRect2.set(WindowManagerProxy.this.mTempDockedTaskRect);
                WindowManagerProxy.this.mTmpRect3.set(WindowManagerProxy.this.mTempDockedInsetRect);
                WindowManagerProxy.this.mTmpRect4.set(WindowManagerProxy.this.mTempOtherTaskRect);
                WindowManagerProxy.this.mTmpRect5.set(WindowManagerProxy.this.mTempOtherInsetRect);
            }
            try {
                ActivityTaskManager.getService().resizeDockedStack(WindowManagerProxy.this.mTmpRect1, WindowManagerProxy.this.mTmpRect2.isEmpty() ? null : WindowManagerProxy.this.mTmpRect2, WindowManagerProxy.this.mTmpRect3.isEmpty() ? null : WindowManagerProxy.this.mTmpRect3, WindowManagerProxy.this.mTmpRect4.isEmpty() ? null : WindowManagerProxy.this.mTmpRect4, WindowManagerProxy.this.mTmpRect5.isEmpty() ? null : WindowManagerProxy.this.mTmpRect5);
            } catch (RemoteException e) {
                Log.w("WindowManagerProxy", "Failed to resize stack: " + e);
            }
        }
    };
    private final Runnable mSetTouchableRegionRunnable = new Runnable() {
        public void run() {
            try {
                synchronized (WindowManagerProxy.this.mDockedRect) {
                    WindowManagerProxy.this.mTmpRect1.set(WindowManagerProxy.this.mTouchableRegion);
                }
                WindowManagerGlobal.getWindowManagerService().setDockedStackDividerTouchRegion(WindowManagerProxy.this.mTmpRect1);
            } catch (RemoteException e) {
                Log.w("WindowManagerProxy", "Failed to set touchable region: " + e);
            }
        }
    };
    private final Runnable mSwapRunnable = new Runnable(this) {
        public void run() {
        }
    };
    /* access modifiers changed from: private */
    public final Rect mTempDockedInsetRect = new Rect();
    /* access modifiers changed from: private */
    public final Rect mTempDockedTaskRect = new Rect();
    /* access modifiers changed from: private */
    public final Rect mTempOtherInsetRect = new Rect();
    /* access modifiers changed from: private */
    public final Rect mTempOtherTaskRect = new Rect();
    /* access modifiers changed from: private */
    public final Rect mTmpRect1 = new Rect();
    /* access modifiers changed from: private */
    public final Rect mTmpRect2 = new Rect();
    /* access modifiers changed from: private */
    public final Rect mTmpRect3 = new Rect();
    /* access modifiers changed from: private */
    public final Rect mTmpRect4 = new Rect();
    /* access modifiers changed from: private */
    public final Rect mTmpRect5 = new Rect();
    /* access modifiers changed from: private */
    @GuardedBy({"mDockedRect"})
    public final Rect mTouchableRegion = new Rect();

    private WindowManagerProxy() {
    }

    public static WindowManagerProxy getInstance() {
        return sInstance;
    }

    public void resizeDockedStack(Rect rect, Rect rect2, Rect rect3, Rect rect4, Rect rect5, boolean z) {
        synchronized (this.mDockedRect) {
            this.mDockedRect.set(rect);
            if (rect2 != null) {
                this.mTempDockedTaskRect.set(rect2);
            } else {
                this.mTempDockedTaskRect.setEmpty();
            }
            if (rect3 != null) {
                this.mTempDockedInsetRect.set(rect3);
            } else {
                this.mTempDockedInsetRect.setEmpty();
            }
            if (rect4 != null) {
                this.mTempOtherTaskRect.set(rect4);
            } else {
                this.mTempOtherTaskRect.setEmpty();
            }
            if (rect5 != null) {
                this.mTempOtherInsetRect.set(rect5);
            } else {
                this.mTempOtherInsetRect.setEmpty();
            }
        }
        if (z) {
            this.mExecutor.execute(this.mResizeRunnable);
        }
    }

    public void dismissDockedStack() {
        this.mExecutor.execute(this.mDismissRunnable);
    }

    public void maximizeDockedStack() {
        this.mExecutor.execute(this.mMaximizeRunnable);
    }

    public void setResizing(final boolean z) {
        this.mExecutor.execute(new Runnable(this) {
            public void run() {
                try {
                    ActivityTaskManager.getService().setSplitScreenResizing(z);
                } catch (RemoteException e) {
                    Log.w("WindowManagerProxy", "Error calling setDockedStackResizing: " + e);
                }
            }
        });
    }

    public int getDockSide() {
        try {
            return WindowManagerGlobal.getWindowManagerService().getDockedStackSide();
        } catch (RemoteException e) {
            Log.w("WindowManagerProxy", "Failed to get dock side: " + e);
            return -1;
        }
    }

    public void setResizeDimLayer(boolean z, int i, int i2, float f) {
        this.mExecutor.execute(this.mDimLayerRunnable);
    }

    public void swapTasks() {
        this.mExecutor.execute(this.mSwapRunnable);
    }

    public void setTouchRegion(Rect rect) {
        synchronized (this.mDockedRect) {
            this.mTouchableRegion.set(rect);
        }
        this.mExecutor.execute(this.mSetTouchableRegionRunnable);
    }
}
