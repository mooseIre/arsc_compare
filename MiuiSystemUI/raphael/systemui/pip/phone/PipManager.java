package com.android.systemui.pip.phone;

import android.app.ActivityManager;
import android.app.ActivityManagerCompat;
import android.app.IActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.ParceledListSlice;
import android.content.res.Configuration;
import android.graphics.Rect;
import android.os.Handler;
import android.os.RemoteException;
import android.util.Log;
import android.view.IPinnedStackController;
import android.view.IPinnedStackListener;
import android.view.IWindowManager;
import android.view.WindowManagerGlobal;
import com.android.systemui.pip.BasePipManager;
import com.android.systemui.pip.phone.PipManager;
import com.android.systemui.recents.events.RecentsEventBus;
import com.android.systemui.recents.events.component.ExpandPipEvent;
import com.android.systemui.recents.misc.SystemServicesProxy;
import java.io.PrintWriter;
import java.util.Objects;

public class PipManager implements BasePipManager {
    private static PipManager sPipController;
    /* access modifiers changed from: private */
    public IActivityManager mActivityManager;
    /* access modifiers changed from: private */
    public Context mContext;
    /* access modifiers changed from: private */
    public Handler mHandler = new Handler();
    private InputConsumerController mInputConsumerController;
    /* access modifiers changed from: private */
    public PipMediaController mMediaController;
    /* access modifiers changed from: private */
    public PipMenuActivityController mMenuController;
    /* access modifiers changed from: private */
    public PipNotificationController mNotificationController;
    private final PinnedStackListener mPinnedStackListener = new PinnedStackListener();
    SystemServicesProxy.TaskStackListener mTaskStackListener = new SystemServicesProxy.TaskStackListener() {
        public void onActivityPinned(String str, int i, int i2) {
            if (checkCurrentUserId(PipManager.this.mContext, false)) {
                PipManager.this.mTouchHandler.onActivityPinned();
                PipManager.this.mMediaController.onActivityPinned();
                PipManager.this.mMenuController.onActivityPinned();
                PipManager.this.mNotificationController.onActivityPinned(str, true);
                SystemServicesProxy.getInstance(PipManager.this.mContext).setPipVisibility(true);
            }
        }

        public void onActivityUnpinned() {
            boolean z = false;
            if (checkCurrentUserId(PipManager.this.mContext, false)) {
                ComponentName topPinnedActivity = PipUtils.getTopPinnedActivity(PipManager.this.mContext, PipManager.this.mActivityManager);
                PipManager.this.mMenuController.hideMenu();
                PipManager.this.mNotificationController.onActivityUnpinned(topPinnedActivity);
                SystemServicesProxy instance = SystemServicesProxy.getInstance(PipManager.this.mContext);
                if (topPinnedActivity != null) {
                    z = true;
                }
                instance.setPipVisibility(z);
            }
        }

        public void onPinnedStackAnimationStarted() {
            PipManager.this.mTouchHandler.setTouchEnabled(false);
        }

        public void onPinnedStackAnimationEnded() {
            PipManager.this.mTouchHandler.setTouchEnabled(true);
            PipManager.this.mTouchHandler.onPinnedStackAnimationEnded();
            PipManager.this.mMenuController.onPinnedStackAnimationEnded();
            PipManager.this.mNotificationController.onPinnedStackAnimationEnded();
        }

        public void onPinnedActivityRestartAttempt(boolean z) {
            if (checkCurrentUserId(PipManager.this.mContext, false)) {
                PipManager.this.mTouchHandler.getMotionHelper().expandPip(z);
            }
        }
    };
    /* access modifiers changed from: private */
    public PipTouchHandler mTouchHandler;
    private IWindowManager mWindowManager;

    private class PinnedStackListener extends IPinnedStackListener.Stub {
        public void onShelfVisibilityChanged(boolean z, int i) {
        }

        private PinnedStackListener() {
        }

        public void onListenerRegistered(IPinnedStackController iPinnedStackController) {
            PipManager.this.mHandler.post(new Runnable(iPinnedStackController) {
                private final /* synthetic */ IPinnedStackController f$1;

                {
                    this.f$1 = r2;
                }

                public final void run() {
                    PipManager.PinnedStackListener.this.lambda$onListenerRegistered$0$PipManager$PinnedStackListener(this.f$1);
                }
            });
        }

        public /* synthetic */ void lambda$onListenerRegistered$0$PipManager$PinnedStackListener(IPinnedStackController iPinnedStackController) {
            PipManager.this.mTouchHandler.setPinnedStackController(iPinnedStackController);
        }

        public void onImeVisibilityChanged(boolean z, int i) {
            PipManager.this.mHandler.post(new Runnable(z, i) {
                private final /* synthetic */ boolean f$1;
                private final /* synthetic */ int f$2;

                {
                    this.f$1 = r2;
                    this.f$2 = r3;
                }

                public final void run() {
                    PipManager.PinnedStackListener.this.lambda$onImeVisibilityChanged$1$PipManager$PinnedStackListener(this.f$1, this.f$2);
                }
            });
        }

        public /* synthetic */ void lambda$onImeVisibilityChanged$1$PipManager$PinnedStackListener(boolean z, int i) {
            PipManager.this.mTouchHandler.onImeVisibilityChanged(z, i);
        }

