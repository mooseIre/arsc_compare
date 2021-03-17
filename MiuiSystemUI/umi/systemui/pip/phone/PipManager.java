package com.android.systemui.pip.phone;

import android.app.ActivityManager;
import android.app.ActivityTaskManager;
import android.app.IActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.ParceledListSlice;
import android.content.res.Configuration;
import android.graphics.Rect;
import android.os.Handler;
import android.os.RemoteException;
import android.util.Log;
import android.view.DisplayInfo;
import android.view.IPinnedStackController;
import android.window.WindowContainerTransaction;
import com.android.systemui.Dependency;
import com.android.systemui.UiOffloadThread;
import com.android.systemui.broadcast.BroadcastDispatcher;
import com.android.systemui.model.SysUiState;
import com.android.systemui.pip.BasePipManager;
import com.android.systemui.pip.PipAnimationController;
import com.android.systemui.pip.PipBoundsHandler;
import com.android.systemui.pip.PipSnapAlgorithm;
import com.android.systemui.pip.PipTaskOrganizer;
import com.android.systemui.pip.phone.PipManager;
import com.android.systemui.shared.recents.IPinnedStackAnimationListener;
import com.android.systemui.shared.system.ActivityManagerWrapper;
import com.android.systemui.shared.system.InputConsumerController;
import com.android.systemui.shared.system.PinnedStackListenerForwarder;
import com.android.systemui.shared.system.TaskStackChangeListener;
import com.android.systemui.shared.system.WindowManagerWrapper;
import com.android.systemui.util.DeviceConfigProxy;
import com.android.systemui.util.FloatingContentCoordinator;
import com.android.systemui.wm.DisplayChangeController;
import com.android.systemui.wm.DisplayController;
import java.io.PrintWriter;

public class PipManager implements BasePipManager, PipTaskOrganizer.PipTransitionCallback {
    /* access modifiers changed from: private */
    public IActivityManager mActivityManager;
    /* access modifiers changed from: private */
    public PipAppOpsListener mAppOpsListener;
    /* access modifiers changed from: private */
    public Context mContext;
    private DisplayController.OnDisplaysChangedListener mFixedRotationListener = new DisplayController.OnDisplaysChangedListener() {
        public void onFixedRotationStarted(int i, int i2) {
            boolean unused = PipManager.this.mIsInFixedRotation = true;
        }

        public void onFixedRotationFinished(int i) {
            boolean unused = PipManager.this.mIsInFixedRotation = false;
        }
    };
    /* access modifiers changed from: private */
    public Handler mHandler = new Handler();
    private InputConsumerController mInputConsumerController;
    /* access modifiers changed from: private */
    public boolean mIsInFixedRotation;
    /* access modifiers changed from: private */
    public PipMediaController mMediaController;
    protected PipMenuActivityController mMenuController;
    private IPinnedStackAnimationListener mPinnedStackAnimationRecentsListener;
    /* access modifiers changed from: private */
    public PipBoundsHandler mPipBoundsHandler;
    private PipTaskOrganizer mPipTaskOrganizer;
    private final Rect mReentryBounds = new Rect();
    private final DisplayChangeController.OnDisplayChangingListener mRotationController = new DisplayChangeController.OnDisplayChangingListener() {
        public final void onRotateDisplay(int i, int i2, int i3, WindowContainerTransaction windowContainerTransaction) {
            PipManager.this.lambda$new$0$PipManager(i, i2, i3, windowContainerTransaction);
        }
    };
    private final TaskStackChangeListener mTaskStackListener = new TaskStackChangeListener() {
        public void onActivityPinned(String str, int i, int i2, int i3) {
            PipManager.this.mTouchHandler.onActivityPinned();
            PipManager.this.mMediaController.onActivityPinned();
            PipManager.this.mMenuController.onActivityPinned();
            PipManager.this.mAppOpsListener.onActivityPinned(str);
            ((UiOffloadThread) Dependency.get(UiOffloadThread.class)).execute($$Lambda$PipManager$2$kFSpUf2kEc9cokMmjrww09bE40o.INSTANCE);
        }

        public void onActivityUnpinned() {
            ComponentName componentName = (ComponentName) PipUtils.getTopPipActivity(PipManager.this.mContext, PipManager.this.mActivityManager).first;
            PipManager.this.mMenuController.onActivityUnpinned();
            PipManager.this.mTouchHandler.onActivityUnpinned(componentName);
            PipManager.this.mAppOpsListener.onActivityUnpinned();
            ((UiOffloadThread) Dependency.get(UiOffloadThread.class)).execute(new Runnable(componentName) {
                public final /* synthetic */ ComponentName f$0;

                {
                    this.f$0 = r1;
                }

                public final void run() {
                    PipManager.AnonymousClass2.lambda$onActivityUnpinned$1(this.f$0);
                }
            });
        }

        static /* synthetic */ void lambda$onActivityUnpinned$1(ComponentName componentName) {
            WindowManagerWrapper.getInstance().setPipVisibility(componentName != null);
        }

        public void onActivityRestartAttempt(ActivityManager.RunningTaskInfo runningTaskInfo, boolean z, boolean z2, boolean z3) {
            if (runningTaskInfo.configuration.windowConfiguration.getWindowingMode() == 2) {
                PipManager.this.mTouchHandler.getMotionHelper().expandPipToFullscreen(z2);
            }
        }
    };
    private final DisplayInfo mTmpDisplayInfo = new DisplayInfo();
    private final Rect mTmpInsetBounds = new Rect();
    private final Rect mTmpNormalBounds = new Rect();
    /* access modifiers changed from: private */
    public PipTouchHandler mTouchHandler;

