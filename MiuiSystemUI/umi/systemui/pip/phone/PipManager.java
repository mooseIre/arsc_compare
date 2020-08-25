package com.android.systemui.pip.phone;

import android.app.ActivityManager;
import android.app.ActivityTaskManager;
import android.app.IActivityManager;
import android.app.IActivityTaskManager;
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
import android.view.WindowManagerGlobal;
import android.window.WindowContainerTransaction;
import com.android.systemui.Dependency;
import com.android.systemui.UiOffloadThread;
import com.android.systemui.pip.BasePipManager;
import com.android.systemui.pip.PipAnimationController;
import com.android.systemui.pip.PipBoundsHandler;
import com.android.systemui.pip.PipSnapAlgorithm;
import com.android.systemui.pip.PipTaskOrganizer;
import com.android.systemui.pip.phone.PinnedStackListenerForwarder;
import com.android.systemui.pip.phone.PipManager;
import com.android.systemui.recents.misc.SystemServicesProxy;
import com.android.systemui.wm.DisplayChangeController;
import com.android.systemui.wm.DisplayController;
import java.io.PrintWriter;

public class PipManager implements BasePipManager, PipTaskOrganizer.PipTransitionCallback {
    private static PipManager sPipController;
    /* access modifiers changed from: private */
    public IActivityManager mActivityManager;
    /* access modifiers changed from: private */
    public PipAppOpsListener mAppOpsListener;
    /* access modifiers changed from: private */
    public Context mContext;
    private DisplayController mDisplayController;
    /* access modifiers changed from: private */
    public Handler mHandler = new Handler();
    private InputConsumerController mInputConsumerController;
    private boolean mIsInFixedRotation;
    /* access modifiers changed from: private */
    public PipMediaController mMediaController;
    protected PipMenuActivityController mMenuController;
    private final PipManagerPinnedStackListener mPinnedStackListener = new PipManagerPinnedStackListener(this, (AnonymousClass1) null);
    private PinnedStackListenerForwarder mPinnedStackListenerForwarder = new PinnedStackListenerForwarder();
    /* access modifiers changed from: private */
    public PipBoundsHandler mPipBoundsHandler;
    public PipTaskOrganizer mPipTaskOrganizer;
    private final Rect mReentryBounds = new Rect();
    private final DisplayChangeController.OnDisplayChangingListener mRotationController = new DisplayChangeController.OnDisplayChangingListener() {
        public final void onRotateDisplay(int i, int i2, int i3, WindowContainerTransaction windowContainerTransaction) {
            PipManager.this.lambda$new$0$PipManager(i, i2, i3, windowContainerTransaction);
        }
    };
    private final SystemServicesProxy.TaskStackListener mTaskStackListener = new SystemServicesProxy.TaskStackListener() {
        public void onActivityPinned(String str, int i, int i2) {
            PipManager.this.mTouchHandler.onActivityPinned();
            PipManager.this.mMediaController.onActivityPinned();
            PipManager.this.mMenuController.onActivityPinned();
            PipManager.this.mAppOpsListener.onActivityPinned(str);
            ((UiOffloadThread) Dependency.get(UiOffloadThread.class)).submit(new Runnable() {
                public final void run() {
                    PipManager.AnonymousClass2.this.lambda$onActivityPinned$0$PipManager$2();
                }
            });
        }

        /* access modifiers changed from: private */
        /* renamed from: lambda$onActivityPinned$0 */
        public /* synthetic */ void lambda$onActivityPinned$0$PipManager$2() {
            SystemServicesProxy.getInstance(PipManager.this.mContext).setPipVisibility(true);
        }

        public void onActivityUnpinned() {
            ComponentName componentName = (ComponentName) PipUtils.getTopPipActivity(PipManager.this.mContext, PipManager.this.mActivityManager).first;
            PipManager.this.mMenuController.onActivityUnpinned();
            PipManager.this.mTouchHandler.onActivityUnpinned(componentName);
            PipManager.this.mAppOpsListener.onActivityUnpinned();
            ((UiOffloadThread) Dependency.get(UiOffloadThread.class)).submit(new Runnable(componentName) {
                public final /* synthetic */ ComponentName f$1;

                {
                    this.f$1 = r2;
                }

                public final void run() {
                    PipManager.AnonymousClass2.this.lambda$onActivityUnpinned$1$PipManager$2(this.f$1);
                }
            });
        }

        /* access modifiers changed from: private */
        /* renamed from: lambda$onActivityUnpinned$1 */
        public /* synthetic */ void lambda$onActivityUnpinned$1$PipManager$2(ComponentName componentName) {
            SystemServicesProxy.getInstance(PipManager.this.mContext).setPipVisibility(componentName != null);
        }

        public void onActivityRestartAttempt(ActivityManager.RunningTaskInfo runningTaskInfo, boolean z, boolean z2, boolean z3) {
            if (z3 && runningTaskInfo.configuration.windowConfiguration.getWindowingMode() == 2) {
                PipManager.this.mTouchHandler.getMotionHelper().expandPipToFullscreen(z2);
            }
        }
    };
    private final DisplayInfo mTmpDisplayInfo = new DisplayInfo();
    private final Rect mTmpInsetBounds = new Rect();
    private final Rect mTmpNormalBounds = new Rect();
    /* access modifiers changed from: private */
    public PipTouchHandler mTouchHandler;

