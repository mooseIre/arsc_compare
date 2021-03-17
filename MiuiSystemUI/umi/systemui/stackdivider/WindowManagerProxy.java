package com.android.systemui.stackdivider;

import android.app.ActivityManager;
import android.app.ActivityTaskManager;
import android.graphics.Rect;
import android.os.Handler;
import android.os.RemoteException;
import android.util.Log;
import android.view.SurfaceControl;
import android.view.WindowManagerGlobal;
import android.window.TaskOrganizer;
import android.window.WindowContainerToken;
import android.window.WindowContainerTransaction;
import android.window.WindowOrganizer;
import com.android.internal.annotations.GuardedBy;
import com.android.systemui.TransactionPool;
import com.android.systemui.stackdivider.SyncTransactionQueue;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Predicate;

public class WindowManagerProxy {
    private static final int[] HOME_AND_RECENTS = {2, 3};
    @GuardedBy({"mDockedRect"})
    private final Rect mDockedRect = new Rect();
    private final ExecutorService mExecutor = Executors.newSingleThreadExecutor();
    private final Runnable mSetTouchableRegionRunnable = new Runnable() {
        /* class com.android.systemui.stackdivider.WindowManagerProxy.AnonymousClass1 */

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
    private final SyncTransactionQueue mSyncTransactionQueue;
    private final Rect mTmpRect1 = new Rect();
    @GuardedBy({"mDockedRect"})
    private final Rect mTouchableRegion = new Rect();

    WindowManagerProxy(TransactionPool transactionPool, Handler handler) {
        this.mSyncTransactionQueue = new SyncTransactionQueue(transactionPool, handler);
    }

    /* access modifiers changed from: package-private */
    public void dismissOrMaximizeDocked(SplitScreenTaskOrganizer splitScreenTaskOrganizer, SplitDisplayLayout splitDisplayLayout, boolean z) {
        this.mExecutor.execute(new Runnable(splitScreenTaskOrganizer, splitDisplayLayout, z) {
            /* class com.android.systemui.stackdivider.$$Lambda$WindowManagerProxy$rVfdVu_tfFj6B198IbDRYIqAmkk */
            public final /* synthetic */ SplitScreenTaskOrganizer f$1;
            public final /* synthetic */ SplitDisplayLayout f$2;
            public final /* synthetic */ boolean f$3;

            {
                this.f$1 = r2;
                this.f$2 = r3;
                this.f$3 = r4;
            }

            public final void run() {
                WindowManagerProxy.this.lambda$dismissOrMaximizeDocked$0$WindowManagerProxy(this.f$1, this.f$2, this.f$3);
            }
        });
    }

    public void setResizing(final boolean z) {
        this.mExecutor.execute(new Runnable(this) {
            /* class com.android.systemui.stackdivider.WindowManagerProxy.AnonymousClass2 */

            public void run() {
                try {
                    ActivityTaskManager.getService().setSplitScreenResizing(z);
                } catch (RemoteException e) {
                    Log.w("WindowManagerProxy", "Error calling setDockedStackResizing: " + e);
                }
            }
        });
    }

    public void setTouchRegion(Rect rect) {
        synchronized (this.mDockedRect) {
            this.mTouchableRegion.set(rect);
        }
        this.mExecutor.execute(this.mSetTouchableRegionRunnable);
    }

    static void applyResizeSplits(int i, SplitDisplayLayout splitDisplayLayout) {
        WindowContainerTransaction windowContainerTransaction = new WindowContainerTransaction();
        splitDisplayLayout.resizeSplits(i, windowContainerTransaction);
        WindowOrganizer.applyTransaction(windowContainerTransaction);
    }

    private static boolean getHomeAndRecentsTasks(List<ActivityManager.RunningTaskInfo> list, WindowContainerToken windowContainerToken) {
        List list2;
        int[] iArr = HOME_AND_RECENTS;
        if (windowContainerToken == null) {
            list2 = TaskOrganizer.getRootTasks(0, iArr);
        } else {
            list2 = TaskOrganizer.getChildTasks(windowContainerToken, iArr);
        }
        int size = list2.size();
        boolean z = false;
        for (int i = 0; i < size; i++) {
            ActivityManager.RunningTaskInfo runningTaskInfo = (ActivityManager.RunningTaskInfo) list2.get(i);
            list.add(runningTaskInfo);
            if (runningTaskInfo.topActivityType == 2) {
                z = runningTaskInfo.isResizeable;
            }
        }
        return z;
    }

    static boolean applyHomeTasksMinimized(SplitDisplayLayout splitDisplayLayout, WindowContainerToken windowContainerToken, WindowContainerTransaction windowContainerTransaction) {
        Rect rect;
        ArrayList arrayList = new ArrayList();
        boolean homeAndRecentsTasks = getHomeAndRecentsTasks(arrayList, windowContainerToken);
        if (homeAndRecentsTasks) {
            rect = splitDisplayLayout.calcResizableMinimizedHomeStackBounds();
        } else {
            boolean z = false;
            rect = new Rect(0, 0, 0, 0);
            int size = arrayList.size() - 1;
            while (true) {
                if (size < 0) {
                    break;
                } else if (((ActivityManager.RunningTaskInfo) arrayList.get(size)).topActivityType == 2) {
                    int i = ((ActivityManager.RunningTaskInfo) arrayList.get(size)).configuration.orientation;
                    boolean isLandscape = splitDisplayLayout.mDisplayLayout.isLandscape();
                    if (i == 2 || (i == 0 && isLandscape)) {
                        z = true;
                    }
                    rect.right = z == isLandscape ? splitDisplayLayout.mDisplayLayout.width() : splitDisplayLayout.mDisplayLayout.height();
                    rect.bottom = z == isLandscape ? splitDisplayLayout.mDisplayLayout.height() : splitDisplayLayout.mDisplayLayout.width();
                } else {
                    size--;
                }
            }
        }
        for (int size2 = arrayList.size() - 1; size2 >= 0; size2--) {
            if (!homeAndRecentsTasks) {
                if (((ActivityManager.RunningTaskInfo) arrayList.get(size2)).topActivityType != 3) {
                    windowContainerTransaction.setWindowingMode(((ActivityManager.RunningTaskInfo) arrayList.get(size2)).token, 1);
                }
            }
            windowContainerTransaction.setBounds(((ActivityManager.RunningTaskInfo) arrayList.get(size2)).token, rect);
        }
        splitDisplayLayout.mTiles.mHomeBounds.set(rect);
        return homeAndRecentsTasks;
    }

    /* access modifiers changed from: package-private */
    public boolean applyEnterSplit(SplitScreenTaskOrganizer splitScreenTaskOrganizer, SplitDisplayLayout splitDisplayLayout) {
        TaskOrganizer.setLaunchRoot(0, splitScreenTaskOrganizer.mSecondary.token);
        List rootTasks = TaskOrganizer.getRootTasks(0, (int[]) null);
        WindowContainerTransaction windowContainerTransaction = new WindowContainerTransaction();
        if (rootTasks.isEmpty()) {
            return false;
        }
        ActivityManager.RunningTaskInfo runningTaskInfo = null;
        for (int size = rootTasks.size() - 1; size >= 0; size--) {
            ActivityManager.RunningTaskInfo runningTaskInfo2 = (ActivityManager.RunningTaskInfo) rootTasks.get(size);
            if ((runningTaskInfo2.isResizeable || runningTaskInfo2.topActivityType == 2) && (runningTaskInfo2.configuration.windowConfiguration.getWindowingMode() == 1 || runningTaskInfo2.configuration.windowConfiguration.getWindowingMode() == 13)) {
                runningTaskInfo = isHomeOrRecentTask(runningTaskInfo2) ? runningTaskInfo2 : null;
                windowContainerTransaction.reparent(runningTaskInfo2.token, splitScreenTaskOrganizer.mSecondary.token, true);
            }
        }
        windowContainerTransaction.reorder(splitScreenTaskOrganizer.mSecondary.token, true);
        boolean applyHomeTasksMinimized = applyHomeTasksMinimized(splitDisplayLayout, null, windowContainerTransaction);
        if (runningTaskInfo != null) {
            windowContainerTransaction.setBoundsChangeTransaction(runningTaskInfo.token, splitScreenTaskOrganizer.mHomeBounds);
        }
        applySyncTransaction(windowContainerTransaction);
        return applyHomeTasksMinimized;
    }

    static boolean isHomeOrRecentTask(ActivityManager.RunningTaskInfo runningTaskInfo) {
        int activityType = runningTaskInfo.configuration.windowConfiguration.getActivityType();
        return activityType == 2 || activityType == 3;
    }

    /* access modifiers changed from: package-private */
    /* renamed from: applyDismissSplit */
    public void lambda$dismissOrMaximizeDocked$0(SplitScreenTaskOrganizer splitScreenTaskOrganizer, SplitDisplayLayout splitDisplayLayout, boolean z) {
        int i;
        int i2;
        TaskOrganizer.setLaunchRoot(0, (WindowContainerToken) null);
        List childTasks = TaskOrganizer.getChildTasks(splitScreenTaskOrganizer.mPrimary.token, (int[]) null);
        List childTasks2 = TaskOrganizer.getChildTasks(splitScreenTaskOrganizer.mSecondary.token, (int[]) null);
        List rootTasks = TaskOrganizer.getRootTasks(0, HOME_AND_RECENTS);
        rootTasks.removeIf(new Predicate() {
            /* class com.android.systemui.stackdivider.$$Lambda$WindowManagerProxy$a7UL3QZ0yLpefq_HcLMSf4F6Xoo */

            @Override // java.util.function.Predicate
            public final boolean test(Object obj) {
                return WindowManagerProxy.lambda$applyDismissSplit$1(SplitScreenTaskOrganizer.this, (ActivityManager.RunningTaskInfo) obj);
            }
        });
        if (!childTasks.isEmpty() || !childTasks2.isEmpty() || !rootTasks.isEmpty()) {
            WindowContainerTransaction windowContainerTransaction = new WindowContainerTransaction();
            if (z) {
                for (int size = childTasks.size() - 1; size >= 0; size--) {
                    windowContainerTransaction.reparent(((ActivityManager.RunningTaskInfo) childTasks.get(size)).token, (WindowContainerToken) null, true);
                }
                boolean z2 = false;
                for (int size2 = childTasks2.size() - 1; size2 >= 0; size2--) {
                    ActivityManager.RunningTaskInfo runningTaskInfo = (ActivityManager.RunningTaskInfo) childTasks2.get(size2);
                    windowContainerTransaction.reparent(runningTaskInfo.token, (WindowContainerToken) null, true);
                    if (isHomeOrRecentTask(runningTaskInfo)) {
                        windowContainerTransaction.setBounds(runningTaskInfo.token, (Rect) null);
                        windowContainerTransaction.setWindowingMode(runningTaskInfo.token, 0);
                        if (size2 == 0) {
                            z2 = true;
                        }
                    }
                }
                if (z2) {
                    boolean isLandscape = splitDisplayLayout.mDisplayLayout.isLandscape();
                    if (isLandscape) {
                        i = splitDisplayLayout.mSecondary.left - splitScreenTaskOrganizer.mHomeBounds.left;
                    } else {
                        i = splitDisplayLayout.mSecondary.left;
                    }
                    if (isLandscape) {
                        i2 = splitDisplayLayout.mSecondary.top;
                    } else {
                        i2 = splitDisplayLayout.mSecondary.top - splitScreenTaskOrganizer.mHomeBounds.top;
                    }
                    SurfaceControl.Transaction transaction = new SurfaceControl.Transaction();
                    transaction.setPosition(splitScreenTaskOrganizer.mSecondarySurface, (float) i, (float) i2);
                    Rect rect = new Rect(0, 0, splitDisplayLayout.mDisplayLayout.width(), splitDisplayLayout.mDisplayLayout.height());
                    rect.offset(-i, -i2);
                    transaction.setWindowCrop(splitScreenTaskOrganizer.mSecondarySurface, rect);
                    windowContainerTransaction.setBoundsChangeTransaction(splitScreenTaskOrganizer.mSecondary.token, transaction);
                }
            } else {
                for (int size3 = childTasks2.size() - 1; size3 >= 0; size3--) {
                    if (!isHomeOrRecentTask((ActivityManager.RunningTaskInfo) childTasks2.get(size3))) {
                        windowContainerTransaction.reparent(((ActivityManager.RunningTaskInfo) childTasks2.get(size3)).token, (WindowContainerToken) null, true);
                    }
                }
                for (int size4 = childTasks2.size() - 1; size4 >= 0; size4--) {
                    ActivityManager.RunningTaskInfo runningTaskInfo2 = (ActivityManager.RunningTaskInfo) childTasks2.get(size4);
                    if (isHomeOrRecentTask(runningTaskInfo2)) {
                        windowContainerTransaction.reparent(runningTaskInfo2.token, (WindowContainerToken) null, true);
                        windowContainerTransaction.setBounds(runningTaskInfo2.token, (Rect) null);
                        windowContainerTransaction.setWindowingMode(runningTaskInfo2.token, 0);
                    }
                }
                for (int size5 = childTasks.size() - 1; size5 >= 0; size5--) {
                    windowContainerTransaction.reparent(((ActivityManager.RunningTaskInfo) childTasks.get(size5)).token, (WindowContainerToken) null, true);
                }
            }
            for (int size6 = rootTasks.size() - 1; size6 >= 0; size6--) {
                windowContainerTransaction.setBounds(((ActivityManager.RunningTaskInfo) rootTasks.get(size6)).token, (Rect) null);
                windowContainerTransaction.setWindowingMode(((ActivityManager.RunningTaskInfo) rootTasks.get(size6)).token, 0);
            }
            windowContainerTransaction.setFocusable(splitScreenTaskOrganizer.mPrimary.token, true);
            applySyncTransaction(windowContainerTransaction);
        }
    }

    static /* synthetic */ boolean lambda$applyDismissSplit$1(SplitScreenTaskOrganizer splitScreenTaskOrganizer, ActivityManager.RunningTaskInfo runningTaskInfo) {
        return runningTaskInfo.token.equals(splitScreenTaskOrganizer.mSecondary.token) || runningTaskInfo.token.equals(splitScreenTaskOrganizer.mPrimary.token);
    }

    /* access modifiers changed from: package-private */
    public void applySyncTransaction(WindowContainerTransaction windowContainerTransaction) {
        this.mSyncTransactionQueue.queue(windowContainerTransaction);
    }

    /* access modifiers changed from: package-private */
    public boolean queueSyncTransactionIfWaiting(WindowContainerTransaction windowContainerTransaction) {
        return this.mSyncTransactionQueue.queueIfWaiting(windowContainerTransaction);
    }

    /* access modifiers changed from: package-private */
    public void runInSync(SyncTransactionQueue.TransactionRunnable transactionRunnable) {
        this.mSyncTransactionQueue.runInSync(transactionRunnable);
    }
}