    /* access modifiers changed from: private */
    /* renamed from: lambda$new$0 */
    public /* synthetic */ void lambda$new$0$PipManager(int i, int i2, int i3, WindowContainerTransaction windowContainerTransaction) {
        if (!this.mPipTaskOrganizer.isInPip() || this.mPipTaskOrganizer.isDeferringEnterPipAnimation()) {
            this.mPipBoundsHandler.onDisplayRotationChangedNotInPip(i3);
            return;
        }
        if (this.mPipBoundsHandler.onDisplayRotationChanged(this.mTmpNormalBounds, this.mPipTaskOrganizer.getCurrentOrAnimatingBounds(), this.mTmpInsetBounds, i, i2, i3, windowContainerTransaction)) {
            this.mTouchHandler.adjustBoundsForRotation(this.mTmpNormalBounds, this.mPipTaskOrganizer.getLastReportedBounds(), this.mTmpInsetBounds);
            if (!this.mIsInFixedRotation) {
                this.mPipBoundsHandler.setShelfHeight(false, 0);
                this.mPipBoundsHandler.onImeVisibilityChanged(false, 0);
                this.mTouchHandler.onShelfVisibilityChanged(false, 0);
                this.mTouchHandler.onImeVisibilityChanged(false, 0);
            }
            updateMovementBounds(this.mTmpNormalBounds, true, false, false, windowContainerTransaction);
        }
    }

    private class PipManagerPinnedStackListener extends PinnedStackListenerForwarder.PinnedStackListener {
        private PipManagerPinnedStackListener() {
        }

        /* access modifiers changed from: private */
        /* renamed from: lambda$onListenerRegistered$0 */
        public /* synthetic */ void lambda$onListenerRegistered$0$PipManager$PipManagerPinnedStackListener(IPinnedStackController iPinnedStackController) {
            PipManager.this.mTouchHandler.setPinnedStackController(iPinnedStackController);
        }

        public void onListenerRegistered(IPinnedStackController iPinnedStackController) {
            PipManager.this.mHandler.post(new Runnable(iPinnedStackController) {
                public final /* synthetic */ IPinnedStackController f$1;

                {
                    this.f$1 = r2;
                }

                public final void run() {
                    PipManager.PipManagerPinnedStackListener.this.lambda$onListenerRegistered$0$PipManager$PipManagerPinnedStackListener(this.f$1);
                }
            });
        }

        public void onImeVisibilityChanged(boolean z, int i) {
            PipManager.this.mHandler.post(new Runnable(z, i) {
                public final /* synthetic */ boolean f$1;
                public final /* synthetic */ int f$2;

                {
                    this.f$1 = r2;
                    this.f$2 = r3;
                }

                public final void run() {
                    PipManager.PipManagerPinnedStackListener.this.lambda$onImeVisibilityChanged$1$PipManager$PipManagerPinnedStackListener(this.f$1, this.f$2);
                }
            });
        }

