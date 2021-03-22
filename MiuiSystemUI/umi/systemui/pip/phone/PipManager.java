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
import com.android.systemui.pip.PipUiEventLogger;
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
    private IActivityManager mActivityManager;
    private PipAppOpsListener mAppOpsListener;
    private Context mContext;
    private DisplayController.OnDisplaysChangedListener mFixedRotationListener = new DisplayController.OnDisplaysChangedListener() {
        /* class com.android.systemui.pip.phone.PipManager.AnonymousClass1 */

        @Override // com.android.systemui.wm.DisplayController.OnDisplaysChangedListener
        public void onFixedRotationStarted(int i, int i2) {
            PipManager.this.mIsInFixedRotation = true;
        }

        @Override // com.android.systemui.wm.DisplayController.OnDisplaysChangedListener
        public void onFixedRotationFinished(int i) {
            PipManager.this.mIsInFixedRotation = false;
        }
    };
    private Handler mHandler = new Handler();
    private InputConsumerController mInputConsumerController;
    private boolean mIsInFixedRotation;
    private PipMediaController mMediaController;
    protected PipMenuActivityController mMenuController;
    private IPinnedStackAnimationListener mPinnedStackAnimationRecentsListener;
    private PipBoundsHandler mPipBoundsHandler;
    private PipTaskOrganizer mPipTaskOrganizer;
    private final Rect mReentryBounds = new Rect();
    private final DisplayChangeController.OnDisplayChangingListener mRotationController = new DisplayChangeController.OnDisplayChangingListener() {
        /* class com.android.systemui.pip.phone.$$Lambda$PipManager$AYejaSf14FPjo5Gs0gXzuHGoWo */

        @Override // com.android.systemui.wm.DisplayChangeController.OnDisplayChangingListener
        public final void onRotateDisplay(int i, int i2, int i3, WindowContainerTransaction windowContainerTransaction) {
            PipManager.this.lambda$new$0$PipManager(i, i2, i3, windowContainerTransaction);
        }
    };
    private final TaskStackChangeListener mTaskStackListener = new TaskStackChangeListener() {
        /* class com.android.systemui.pip.phone.PipManager.AnonymousClass2 */

        @Override // com.android.systemui.shared.system.TaskStackChangeListener
        public void onActivityPinned(String str, int i, int i2, int i3) {
            PipManager.this.mTouchHandler.onActivityPinned();
            PipManager.this.mMediaController.onActivityPinned();
            PipManager.this.mMenuController.onActivityPinned();
            PipManager.this.mAppOpsListener.onActivityPinned(str);
            ((UiOffloadThread) Dependency.get(UiOffloadThread.class)).execute($$Lambda$PipManager$2$kFSpUf2kEc9cokMmjrww09bE40o.INSTANCE);
        }

        @Override // com.android.systemui.shared.system.TaskStackChangeListener
        public void onActivityUnpinned() {
            ComponentName componentName = (ComponentName) PipUtils.getTopPipActivity(PipManager.this.mContext, PipManager.this.mActivityManager).first;
            PipManager.this.mMenuController.onActivityUnpinned();
            PipManager.this.mTouchHandler.onActivityUnpinned(componentName);
            PipManager.this.mAppOpsListener.onActivityUnpinned();
            ((UiOffloadThread) Dependency.get(UiOffloadThread.class)).execute(new Runnable(componentName) {
                /* class com.android.systemui.pip.phone.$$Lambda$PipManager$2$7wUFTc4hTjTQbo3BPlrl8V6q3tU */
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

        @Override // com.android.systemui.shared.system.TaskStackChangeListener
        public void onActivityRestartAttempt(ActivityManager.RunningTaskInfo runningTaskInfo, boolean z, boolean z2, boolean z3) {
            if (runningTaskInfo.configuration.windowConfiguration.getWindowingMode() == 2) {
                PipManager.this.mTouchHandler.getMotionHelper().expandPipToFullscreen(z2);
            }
        }
    };
    private final DisplayInfo mTmpDisplayInfo = new DisplayInfo();
    private final Rect mTmpInsetBounds = new Rect();
    private final Rect mTmpNormalBounds = new Rect();
    private PipTouchHandler mTouchHandler;

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

    /* access modifiers changed from: private */
    public class PipManagerPinnedStackListener extends PinnedStackListenerForwarder.PinnedStackListener {
        private PipManagerPinnedStackListener() {
        }

        /* access modifiers changed from: private */
        /* renamed from: lambda$onListenerRegistered$0 */
        public /* synthetic */ void lambda$onListenerRegistered$0$PipManager$PipManagerPinnedStackListener(IPinnedStackController iPinnedStackController) {
            PipManager.this.mTouchHandler.setPinnedStackController(iPinnedStackController);
        }

        @Override // com.android.systemui.shared.system.PinnedStackListenerForwarder.PinnedStackListener
        public void onListenerRegistered(IPinnedStackController iPinnedStackController) {
            PipManager.this.mHandler.post(new Runnable(iPinnedStackController) {
                /* class com.android.systemui.pip.phone.$$Lambda$PipManager$PipManagerPinnedStackListener$MKnIqOfkRku5KYToXZ0DmcZA */
                public final /* synthetic */ IPinnedStackController f$1;

                {
                    this.f$1 = r2;
                }

                public final void run() {
                    PipManager.PipManagerPinnedStackListener.this.lambda$onListenerRegistered$0$PipManager$PipManagerPinnedStackListener(this.f$1);
                }
            });
        }

        @Override // com.android.systemui.shared.system.PinnedStackListenerForwarder.PinnedStackListener
        public void onImeVisibilityChanged(boolean z, int i) {
            PipManager.this.mHandler.post(new Runnable(z, i) {
                /* class com.android.systemui.pip.phone.$$Lambda$PipManager$PipManagerPinnedStackListener$u1KCCoxakH7gZKPv7iZK4aLn7MU */
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
            PipManager.this.updateMovementBounds(null, false, z, false, null);
        }

        @Override // com.android.systemui.shared.system.PinnedStackListenerForwarder.PinnedStackListener
        public void onMovementBoundsChanged(boolean z) {
            PipManager.this.mHandler.post(new Runnable(z) {
                /* class com.android.systemui.pip.phone.$$Lambda$PipManager$PipManagerPinnedStackListener$fSQ0yydZH1ZP4stoL54CdBZ7QU */
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

        @Override // com.android.systemui.shared.system.PinnedStackListenerForwarder.PinnedStackListener
        public void onActionsChanged(ParceledListSlice parceledListSlice) {
            PipManager.this.mHandler.post(new Runnable(parceledListSlice) {
                /* class com.android.systemui.pip.phone.$$Lambda$PipManager$PipManagerPinnedStackListener$w3TtXQNx6JYy0rkssM6SOCMIiCQ */
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

        @Override // com.android.systemui.shared.system.PinnedStackListenerForwarder.PinnedStackListener
        public void onActivityHidden(ComponentName componentName) {
            PipManager.this.mHandler.post(new Runnable(componentName) {
                /* class com.android.systemui.pip.phone.$$Lambda$PipManager$PipManagerPinnedStackListener$jzbSRhWFoxplnSPY2RgqZCPd1ts */
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

        @Override // com.android.systemui.shared.system.PinnedStackListenerForwarder.PinnedStackListener
        public void onDisplayInfoChanged(DisplayInfo displayInfo) {
            PipManager.this.mHandler.post(new Runnable(displayInfo) {
                /* class com.android.systemui.pip.phone.$$Lambda$PipManager$PipManagerPinnedStackListener$P0_Ji3WptNFaEdrasIn3ZLSvnUM */
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

        @Override // com.android.systemui.shared.system.PinnedStackListenerForwarder.PinnedStackListener
        public void onConfigurationChanged() {
            PipManager.this.mHandler.post(new Runnable() {
                /* class com.android.systemui.pip.phone.$$Lambda$PipManager$PipManagerPinnedStackListener$_tnyP4cjZoY1aQdH46PDBhGhzVU */

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

        @Override // com.android.systemui.shared.system.PinnedStackListenerForwarder.PinnedStackListener
        public void onAspectRatioChanged(float f) {
            PipManager.this.mHandler.post(new Runnable(f) {
                /* class com.android.systemui.pip.phone.$$Lambda$PipManager$PipManagerPinnedStackListener$as1Gj0OwAKB_hvEWCKYdRYRFM9g */
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

    public PipManager(Context context, BroadcastDispatcher broadcastDispatcher, DisplayController displayController, FloatingContentCoordinator floatingContentCoordinator, DeviceConfigProxy deviceConfigProxy, PipBoundsHandler pipBoundsHandler, PipSnapAlgorithm pipSnapAlgorithm, PipTaskOrganizer pipTaskOrganizer, SysUiState sysUiState, PipUiEventLogger pipUiEventLogger) {
        this.mContext = context;
        this.mActivityManager = ActivityManager.getService();
        try {
            WindowManagerWrapper.getInstance().addPinnedStackListener(new PipManagerPinnedStackListener());
        } catch (RemoteException e) {
            Log.e("PipManager", "Failed to register pinned stack listener", e);
        }
        ActivityManagerWrapper.getInstance().registerTaskStackListener(this.mTaskStackListener);
        this.mPipBoundsHandler = pipBoundsHandler;
        this.mPipTaskOrganizer = pipTaskOrganizer;
        pipTaskOrganizer.registerPipTransitionCallback(this);
        this.mInputConsumerController = InputConsumerController.getPipInputConsumer();
        PipMediaController pipMediaController = new PipMediaController(context, this.mActivityManager, broadcastDispatcher);
        this.mMediaController = pipMediaController;
        PipMenuActivityController pipMenuActivityController = new PipMenuActivityController(context, pipMediaController, this.mInputConsumerController);
        this.mMenuController = pipMenuActivityController;
        this.mTouchHandler = new PipTouchHandler(context, this.mActivityManager, pipMenuActivityController, this.mInputConsumerController, this.mPipBoundsHandler, this.mPipTaskOrganizer, floatingContentCoordinator, deviceConfigProxy, pipSnapAlgorithm, sysUiState, pipUiEventLogger);
        this.mAppOpsListener = new PipAppOpsListener(context, this.mActivityManager, this.mTouchHandler.getMotionHelper());
        displayController.addDisplayChangingController(this.mRotationController);
        displayController.addDisplayWindowListener(this.mFixedRotationListener);
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

    @Override // com.android.systemui.pip.BasePipManager
    public void onConfigurationChanged(Configuration configuration) {
        this.mTouchHandler.onConfigurationChanged();
    }

    @Override // com.android.systemui.pip.BasePipManager
    public void showPictureInPictureMenu() {
        this.mTouchHandler.showPictureInPictureMenu();
    }

    @Override // com.android.systemui.pip.BasePipManager
    public void setShelfHeight(boolean z, int i) {
        this.mHandler.post(new Runnable(z, i) {
            /* class com.android.systemui.pip.phone.$$Lambda$PipManager$f_jRwYFIWoME7ctXwrfhuNp1q0 */
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
            updateMovementBounds(this.mPipTaskOrganizer.getLastReportedBounds(), false, false, true, null);
        }
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$setPinnedStackAnimationType$2 */
    public /* synthetic */ void lambda$setPinnedStackAnimationType$2$PipManager(int i) {
        this.mPipTaskOrganizer.setOneShotAnimationType(i);
    }

    @Override // com.android.systemui.pip.BasePipManager
    public void setPinnedStackAnimationType(int i) {
        this.mHandler.post(new Runnable(i) {
            /* class com.android.systemui.pip.phone.$$Lambda$PipManager$_erwmkZE5c2eLc8r_OSTlUw7erk */
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

    @Override // com.android.systemui.pip.BasePipManager
    public void setPinnedStackAnimationListener(IPinnedStackAnimationListener iPinnedStackAnimationListener) {
        this.mHandler.post(new Runnable(iPinnedStackAnimationListener) {
            /* class com.android.systemui.pip.phone.$$Lambda$PipManager$t2XWznriuk4XHpM7EiG9uJamHUY */
            public final /* synthetic */ IPinnedStackAnimationListener f$1;

            {
                this.f$1 = r2;
            }

            public final void run() {
                PipManager.this.lambda$setPinnedStackAnimationListener$3$PipManager(this.f$1);
            }
        });
    }

    @Override // com.android.systemui.pip.PipTaskOrganizer.PipTransitionCallback
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

    @Override // com.android.systemui.pip.PipTaskOrganizer.PipTransitionCallback
    public void onPipTransitionFinished(ComponentName componentName, int i) {
        onPipTransitionFinishedOrCanceled(i);
    }

    @Override // com.android.systemui.pip.PipTaskOrganizer.PipTransitionCallback
    public void onPipTransitionCanceled(ComponentName componentName, int i) {
        onPipTransitionFinishedOrCanceled(i);
    }

    private void onPipTransitionFinishedOrCanceled(int i) {
        this.mTouchHandler.setTouchEnabled(true);
        this.mTouchHandler.onPinnedStackAnimationEnded(i);
        this.mMenuController.onPinnedStackAnimationEnded();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateMovementBounds(Rect rect, boolean z, boolean z2, boolean z3, WindowContainerTransaction windowContainerTransaction) {
        Rect rect2 = new Rect(rect);
        this.mPipBoundsHandler.onMovementBoundsChanged(this.mTmpInsetBounds, this.mTmpNormalBounds, rect2, this.mTmpDisplayInfo);
        this.mPipTaskOrganizer.onMovementBoundsChanged(rect2, z, z2, z3, windowContainerTransaction);
        this.mTouchHandler.onMovementBoundsChanged(this.mTmpInsetBounds, this.mTmpNormalBounds, rect2, z2, z3, this.mTmpDisplayInfo.rotation);
    }

    @Override // com.android.systemui.pip.BasePipManager
    public void dump(PrintWriter printWriter) {
        printWriter.println("PipManager");
        this.mInputConsumerController.dump(printWriter, "  ");
        this.mMenuController.dump(printWriter, "  ");
        this.mTouchHandler.dump(printWriter, "  ");
        this.mPipBoundsHandler.dump(printWriter, "  ");
        this.mPipTaskOrganizer.dump(printWriter, "  ");
    }
}