    /* renamed from: com.android.systemui.pip.phone.PipManager$1  reason: invalid class name */
    class AnonymousClass1 implements DisplayController.OnDisplaysChangedListener {
    }

    public static PipManager getInstance(Context context) {
        if (sPipController == null) {
            sPipController = new PipManager(context);
        }
        return sPipController;
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$new$0 */
    public /* synthetic */ void lambda$new$0$PipManager(int i, int i2, int i3, WindowContainerTransaction windowContainerTransaction) {
        if (this.mPipBoundsHandler.onDisplayRotationChanged(this.mTmpNormalBounds, this.mPipTaskOrganizer.getLastReportedBounds(), this.mTmpInsetBounds, i, i2, i3, windowContainerTransaction)) {
            this.mTouchHandler.adjustBoundsForRotation(this.mTmpNormalBounds, this.mPipTaskOrganizer.getLastReportedBounds(), this.mTmpInsetBounds);
            if (!this.mIsInFixedRotation) {
                this.mPipBoundsHandler.setShelfHeight(false, 0);
                this.mPipBoundsHandler.onImeVisibilityChanged(false, 0);
                this.mTouchHandler.onShelfVisibilityChanged(false, 0);
                this.mTouchHandler.onImeVisibilityChanged(false, 0);
            }
            updateMovementBounds(this.mTmpNormalBounds, true, false, false);
        }
    }

    private class PipManagerPinnedStackListener extends PinnedStackListenerForwarder.PinnedStackListener {
        private PipManagerPinnedStackListener() {
        }

        /* synthetic */ PipManagerPinnedStackListener(PipManager pipManager, AnonymousClass1 r2) {
            this();
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
            PipManager.this.updateMovementBounds((Rect) null, false, z, false);
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

    public PipManager(Context context) {
        this.mContext = context;
        this.mActivityManager = ActivityManager.getService();
        try {
            this.mPinnedStackListenerForwarder.addListener(this.mPinnedStackListener);
            WindowManagerGlobal.getWindowManagerService().registerPinnedStackListener(0, this.mPinnedStackListenerForwarder);
        } catch (RemoteException e) {
            Log.e("PipManager", "Failed to register pinned stack listener", e);
        }
        SystemServicesProxy.getInstance(this.mContext).registerTaskStackListener(this.mTaskStackListener);
        IActivityTaskManager service = ActivityTaskManager.getService();
        PipTaskOrganizer pipTaskOrganizer = new PipTaskOrganizer(this.mContext);
        this.mPipTaskOrganizer = pipTaskOrganizer;
        pipTaskOrganizer.registerPipTransitionCallback(this);
        this.mInputConsumerController = InputConsumerController.getPipInputConsumer();
        PipMediaController pipMediaController = new PipMediaController(context, this.mActivityManager);
        this.mMediaController = pipMediaController;
        this.mMenuController = new PipMenuActivityController(context, pipMediaController, this.mInputConsumerController);
        PipBoundsHandler instance = PipBoundsHandler.getInstance(this.mContext);
        this.mPipBoundsHandler = instance;
        this.mTouchHandler = new PipTouchHandler(context, this.mActivityManager, service, this.mMenuController, this.mInputConsumerController, instance, this.mPipTaskOrganizer, PipSnapAlgorithm.getInstance(this.mContext));
        this.mAppOpsListener = new PipAppOpsListener(context, this.mActivityManager, this.mTouchHandler.getMotionHelper());
        DisplayController displayController = new DisplayController(this.mContext);
        this.mDisplayController = displayController;
        displayController.addDisplayChangingController(this.mRotationController);
        DisplayInfo displayInfo = new DisplayInfo();
        context.getDisplay().getDisplayInfo(displayInfo);
        this.mPipBoundsHandler.onDisplayInfoChanged(displayInfo);
        try {
            this.mPipTaskOrganizer.registerOrganizer(2);
            if (service.getStackInfo(2, 0) != null) {
                this.mInputConsumerController.registerInputConsumer();
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

    public void onPipTransitionStarted(ComponentName componentName, int i) {
        if (PipAnimationController.isOutPipDirection(i)) {
            this.mReentryBounds.set(this.mTouchHandler.getNormalBounds());
            this.mPipBoundsHandler.applySnapFraction(this.mReentryBounds, this.mPipBoundsHandler.getSnapFraction(this.mPipTaskOrganizer.getLastReportedBounds()));
            this.mPipBoundsHandler.onSaveReentryBounds(componentName, this.mReentryBounds);
        }
        this.mTouchHandler.setTouchEnabled(false);
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
    public void updateMovementBounds(Rect rect, boolean z, boolean z2, boolean z3) {
        Rect rect2 = new Rect(rect);
        this.mPipBoundsHandler.onMovementBoundsChanged(this.mTmpInsetBounds, this.mTmpNormalBounds, rect2, this.mTmpDisplayInfo);
        this.mPipTaskOrganizer.onMovementBoundsChanged(rect2, z, z2, z3);
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