        /* access modifiers changed from: private */
        /* renamed from: lambda$onImeVisibilityChanged$1 */
        public /* synthetic */ void lambda$onImeVisibilityChanged$1$PipManager$PipManagerPinnedStackListener(boolean z, int i) {
            PipManager.this.mPipBoundsHandler.onImeVisibilityChanged(z, i);
            PipManager.this.mTouchHandler.onImeVisibilityChanged(z, i);
        }

        /* access modifiers changed from: private */
        /* renamed from: lambda$onMovementBoundsChanged$2 */
        public /* synthetic */ void lambda$onMovementBoundsChanged$2$PipManager$PipManagerPinnedStackListener(boolean z) {
            PipManager.this.updateMovementBounds((Rect) null, false, z, false, (WindowContainerTransaction) null);
        }

        public void onMovementBoundsChanged(boolean z) {
            PipManager.this.mHandler.post(new Runnable(z) {
                public final /* synthetic */ boolean f$1;

                {
                    this.f$1 = r2;
                }

                public final void run() {
                    PipManager.PipManagerPinnedStackListener.this.lambda$onMovementBoundsChanged$2$PipManager$PipManagerPinnedStackListener(this.f$1);
                }
            });
        }

        /* access modifiers changed from: private */
        /* renamed from: lambda$onActionsChanged$3 */
        public /* synthetic */ void lambda$onActionsChanged$3$PipManager$PipManagerPinnedStackListener(ParceledListSlice parceledListSlice) {
            PipManager.this.mMenuController.setAppActions(parceledListSlice);
        }

        public void onActionsChanged(ParceledListSlice parceledListSlice) {
            PipManager.this.mHandler.post(new Runnable(parceledListSlice) {
                public final /* synthetic */ ParceledListSlice f$1;

                {
                    this.f$1 = r2;
                }

                public final void run() {
                    PipManager.PipManagerPinnedStackListener.this.lambda$onActionsChanged$3$PipManager$PipManagerPinnedStackListener(this.f$1);
                }
            });
        }

        /* access modifiers changed from: private */
        /* renamed from: lambda$onActivityHidden$4 */
        public /* synthetic */ void lambda$onActivityHidden$4$PipManager$PipManagerPinnedStackListener(ComponentName componentName) {
            PipManager.this.mPipBoundsHandler.onResetReentryBounds(componentName);
        }

        public void onActivityHidden(ComponentName componentName) {
            PipManager.this.mHandler.post(new Runnable(componentName) {
                public final /* synthetic */ ComponentName f$1;

                {
                    this.f$1 = r2;
                }

                public final void run() {
                    PipManager.PipManagerPinnedStackListener.this.lambda$onActivityHidden$4$PipManager$PipManagerPinnedStackListener(this.f$1);
                }
            });
        }

        /* access modifiers changed from: private */
        /* renamed from: lambda$onDisplayInfoChanged$5 */
        public /* synthetic */ void lambda$onDisplayInfoChanged$5$PipManager$PipManagerPinnedStackListener(DisplayInfo displayInfo) {
            PipManager.this.mPipBoundsHandler.onDisplayInfoChanged(displayInfo);
        }

        public void onDisplayInfoChanged(DisplayInfo displayInfo) {
            PipManager.this.mHandler.post(new Runnable(displayInfo) {
                public final /* synthetic */ DisplayInfo f$1;

                {
                    this.f$1 = r2;
                }

                public final void run() {
                    PipManager.PipManagerPinnedStackListener.this.lambda$onDisplayInfoChanged$5$PipManager$PipManagerPinnedStackListener(this.f$1);
                }
            });
        }

        /* access modifiers changed from: private */
        /* renamed from: lambda$onConfigurationChanged$6 */
        public /* synthetic */ void lambda$onConfigurationChanged$6$PipManager$PipManagerPinnedStackListener() {
            PipManager.this.mPipBoundsHandler.onConfigurationChanged();
        }

        public void onConfigurationChanged() {
            PipManager.this.mHandler.post(new Runnable() {
                public final void run() {
                    PipManager.PipManagerPinnedStackListener.this.lambda$onConfigurationChanged$6$PipManager$PipManagerPinnedStackListener();
                }
            });
        }