        public void onMinimizedStateChanged(boolean z) {
            PipManager.this.mHandler.post(new Runnable(z) {
                private final /* synthetic */ boolean f$1;

                {
                    this.f$1 = r2;
                }

                public final void run() {
                    PipManager.PinnedStackListener.this.lambda$onMinimizedStateChanged$2$PipManager$PinnedStackListener(this.f$1);
                }
            });
        }

        public /* synthetic */ void lambda$onMinimizedStateChanged$2$PipManager$PinnedStackListener(boolean z) {
            PipManager.this.mTouchHandler.setMinimizedState(z, true);
        }

        public void onMovementBoundsChanged(Rect rect, Rect rect2, Rect rect3, boolean z, int i) {
            PipManager.this.mHandler.post(new Runnable(rect, rect2, rect3, z, i) {
                private final /* synthetic */ Rect f$1;
                private final /* synthetic */ Rect f$2;
                private final /* synthetic */ Rect f$3;
                private final /* synthetic */ boolean f$4;
                private final /* synthetic */ int f$5;

                {
                    this.f$1 = r2;
                    this.f$2 = r3;
                    this.f$3 = r4;
                    this.f$4 = r5;
                    this.f$5 = r6;
                }

                public final void run() {
                    PipManager.PinnedStackListener.this.lambda$onMovementBoundsChanged$3$PipManager$PinnedStackListener(this.f$1, this.f$2, this.f$3, this.f$4, this.f$5);
                }
            });
        }

        public /* synthetic */ void lambda$onMovementBoundsChanged$3$PipManager$PinnedStackListener(Rect rect, Rect rect2, Rect rect3, boolean z, int i) {
            PipManager.this.mTouchHandler.onMovementBoundsChanged(rect, rect2, rect3, z, i);
        }

        public void onMovementBoundsChanged(Rect rect, Rect rect2, Rect rect3, boolean z, boolean z2, int i) {
            onMovementBoundsChanged(rect, rect2, rect3, z, i);
        }

        public void onActionsChanged(ParceledListSlice parceledListSlice) {
            PipManager.this.mHandler.post(new Runnable(parceledListSlice) {
                private final /* synthetic */ ParceledListSlice f$1;

                {
                    this.f$1 = r2;
                }

                public final void run() {
                    PipManager.PinnedStackListener.this.lambda$onActionsChanged$4$PipManager$PinnedStackListener(this.f$1);
                }
            });
        }

        public /* synthetic */ void lambda$onActionsChanged$4$PipManager$PinnedStackListener(ParceledListSlice parceledListSlice) {
            PipManager.this.mMenuController.setAppActions(parceledListSlice);
        }
    }

    private PipManager() {
    }

    public void initialize(Context context) {
        this.mContext = context;
        this.mActivityManager = ActivityManagerCompat.getService();
        this.mWindowManager = WindowManagerGlobal.getWindowManagerService();
        try {
            this.mWindowManager.registerPinnedStackListener(0, this.mPinnedStackListener);
        } catch (RemoteException e) {
            Log.e("PipManager", "Failed to register pinned stack listener", e);
        }
        SystemServicesProxy.getInstance(this.mContext).registerTaskStackListener(this.mTaskStackListener);
        this.mInputConsumerController = new InputConsumerController(this.mWindowManager);
        this.mMediaController = new PipMediaController(context, this.mActivityManager);
        this.mMenuController = new PipMenuActivityController(context, this.mActivityManager, this.mMediaController, this.mInputConsumerController);
        this.mTouchHandler = new PipTouchHandler(context, this.mActivityManager, this.mMenuController, this.mInputConsumerController);
        this.mNotificationController = new PipNotificationController(context, this.mActivityManager, this.mTouchHandler.getMotionHelper());
        RecentsEventBus.getDefault().register(this);
    }

    public void onConfigurationChanged(Configuration configuration) {
        this.mTouchHandler.onConfigurationChanged();
    }

    public final void onBusEvent(ExpandPipEvent expandPipEvent) {
        Objects.requireNonNull(expandPipEvent);
        try {
            ActivityManager.StackInfo stackInfo = ActivityManagerCompat.getStackInfo(4, 2, 0);
            if (!(stackInfo == null || stackInfo.taskIds == null)) {
                SystemServicesProxy instance = SystemServicesProxy.getInstance(this.mContext);
                for (int cancelThumbnailTransition : stackInfo.taskIds) {
                    instance.cancelThumbnailTransition(cancelThumbnailTransition);
                }
            }
        } catch (Exception unused) {
        }
        this.mTouchHandler.getMotionHelper().expandPip(false);
    }

    public void showPictureInPictureMenu() {
        this.mTouchHandler.showPictureInPictureMenu();
    }

    public static PipManager getInstance() {
        if (sPipController == null) {
            sPipController = new PipManager();
        }
        return sPipController;
    }

    public void dump(PrintWriter printWriter) {
        printWriter.println("PipManager");
        this.mInputConsumerController.dump(printWriter, "  ");
        this.mMenuController.dump(printWriter, "  ");
        this.mTouchHandler.dump(printWriter, "  ");
    }
}
