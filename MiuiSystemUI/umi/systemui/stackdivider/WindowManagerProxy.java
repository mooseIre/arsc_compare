package com.android.systemui.stackdivider;

import android.app.ActivityManager;
import android.app.ActivityTaskManager;
import android.graphics.Rect;
import android.os.RemoteException;
import android.util.Log;
import android.view.WindowManagerGlobal;
import android.window.TaskOrganizer;
import android.window.WindowContainerToken;
import android.window.WindowContainerTransaction;
import android.window.WindowOrganizer;
import com.android.internal.annotations.GuardedBy;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class WindowManagerProxy {
    private static final int[] HOME_AND_RECENTS = {2, 3};
    private static final WindowManagerProxy sInstance = new WindowManagerProxy();
    /* access modifiers changed from: private */
    @GuardedBy({"mDockedRect"})
    public final Rect mDockedRect = new Rect();
    private final ExecutorService mExecutor = Executors.newSingleThreadExecutor();
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
    /* access modifiers changed from: private */
    public final Rect mTmpRect1 = new Rect();
    /* access modifiers changed from: private */
    @GuardedBy({"mDockedRect"})
    public final Rect mTouchableRegion = new Rect();

    public void maximizeDockedStack() {
    }

    private WindowManagerProxy() {
    }

    public static WindowManagerProxy getInstance() {
        return sInstance;
    }

    /* access modifiers changed from: package-private */
    public void dismissOrMaximizeDocked(SplitScreenTaskOrganizer splitScreenTaskOrganizer, boolean z) {
        this.mExecutor.execute(new Runnable(z) {
            public final /* synthetic */ boolean f$1;

            {
                this.f$1 = r2;
            }

            public final void run() {
                WindowManagerProxy.applyDismissSplit(SplitScreenTaskOrganizer.this, this.f$1);
            }
        });
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

    private static boolean getHomeAndRecentsTasks(List<WindowContainerToken> list, WindowContainerToken windowContainerToken) {
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
            list.add(runningTaskInfo.token);
            if (runningTaskInfo.topActivityType == 2) {
                z = Utils.isResizable(runningTaskInfo);
            }
        }
        return z;
    }

    static boolean applyHomeTasksMinimized(SplitDisplayLayout splitDisplayLayout, WindowContainerToken windowContainerToken, WindowContainerTransaction windowContainerTransaction) {
        Rect rect;
        ArrayList arrayList = new ArrayList();
        boolean homeAndRecentsTasks = getHomeAndRecentsTasks(arrayList, windowContainerToken);
        if (homeAndRecentsTasks) {
            rect = splitDisplayLayout.calcMinimizedHomeStackBounds();
        } else {
            rect = new Rect(0, 0, splitDisplayLayout.mDisplayLayout.width(), splitDisplayLayout.mDisplayLayout.height());
        }
        for (int size = arrayList.size() - 1; size >= 0; size--) {
            windowContainerTransaction.setBounds((WindowContainerToken) arrayList.get(size), rect);
        }
        splitDisplayLayout.mTiles.mHomeBounds.set(rect);
        return homeAndRecentsTasks;
    }

    static boolean applyEnterSplit(SplitScreenTaskOrganizer splitScreenTaskOrganizer, SplitDisplayLayout splitDisplayLayout) {
        TaskOrganizer.setLaunchRoot(0, splitScreenTaskOrganizer.mSecondary.token);
        List rootTasks = TaskOrganizer.getRootTasks(0, (int[]) null);
        WindowContainerTransaction windowContainerTransaction = new WindowContainerTransaction();
        if (rootTasks.isEmpty()) {
            return false;
        }
        for (int size = rootTasks.size() - 1; size >= 0; size--) {
            ActivityManager.RunningTaskInfo runningTaskInfo = (ActivityManager.RunningTaskInfo) rootTasks.get(size);
            if (Utils.isResizable(runningTaskInfo) && runningTaskInfo.configuration.windowConfiguration.getWindowingMode() == 1) {
                windowContainerTransaction.reparent(runningTaskInfo.token, splitScreenTaskOrganizer.mSecondary.token, true);
            }
        }
        windowContainerTransaction.reorder(splitScreenTaskOrganizer.mSecondary.token, true);
        boolean applyHomeTasksMinimized = applyHomeTasksMinimized(splitDisplayLayout, (WindowContainerToken) null, windowContainerTransaction);
        WindowOrganizer.applyTransaction(windowContainerTransaction);
        return applyHomeTasksMinimized;
    }

    private static boolean isHomeOrRecentTask(ActivityManager.RunningTaskInfo runningTaskInfo) {
        int activityType = runningTaskInfo.configuration.windowConfiguration.getActivityType();
        return activityType == 2 || activityType == 3;
    }

    /* access modifiers changed from: package-private */
    public static void applyDismissSplit(SplitScreenTaskOrganizer splitScreenTaskOrganizer, boolean z) {
        TaskOrganizer.setLaunchRoot(0, (WindowContainerToken) null);
        List childTasks = TaskOrganizer.getChildTasks(splitScreenTaskOrganizer.mPrimary.token, (int[]) null);
        List childTasks2 = TaskOrganizer.getChildTasks(splitScreenTaskOrganizer.mSecondary.token, (int[]) null);
        List rootTasks = TaskOrganizer.getRootTasks(0, HOME_AND_RECENTS);
        if (!childTasks.isEmpty() || !childTasks2.isEmpty() || !rootTasks.isEmpty()) {
            WindowContainerTransaction windowContainerTransaction = new WindowContainerTransaction();
            if (z) {
                for (int size = childTasks.size() - 1; size >= 0; size--) {
                    windowContainerTransaction.reparent(((ActivityManager.RunningTaskInfo) childTasks.get(size)).token, (WindowContainerToken) null, true);
                }
                for (int size2 = childTasks2.size() - 1; size2 >= 0; size2--) {
                    ActivityManager.RunningTaskInfo runningTaskInfo = (ActivityManager.RunningTaskInfo) childTasks2.get(size2);
                    windowContainerTransaction.reparent(runningTaskInfo.token, (WindowContainerToken) null, true);
                    if (isHomeOrRecentTask(runningTaskInfo)) {
                        windowContainerTransaction.setBounds(runningTaskInfo.token, (Rect) null);
                    }
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
                    }
                }
                for (int size5 = childTasks.size() - 1; size5 >= 0; size5--) {
                    windowContainerTransaction.reparent(((ActivityManager.RunningTaskInfo) childTasks.get(size5)).token, (WindowContainerToken) null, true);
                }
            }
            for (int size6 = rootTasks.size() - 1; size6 >= 0; size6--) {
                windowContainerTransaction.setBounds(((ActivityManager.RunningTaskInfo) rootTasks.get(size6)).token, (Rect) null);
            }
            windowContainerTransaction.setFocusable(splitScreenTaskOrganizer.mPrimary.token, true);
            WindowOrganizer.applyTransaction(windowContainerTransaction);
        }
    }
}