        /* access modifiers changed from: private */
        /* renamed from: lambda$onAspectRatioChanged$7 */
        public /* synthetic */ void lambda$onAspectRatioChanged$7$PipManager$PipManagerPinnedStackListener(float f) {
            PipManager.this.mPipBoundsHandler.onAspectRatioChanged(f);
        }

        public void onAspectRatioChanged(float f) {
            PipManager.this.mHandler.post(new Runnable(f) {
                public final /* synthetic */ float f$1;

                {
                    this.f$1 = r2;
                }

                public final void run() {
                    PipManager.PipManagerPinnedStackListener.this.lambda$onAspectRatioChanged$7$PipManager$PipManagerPinnedStackListener(this.f$1);
                }
            });
        }
    }

    public PipManager(Context context, BroadcastDispatcher broadcastDispatcher, DisplayController displayController, FloatingContentCoordinator floatingContentCoordinator, DeviceConfigProxy deviceConfigProxy, PipBoundsHandler pipBoundsHandler, PipSnapAlgorithm pipSnapAlgorithm, PipTaskOrganizer pipTaskOrganizer, SysUiState sysUiState) {
        Context context2 = context;
        DisplayController displayController2 = displayController;
        PipTaskOrganizer pipTaskOrganizer2 = pipTaskOrganizer;
        this.mContext = context2;
        this.mActivityManager = ActivityManager.getService();
        try {
            WindowManagerWrapper.getInstance().addPinnedStackListener(new PipManagerPinnedStackListener());
        } catch (RemoteException e) {
            Log.e("PipManager", "Failed to register pinned stack listener", e);
        }
        ActivityManagerWrapper.getInstance().registerTaskStackListener(this.mTaskStackListener);
        this.mPipBoundsHandler = pipBoundsHandler;
        this.mPipTaskOrganizer = pipTaskOrganizer2;
        pipTaskOrganizer2.registerPipTransitionCallback(this);
        this.mInputConsumerController = InputConsumerController.getPipInputConsumer();
        PipMediaController pipMediaController = new PipMediaController(context2, this.mActivityManager, broadcastDispatcher);
        this.mMediaController = pipMediaController;
        PipMenuActivityController pipMenuActivityController = new PipMenuActivityController(context2, pipMediaController, this.mInputConsumerController);
        this.mMenuController = pipMenuActivityController;
        this.mTouchHandler = new PipTouchHandler(context, this.mActivityManager, pipMenuActivityController, this.mInputConsumerController, this.mPipBoundsHandler, this.mPipTaskOrganizer, floatingContentCoordinator, deviceConfigProxy, pipSnapAlgorithm, sysUiState);
        this.mAppOpsListener = new PipAppOpsListener(context2, this.mActivityManager, this.mTouchHandler.getMotionHelper());
        displayController2.addDisplayChangingController(this.mRotationController);
        displayController2.addDisplayWindowListener(this.mFixedRotationListener);
        DisplayInfo displayInfo = new DisplayInfo();
        context.getDisplay().getDisplayInfo(displayInfo);
        this.mPipBoundsHandler.onDisplayInfoChanged(displayInfo);
        try {
            this.mPipTaskOrganizer.registerOrganizer(2);
            if (ActivityTaskManager.getService().getStackInfo(2, 0) != null) {
                this.mInputConsumerController.registerInputConsumer(true);
            }
        } catch (RemoteException | UnsupportedOperationException e2) {
            e2.printStackTrace();
        }
    }

    public void onConfigurationChanged(Configuration configuration) {
        this.mTouchHandler.onConfigurationChanged();
    }

    public void showPictureInPictureMenu() {
        this.mTouchHandler.showPictureInPictureMenu();
    }

    public void setShelfHeight(boolean z, int i) {
        this.mHandler.post(new Runnable(z, i) {
            public final /* synthetic */ boolean f$1;
            public final /* synthetic */ int f$2;

            {
                this.f$1 = r2;
                this.f$2 = r3;
            }

            public final void run() {
                PipManager.this.lambda$setShelfHeight$1$PipManager(this.f$1, this.f$2);
            }
        });
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$setShelfHeight$1 */
    public /* synthetic */ void lambda$setShelfHeight$1$PipManager(boolean z, int i) {
        if (!z) {
            i = 0;
        }
        if (this.mPipBoundsHandler.setShelfHeight(z, i)) {
            this.mTouchHandler.onShelfVisibilityChanged(z, i);
            updateMovementBounds(this.mPipTaskOrganizer.getLastReportedBounds(), false, false, true, (WindowContainerTransaction) null);
        }
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$setPinnedStackAnimationType$2 */
    public /* synthetic */ void lambda$setPinnedStackAnimationType$2$PipManager(int i) {
        this.mPipTaskOrganizer.setOneShotAnimationType(i);
    }

    public void setPinnedStackAnimationType(int i) {
        this.mHandler.post(new Runnable(i) {
            public final /* synthetic */ int f$1;

            {
                this.f$1 = r2;
            }

            public final void run() {
                PipManager.this.lambda$setPinnedStackAnimationType$2$PipManager(this.f$1);
            }
        });
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$setPinnedStackAnimationListener$3 */
    public /* synthetic */ void lambda$setPinnedStackAnimationListener$3$PipManager(IPinnedStackAnimationListener iPinnedStackAnimationListener) {
        this.mPinnedStackAnimationRecentsListener = iPinnedStackAnimationListener;
    }

    public void setPinnedStackAnimationListener(IPinnedStackAnimationListener iPinnedStackAnimationListener) {
        this.mHandler.post(new Runnable(iPinnedStackAnimationListener) {
            public final /* synthetic */ IPinnedStackAnimationListener f$1;

            {
                this.f$1 = r2;
            }

            public final void run() {
                PipManager.this.lambda$setPinnedStackAnimationListener$3$PipManager(this.f$1);
            }
        });
    }

    public void onPipTransitionStarted(ComponentName componentName, int i) {
        if (PipAnimationController.isOutPipDirection(i)) {
            this.mReentryBounds.set(this.mTouchHandler.getNormalBounds());
            this.mPipBoundsHandler.applySnapFraction(this.mReentryBounds, this.mPipBoundsHandler.getSnapFraction(this.mPipTaskOrganizer.getLastReportedBounds()));
            this.mPipBoundsHandler.onSaveReentryBounds(componentName, this.mReentryBounds);
        }
        this.mTouchHandler.setTouchEnabled(false);
        IPinnedStackAnimationListener iPinnedStackAnimationListener = this.mPinnedStackAnimationRecentsListener;
        if (iPinnedStackAnimationListener != null) {
            try {
                iPinnedStackAnimationListener.onPinnedStackAnimationStarted();
            } catch (RemoteException e) {
                Log.e("PipManager", "Failed to callback recents", e);
            }
        }
    }

    public void onPipTransitionFinished(ComponentName componentName, int i) {
        onPipTransitionFinishedOrCanceled(i);
    }

    public void onPipTransitionCanceled(ComponentName componentName, int i) {
        onPipTransitionFinishedOrCanceled(i);
    }

    private void onPipTransitionFinishedOrCanceled(int i) {
        this.mTouchHandler.setTouchEnabled(true);
        this.mTouchHandler.onPinnedStackAnimationEnded(i);
        this.mMenuController.onPinnedStackAnimationEnded();
    }

    /* access modifiers changed from: private */
    public void updateMovementBounds(Rect rect, boolean z, boolean z2, boolean z3, WindowContainerTransaction windowContainerTransaction) {
        Rect rect2 = new Rect(rect);
        this.mPipBoundsHandler.onMovementBoundsChanged(this.mTmpInsetBounds, this.mTmpNormalBounds, rect2, this.mTmpDisplayInfo);
        this.mPipTaskOrganizer.onMovementBoundsChanged(rect2, z, z2, z3, windowContainerTransaction);
        this.mTouchHandler.onMovementBoundsChanged(this.mTmpInsetBounds, this.mTmpNormalBounds, rect2, z2, z3, this.mTmpDisplayInfo.rotation);
    }

    public void dump(PrintWriter printWriter) {
        printWriter.println("PipManager");
        this.mInputConsumerController.dump(printWriter, "  ");
        this.mMenuController.dump(printWriter, "  ");
        this.mTouchHandler.dump(printWriter, "  ");
        this.mPipBoundsHandler.dump(printWriter, "  ");
        this.mPipTaskOrganizer.dump(printWriter, "  ");
    }
}
