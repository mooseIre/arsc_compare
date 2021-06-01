package com.android.systemui.pip;

import android.app.ActivityManager;
import android.app.ActivityTaskManager;
import android.app.PictureInPictureParams;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Rect;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.util.Log;
import android.util.Size;
import android.view.SurfaceControl;
import android.window.TaskOrganizer;
import android.window.WindowContainerToken;
import android.window.WindowContainerTransaction;
import android.window.WindowContainerTransactionCallback;
import android.window.WindowOrganizer;
import com.android.internal.os.SomeArgs;
import com.android.systemui.C0016R$integer;
import com.android.systemui.pip.PipAnimationController;
import com.android.systemui.pip.PipSurfaceTransactionHelper;
import com.android.systemui.pip.PipTaskOrganizer;
import com.android.systemui.pip.PipUiEventLogger;
import com.android.systemui.pip.phone.PipUpdateThread;
import com.android.systemui.stackdivider.Divider;
import com.android.systemui.wm.DisplayController;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

public class PipTaskOrganizer extends TaskOrganizer implements DisplayController.OnDisplaysChangedListener {
    private static final String TAG = PipTaskOrganizer.class.getSimpleName();
    private final int mEnterExitAnimationDuration;
    private boolean mExitingPip;
    private boolean mInPip;
    private final Map<IBinder, Configuration> mInitialState = new HashMap();
    private final Rect mLastReportedBounds = new Rect();
    private SurfaceControl mLeash;
    private final Handler mMainHandler = new Handler(Looper.getMainLooper());
    private int mOneShotAnimationType = 0;
    private PictureInPictureParams mPictureInPictureParams;
    private final PipAnimationController.PipAnimationCallback mPipAnimationCallback = new PipAnimationController.PipAnimationCallback() {
        /* class com.android.systemui.pip.PipTaskOrganizer.AnonymousClass1 */

        @Override // com.android.systemui.pip.PipAnimationController.PipAnimationCallback
        public void onPipAnimationStart(PipAnimationController.PipTransitionAnimator pipTransitionAnimator) {
            PipTaskOrganizer.this.sendOnPipTransitionStarted(pipTransitionAnimator.getTransitionDirection());
        }

        @Override // com.android.systemui.pip.PipAnimationController.PipAnimationCallback
        public void onPipAnimationEnd(SurfaceControl.Transaction transaction, PipAnimationController.PipTransitionAnimator pipTransitionAnimator) {
            PipTaskOrganizer.this.finishResize(transaction, pipTransitionAnimator.getDestinationBounds(), pipTransitionAnimator.getTransitionDirection(), pipTransitionAnimator.getAnimationType());
            PipTaskOrganizer.this.sendOnPipTransitionFinished(pipTransitionAnimator.getTransitionDirection());
        }

        @Override // com.android.systemui.pip.PipAnimationController.PipAnimationCallback
        public void onPipAnimationCancel(PipAnimationController.PipTransitionAnimator pipTransitionAnimator) {
            PipTaskOrganizer.this.sendOnPipTransitionCancelled(pipTransitionAnimator.getTransitionDirection());
        }
    };
    private final PipAnimationController mPipAnimationController;
    private final PipBoundsHandler mPipBoundsHandler;
    private final List<PipTransitionCallback> mPipTransitionCallbacks = new ArrayList();
    private final PipUiEventLogger mPipUiEventLoggerLogger;
    private boolean mShouldDeferEnteringPip;
    private final Divider mSplitDivider;
    private PipSurfaceTransactionHelper.SurfaceControlTransactionFactory mSurfaceControlTransactionFactory;
    private final PipSurfaceTransactionHelper mSurfaceTransactionHelper;
    private ActivityManager.RunningTaskInfo mTaskInfo;
    private WindowContainerToken mToken;
    private final Handler.Callback mUpdateCallbacks = new Handler.Callback() {
        /* class com.android.systemui.pip.$$Lambda$PipTaskOrganizer$5wOr4WCerG3hW1uyUvJi4O0nPDE */

        public final boolean handleMessage(Message message) {
            return PipTaskOrganizer.this.lambda$new$0$PipTaskOrganizer(message);
        }
    };
    private final Handler mUpdateHandler = new Handler(PipUpdateThread.get().getLooper(), this.mUpdateCallbacks);

    public interface PipTransitionCallback {
        void onPipTransitionCanceled(ComponentName componentName, int i);

        void onPipTransitionFinished(ComponentName componentName, int i);

        void onPipTransitionStarted(ComponentName componentName, int i);
    }

    public int getOutPipWindowingMode() {
        return 0;
    }

    public void onBackPressedOnTaskRoot(ActivityManager.RunningTaskInfo runningTaskInfo) {
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$new$0 */
    public /* synthetic */ boolean lambda$new$0$PipTaskOrganizer(Message message) {
        SomeArgs someArgs = (SomeArgs) message.obj;
        Consumer consumer = (Consumer) someArgs.arg1;
        int i = message.what;
        if (i == 1) {
            Rect rect = (Rect) someArgs.arg2;
            resizePip(rect);
            if (consumer != null) {
                consumer.accept(rect);
            }
        } else if (i == 2) {
            Rect rect2 = (Rect) someArgs.arg2;
            Rect rect3 = (Rect) someArgs.arg3;
            animateResizePip(rect2, rect3, (Rect) someArgs.arg4, someArgs.argi1, someArgs.argi2);
            if (consumer != null) {
                consumer.accept(rect3);
            }
        } else if (i == 3) {
            Rect rect4 = (Rect) someArgs.arg2;
            int i2 = someArgs.argi1;
            offsetPip(rect4, 0, i2, someArgs.argi2);
            Rect rect5 = new Rect(rect4);
            rect5.offset(0, i2);
            if (consumer != null) {
                consumer.accept(rect5);
            }
        } else if (i == 4) {
            Rect rect6 = (Rect) someArgs.arg3;
            finishResize((SurfaceControl.Transaction) someArgs.arg2, rect6, someArgs.argi1, -1);
            if (consumer != null) {
                consumer.accept(rect6);
            }
        } else if (i == 5) {
            userResizePip((Rect) someArgs.arg2, (Rect) someArgs.arg3);
        }
        someArgs.recycle();
        return true;
    }

    public PipTaskOrganizer(Context context, PipBoundsHandler pipBoundsHandler, PipSurfaceTransactionHelper pipSurfaceTransactionHelper, Divider divider, DisplayController displayController, PipAnimationController pipAnimationController, PipUiEventLogger pipUiEventLogger) {
        this.mPipBoundsHandler = pipBoundsHandler;
        this.mEnterExitAnimationDuration = context.getResources().getInteger(C0016R$integer.config_pipResizeAnimationDuration);
        this.mSurfaceTransactionHelper = pipSurfaceTransactionHelper;
        this.mPipAnimationController = pipAnimationController;
        this.mPipUiEventLoggerLogger = pipUiEventLogger;
        this.mSurfaceControlTransactionFactory = $$Lambda$0FLZQAxNoOm85ohJ3bgjkYQDWsU.INSTANCE;
        this.mSplitDivider = divider;
        displayController.addDisplayWindowListener(this);
    }

    public Handler getUpdateHandler() {
        return this.mUpdateHandler;
    }

    public Rect getLastReportedBounds() {
        return new Rect(this.mLastReportedBounds);
    }

    public Rect getCurrentOrAnimatingBounds() {
        PipAnimationController.PipTransitionAnimator currentAnimator = this.mPipAnimationController.getCurrentAnimator();
        if (currentAnimator == null || !currentAnimator.isRunning()) {
            return getLastReportedBounds();
        }
        return new Rect(currentAnimator.getDestinationBounds());
    }

    public boolean isInPip() {
        return this.mInPip;
    }

    public boolean isDeferringEnterPipAnimation() {
        return this.mInPip && this.mShouldDeferEnteringPip;
    }

    public void registerPipTransitionCallback(PipTransitionCallback pipTransitionCallback) {
        this.mPipTransitionCallbacks.add(pipTransitionCallback);
    }

    public void setOneShotAnimationType(int i) {
        this.mOneShotAnimationType = i;
    }

    public void exitPip(final int i) {
        if (!this.mInPip || this.mExitingPip || this.mToken == null) {
            Log.wtf(TAG, "Not allowed to exitPip in current state mInPip=" + this.mInPip + " mExitingPip=" + this.mExitingPip + " mToken=" + this.mToken);
            return;
        }
        this.mPipUiEventLoggerLogger.log(PipUiEventLogger.PipUiEventEnum.PICTURE_IN_PICTURE_EXPAND_TO_FULLSCREEN);
        Configuration remove = this.mInitialState.remove(this.mToken.asBinder());
        boolean z = remove.windowConfiguration.getRotation() != this.mPipBoundsHandler.getDisplayRotation();
        WindowContainerTransaction windowContainerTransaction = new WindowContainerTransaction();
        final Rect bounds = remove.windowConfiguration.getBounds();
        int i2 = 4;
        final int i3 = syncWithSplitScreenBounds(bounds) ? 4 : 3;
        if (z) {
            sendOnPipTransitionStarted(i3);
            applyWindowingModeChangeOnExit(windowContainerTransaction, i3);
            WindowOrganizer.applyTransaction(windowContainerTransaction);
            sendOnPipTransitionFinished(i3);
            this.mInPip = false;
        } else {
            SurfaceControl.Transaction transaction = this.mSurfaceControlTransactionFactory.getTransaction();
            this.mSurfaceTransactionHelper.scale(transaction, this.mLeash, bounds, this.mLastReportedBounds);
            transaction.setWindowCrop(this.mLeash, bounds.width(), bounds.height());
            WindowContainerToken windowContainerToken = this.mToken;
            if (i3 != 4) {
                i2 = 1;
            }
            windowContainerTransaction.setActivityWindowingMode(windowContainerToken, i2);
            windowContainerTransaction.setBounds(this.mToken, bounds);
            windowContainerTransaction.setBoundsChangeTransaction(this.mToken, transaction);
            applySyncTransaction(windowContainerTransaction, new WindowContainerTransactionCallback() {
                /* class com.android.systemui.pip.PipTaskOrganizer.AnonymousClass2 */

                public void onTransactionReady(int i, SurfaceControl.Transaction transaction) {
                    transaction.apply();
                    PipTaskOrganizer pipTaskOrganizer = PipTaskOrganizer.this;
                    pipTaskOrganizer.scheduleAnimateResizePip(pipTaskOrganizer.mLastReportedBounds, bounds, null, i3, i, null);
                    PipTaskOrganizer.this.mInPip = false;
                }
            });
        }
        this.mExitingPip = true;
    }

    private void applyWindowingModeChangeOnExit(WindowContainerTransaction windowContainerTransaction, int i) {
        windowContainerTransaction.setWindowingMode(this.mToken, getOutPipWindowingMode());
        windowContainerTransaction.setActivityWindowingMode(this.mToken, 0);
        Divider divider = this.mSplitDivider;
        if (divider != null && i == 4) {
            windowContainerTransaction.reparent(this.mToken, divider.getSecondaryRoot(), true);
        }
    }

    public void removePip() {
        if (!this.mInPip || this.mExitingPip || this.mToken == null) {
            String str = TAG;
            Log.wtf(str, "Not allowed to removePip in current state mInPip=" + this.mInPip + " mExitingPip=" + this.mExitingPip + " mToken=" + this.mToken);
            return;
        }
        getUpdateHandler().post(new Runnable() {
            /* class com.android.systemui.pip.$$Lambda$PipTaskOrganizer$imSGihBVaB7Q3dNvjZL6kcUBBY */

            public final void run() {
                PipTaskOrganizer.this.lambda$removePip$1$PipTaskOrganizer();
            }
        });
        this.mInitialState.remove(this.mToken.asBinder());
        this.mExitingPip = true;
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$removePip$1 */
    public /* synthetic */ void lambda$removePip$1$PipTaskOrganizer() {
        try {
            WindowContainerTransaction windowContainerTransaction = new WindowContainerTransaction();
            windowContainerTransaction.setBounds(this.mToken, (Rect) null);
            WindowOrganizer.applyTransaction(windowContainerTransaction);
            ActivityTaskManager.getService().removeStacksInWindowingModes(new int[]{2});
        } catch (RemoteException e) {
            Log.e(TAG, "Failed to remove PiP", e);
        }
    }

    public void onTaskAppeared(ActivityManager.RunningTaskInfo runningTaskInfo, SurfaceControl surfaceControl) {
        Objects.requireNonNull(runningTaskInfo, "Requires RunningTaskInfo");
        this.mTaskInfo = runningTaskInfo;
        WindowContainerToken windowContainerToken = runningTaskInfo.token;
        this.mToken = windowContainerToken;
        this.mInPip = true;
        this.mExitingPip = false;
        this.mLeash = surfaceControl;
        this.mInitialState.put(windowContainerToken.asBinder(), new Configuration(this.mTaskInfo.configuration));
        ActivityManager.RunningTaskInfo runningTaskInfo2 = this.mTaskInfo;
        this.mPictureInPictureParams = runningTaskInfo2.pictureInPictureParams;
        this.mPipUiEventLoggerLogger.setTaskInfo(runningTaskInfo2);
        this.mPipUiEventLoggerLogger.log(PipUiEventLogger.PipUiEventEnum.PICTURE_IN_PICTURE_ENTER);
        if (this.mShouldDeferEnteringPip) {
            SurfaceControl.Transaction transaction = this.mSurfaceControlTransactionFactory.getTransaction();
            transaction.setAlpha(this.mLeash, 0.0f);
            transaction.show(this.mLeash);
            transaction.apply();
            return;
        }
        Rect destinationBounds = this.mPipBoundsHandler.getDestinationBounds(this.mTaskInfo.topActivity, getAspectRatioOrDefault(this.mPictureInPictureParams), null, getMinimalSize(this.mTaskInfo.topActivityInfo));
        Objects.requireNonNull(destinationBounds, "Missing destination bounds");
        Rect bounds = this.mTaskInfo.configuration.windowConfiguration.getBounds();
        int i = this.mOneShotAnimationType;
        if (i == 0) {
            scheduleAnimateResizePip(bounds, destinationBounds, getValidSourceHintRect(runningTaskInfo, bounds), 2, this.mEnterExitAnimationDuration, null);
        } else if (i == 1) {
            enterPipWithAlphaAnimation(destinationBounds, (long) this.mEnterExitAnimationDuration);
            this.mOneShotAnimationType = 0;
        } else {
            throw new RuntimeException("Unrecognized animation type: " + this.mOneShotAnimationType);
        }
    }

    private Rect getValidSourceHintRect(ActivityManager.RunningTaskInfo runningTaskInfo, Rect rect) {
        PictureInPictureParams pictureInPictureParams = runningTaskInfo.pictureInPictureParams;
        Rect sourceRectHint = (pictureInPictureParams == null || !pictureInPictureParams.hasSourceBoundsHint()) ? null : runningTaskInfo.pictureInPictureParams.getSourceRectHint();
        if (sourceRectHint == null || !rect.contains(sourceRectHint)) {
            return null;
        }
        return sourceRectHint;
    }

    private void enterPipWithAlphaAnimation(final Rect rect, final long j) {
        SurfaceControl.Transaction transaction = this.mSurfaceControlTransactionFactory.getTransaction();
        transaction.setAlpha(this.mLeash, 0.0f);
        transaction.apply();
        WindowContainerTransaction windowContainerTransaction = new WindowContainerTransaction();
        windowContainerTransaction.setActivityWindowingMode(this.mToken, 0);
        windowContainerTransaction.setBounds(this.mToken, rect);
        windowContainerTransaction.scheduleFinishEnterPip(this.mToken, rect);
        applySyncTransaction(windowContainerTransaction, new WindowContainerTransactionCallback() {
            /* class com.android.systemui.pip.PipTaskOrganizer.AnonymousClass3 */

            public void onTransactionReady(int i, SurfaceControl.Transaction transaction) {
                transaction.apply();
                PipTaskOrganizer.this.mUpdateHandler.post(new Runnable(rect, j) {
                    /* class com.android.systemui.pip.$$Lambda$PipTaskOrganizer$3$8PwkJ_EmtPzPmZpwIQc9Ay0188 */
                    public final /* synthetic */ Rect f$1;
                    public final /* synthetic */ long f$2;

                    {
                        this.f$1 = r2;
                        this.f$2 = r3;
                    }

                    public final void run() {
                        PipTaskOrganizer.AnonymousClass3.this.lambda$onTransactionReady$0$PipTaskOrganizer$3(this.f$1, this.f$2);
                    }
                });
            }

            /* access modifiers changed from: private */
            /* renamed from: lambda$onTransactionReady$0 */
            public /* synthetic */ void lambda$onTransactionReady$0$PipTaskOrganizer$3(Rect rect, long j) {
                PipAnimationController.PipTransitionAnimator animator = PipTaskOrganizer.this.mPipAnimationController.getAnimator(PipTaskOrganizer.this.mLeash, rect, 0.0f, 1.0f);
                animator.setTransitionDirection(2);
                animator.setPipAnimationCallback(PipTaskOrganizer.this.mPipAnimationCallback);
                animator.setDuration(j).start();
            }
        });
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void sendOnPipTransitionStarted(int i) {
        runOnMainHandler(new Runnable(i) {
            /* class com.android.systemui.pip.$$Lambda$PipTaskOrganizer$JUO3Grq_R7eMASAsK_v_c6_mMuI */
            public final /* synthetic */ int f$1;

            {
                this.f$1 = r2;
            }

            public final void run() {
                PipTaskOrganizer.this.lambda$sendOnPipTransitionStarted$2$PipTaskOrganizer(this.f$1);
            }
        });
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$sendOnPipTransitionStarted$2 */
    public /* synthetic */ void lambda$sendOnPipTransitionStarted$2$PipTaskOrganizer(int i) {
        for (int size = this.mPipTransitionCallbacks.size() - 1; size >= 0; size--) {
            this.mPipTransitionCallbacks.get(size).onPipTransitionStarted(this.mTaskInfo.baseActivity, i);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void sendOnPipTransitionFinished(int i) {
        runOnMainHandler(new Runnable(i) {
            /* class com.android.systemui.pip.$$Lambda$PipTaskOrganizer$cVPyDgP7uB9XVGZ8xzNAyEDd4 */
            public final /* synthetic */ int f$1;

            {
                this.f$1 = r2;
            }

            public final void run() {
                PipTaskOrganizer.this.lambda$sendOnPipTransitionFinished$3$PipTaskOrganizer(this.f$1);
            }
        });
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$sendOnPipTransitionFinished$3 */
    public /* synthetic */ void lambda$sendOnPipTransitionFinished$3$PipTaskOrganizer(int i) {
        for (int size = this.mPipTransitionCallbacks.size() - 1; size >= 0; size--) {
            this.mPipTransitionCallbacks.get(size).onPipTransitionFinished(this.mTaskInfo.baseActivity, i);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void sendOnPipTransitionCancelled(int i) {
        runOnMainHandler(new Runnable(i) {
            /* class com.android.systemui.pip.$$Lambda$PipTaskOrganizer$TTTo1eGF0SlCllWVDuAJlbhmvjo */
            public final /* synthetic */ int f$1;

            {
                this.f$1 = r2;
            }

            public final void run() {
                PipTaskOrganizer.this.lambda$sendOnPipTransitionCancelled$4$PipTaskOrganizer(this.f$1);
            }
        });
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$sendOnPipTransitionCancelled$4 */
    public /* synthetic */ void lambda$sendOnPipTransitionCancelled$4$PipTaskOrganizer(int i) {
        for (int size = this.mPipTransitionCallbacks.size() - 1; size >= 0; size--) {
            this.mPipTransitionCallbacks.get(size).onPipTransitionCanceled(this.mTaskInfo.baseActivity, i);
        }
    }

    private void runOnMainHandler(Runnable runnable) {
        if (Looper.getMainLooper() == Looper.myLooper()) {
            runnable.run();
        } else {
            this.mMainHandler.post(runnable);
        }
    }

    public void onTaskVanished(ActivityManager.RunningTaskInfo runningTaskInfo) {
        if (this.mInPip) {
            WindowContainerToken windowContainerToken = runningTaskInfo.token;
            Objects.requireNonNull(windowContainerToken, "Requires valid WindowContainerToken");
            if (windowContainerToken.asBinder() != this.mToken.asBinder()) {
                String str = TAG;
                Log.wtf(str, "Unrecognized token: " + windowContainerToken);
                return;
            }
            this.mShouldDeferEnteringPip = false;
            this.mPictureInPictureParams = null;
            this.mInPip = false;
            this.mExitingPip = false;
            this.mPipUiEventLoggerLogger.setTaskInfo(null);
        }
    }

    public void onTaskInfoChanged(ActivityManager.RunningTaskInfo runningTaskInfo) {
        Objects.requireNonNull(this.mToken, "onTaskInfoChanged requires valid existing mToken");
        PictureInPictureParams pictureInPictureParams = runningTaskInfo.pictureInPictureParams;
        if (pictureInPictureParams == null || !applyPictureInPictureParams(pictureInPictureParams)) {
            String str = TAG;
            Log.d(str, "Ignored onTaskInfoChanged with PiP param: " + pictureInPictureParams);
            return;
        }
        Rect destinationBounds = this.mPipBoundsHandler.getDestinationBounds(runningTaskInfo.topActivity, getAspectRatioOrDefault(pictureInPictureParams), this.mLastReportedBounds, getMinimalSize(runningTaskInfo.topActivityInfo), true);
        Objects.requireNonNull(destinationBounds, "Missing destination bounds");
        scheduleAnimateResizePip(destinationBounds, this.mEnterExitAnimationDuration, null);
    }

    @Override // com.android.systemui.wm.DisplayController.OnDisplaysChangedListener
    public void onFixedRotationStarted(int i, int i2) {
        this.mShouldDeferEnteringPip = true;
    }

    @Override // com.android.systemui.wm.DisplayController.OnDisplaysChangedListener
    public void onFixedRotationFinished(int i) {
        if (this.mShouldDeferEnteringPip && this.mInPip) {
            enterPipWithAlphaAnimation(this.mPipBoundsHandler.getDestinationBounds(this.mTaskInfo.topActivity, getAspectRatioOrDefault(this.mPictureInPictureParams), null, getMinimalSize(this.mTaskInfo.topActivityInfo)), 0);
        }
        this.mShouldDeferEnteringPip = false;
    }

    public void onMovementBoundsChanged(Rect rect, boolean z, boolean z2, boolean z3, WindowContainerTransaction windowContainerTransaction) {
        PipAnimationController.PipTransitionAnimator currentAnimator = this.mPipAnimationController.getCurrentAnimator();
        if (currentAnimator != null && currentAnimator.isRunning() && currentAnimator.getTransitionDirection() == 2) {
            Rect destinationBounds = currentAnimator.getDestinationBounds();
            rect.set(destinationBounds);
            if (z2 || z3 || !this.mPipBoundsHandler.getDisplayBounds().contains(destinationBounds)) {
                Rect destinationBounds2 = this.mPipBoundsHandler.getDestinationBounds(this.mTaskInfo.topActivity, getAspectRatioOrDefault(this.mPictureInPictureParams), null, getMinimalSize(this.mTaskInfo.topActivityInfo));
                if (!destinationBounds2.equals(destinationBounds)) {
                    if (currentAnimator.getAnimationType() == 0) {
                        currentAnimator.updateEndValue(destinationBounds2);
                    }
                    currentAnimator.setDestinationBounds(destinationBounds2);
                    rect.set(destinationBounds2);
                }
            }
        } else if (this.mInPip && z) {
            int i = 0;
            if (currentAnimator != null) {
                i = currentAnimator.getTransitionDirection();
                currentAnimator.removeAllUpdateListeners();
                currentAnimator.removeAllListeners();
                currentAnimator.cancel();
                sendOnPipTransitionCancelled(i);
                sendOnPipTransitionFinished(i);
            }
            this.mLastReportedBounds.set(rect);
            prepareFinishResizeTransaction(rect, i, createFinishResizeSurfaceTransaction(rect), windowContainerTransaction);
        } else if (currentAnimator == null || !currentAnimator.isRunning()) {
            if (!this.mLastReportedBounds.isEmpty()) {
                rect.set(this.mLastReportedBounds);
            }
        } else if (!currentAnimator.getDestinationBounds().isEmpty()) {
            rect.set(currentAnimator.getDestinationBounds());
        }
    }

    private boolean applyPictureInPictureParams(PictureInPictureParams pictureInPictureParams) {
        PictureInPictureParams pictureInPictureParams2 = this.mPictureInPictureParams;
        boolean z = pictureInPictureParams2 == null || !Objects.equals(pictureInPictureParams2.getAspectRatioRational(), pictureInPictureParams.getAspectRatioRational());
        if (z) {
            this.mPictureInPictureParams = pictureInPictureParams;
            this.mPipBoundsHandler.onAspectRatioChanged(pictureInPictureParams.getAspectRatio());
        }
        return z;
    }

    public void scheduleAnimateResizePip(Rect rect, int i, Consumer<Rect> consumer) {
        if (this.mShouldDeferEnteringPip) {
            Log.d(TAG, "skip scheduleAnimateResizePip, entering pip deferred");
        } else {
            scheduleAnimateResizePip(this.mLastReportedBounds, rect, null, 0, i, consumer);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void scheduleAnimateResizePip(Rect rect, Rect rect2, Rect rect3, int i, int i2, Consumer<Rect> consumer) {
        if (this.mInPip) {
            SomeArgs obtain = SomeArgs.obtain();
            obtain.arg1 = consumer;
            obtain.arg2 = rect;
            obtain.arg3 = rect2;
            obtain.arg4 = rect3;
            obtain.argi1 = i;
            obtain.argi2 = i2;
            Handler handler = this.mUpdateHandler;
            handler.sendMessage(handler.obtainMessage(2, obtain));
        }
    }

    public void scheduleResizePip(Rect rect, Consumer<Rect> consumer) {
        SomeArgs obtain = SomeArgs.obtain();
        obtain.arg1 = consumer;
        obtain.arg2 = rect;
        Handler handler = this.mUpdateHandler;
        handler.sendMessage(handler.obtainMessage(1, obtain));
    }

    public void scheduleUserResizePip(Rect rect, Rect rect2, Consumer<Rect> consumer) {
        SomeArgs obtain = SomeArgs.obtain();
        obtain.arg1 = consumer;
        obtain.arg2 = rect;
        obtain.arg3 = rect2;
        Handler handler = this.mUpdateHandler;
        handler.sendMessage(handler.obtainMessage(5, obtain));
    }

    public void scheduleFinishResizePip(Rect rect) {
        scheduleFinishResizePip(rect, null);
    }

    public void scheduleFinishResizePip(Rect rect, Consumer<Rect> consumer) {
        scheduleFinishResizePip(rect, 0, consumer);
    }

    private void scheduleFinishResizePip(Rect rect, int i, Consumer<Rect> consumer) {
        if (!shouldBlockResizeRequest()) {
            SomeArgs obtain = SomeArgs.obtain();
            obtain.arg1 = consumer;
            obtain.arg2 = createFinishResizeSurfaceTransaction(rect);
            obtain.arg3 = rect;
            obtain.argi1 = i;
            Handler handler = this.mUpdateHandler;
            handler.sendMessage(handler.obtainMessage(4, obtain));
        }
    }

    private SurfaceControl.Transaction createFinishResizeSurfaceTransaction(Rect rect) {
        SurfaceControl.Transaction transaction = this.mSurfaceControlTransactionFactory.getTransaction();
        PipSurfaceTransactionHelper pipSurfaceTransactionHelper = this.mSurfaceTransactionHelper;
        pipSurfaceTransactionHelper.crop(transaction, this.mLeash, rect);
        pipSurfaceTransactionHelper.resetScale(transaction, this.mLeash, rect);
        pipSurfaceTransactionHelper.round(transaction, this.mLeash, this.mInPip);
        return transaction;
    }

    public void scheduleOffsetPip(Rect rect, int i, int i2, Consumer<Rect> consumer) {
        if (!shouldBlockResizeRequest()) {
            if (this.mShouldDeferEnteringPip) {
                Log.d(TAG, "skip scheduleOffsetPip, entering pip deferred");
                return;
            }
            SomeArgs obtain = SomeArgs.obtain();
            obtain.arg1 = consumer;
            obtain.arg2 = rect;
            obtain.argi1 = i;
            obtain.argi2 = i2;
            Handler handler = this.mUpdateHandler;
            handler.sendMessage(handler.obtainMessage(3, obtain));
        }
    }

    private void offsetPip(Rect rect, int i, int i2, int i3) {
        if (Looper.myLooper() != this.mUpdateHandler.getLooper()) {
            throw new RuntimeException("Callers should call scheduleOffsetPip() instead of this directly");
        } else if (this.mTaskInfo == null) {
            Log.w(TAG, "mTaskInfo is not set");
        } else {
            Rect rect2 = new Rect(rect);
            rect2.offset(i, i2);
            animateResizePip(rect, rect2, null, 1, i3);
        }
    }

    private void resizePip(Rect rect) {
        if (Looper.myLooper() != this.mUpdateHandler.getLooper()) {
            throw new RuntimeException("Callers should call scheduleResizePip() instead of this directly");
        } else if (this.mToken == null || this.mLeash == null) {
            Log.w(TAG, "Abort animation, invalid leash");
        } else {
            this.mLastReportedBounds.set(rect);
            SurfaceControl.Transaction transaction = this.mSurfaceControlTransactionFactory.getTransaction();
            PipSurfaceTransactionHelper pipSurfaceTransactionHelper = this.mSurfaceTransactionHelper;
            pipSurfaceTransactionHelper.crop(transaction, this.mLeash, rect);
            pipSurfaceTransactionHelper.round(transaction, this.mLeash, this.mInPip);
            transaction.apply();
        }
    }

    private void userResizePip(Rect rect, Rect rect2) {
        String str = TAG;
        if (Looper.myLooper() != this.mUpdateHandler.getLooper()) {
            throw new RuntimeException("Callers should call scheduleUserResizePip() instead of this directly");
        } else if (this.mToken == null || this.mLeash == null) {
            Log.w(str, "Abort animation, invalid leash");
        } else if (rect.isEmpty() || rect2.isEmpty()) {
            Log.w(str, "Attempted to user resize PIP to or from empty bounds, aborting.");
        } else {
            SurfaceControl.Transaction transaction = this.mSurfaceControlTransactionFactory.getTransaction();
            this.mSurfaceTransactionHelper.scale(transaction, this.mLeash, rect, rect2);
            transaction.apply();
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void finishResize(SurfaceControl.Transaction transaction, Rect rect, int i, int i2) {
        if (Looper.myLooper() == this.mUpdateHandler.getLooper()) {
            this.mLastReportedBounds.set(rect);
            if (!PipAnimationController.isInPipDirection(i) || i2 != 1) {
                WindowContainerTransaction windowContainerTransaction = new WindowContainerTransaction();
                prepareFinishResizeTransaction(rect, i, transaction, windowContainerTransaction);
                applyFinishBoundsResize(windowContainerTransaction, i);
                return;
            }
            return;
        }
        throw new RuntimeException("Callers should call scheduleResizePip() instead of this directly");
    }

    private void prepareFinishResizeTransaction(Rect rect, int i, SurfaceControl.Transaction transaction, WindowContainerTransaction windowContainerTransaction) {
        if (PipAnimationController.isInPipDirection(i)) {
            windowContainerTransaction.setActivityWindowingMode(this.mToken, 0);
            windowContainerTransaction.scheduleFinishEnterPip(this.mToken, rect);
        } else if (PipAnimationController.isOutPipDirection(i)) {
            if (i == 3) {
                rect = null;
            }
            applyWindowingModeChangeOnExit(windowContainerTransaction, i);
        }
        windowContainerTransaction.setBounds(this.mToken, rect);
        windowContainerTransaction.setBoundsChangeTransaction(this.mToken, transaction);
    }

    public void applyFinishBoundsResize(WindowContainerTransaction windowContainerTransaction, int i) {
        WindowOrganizer.applyTransaction(windowContainerTransaction);
    }

    private void animateResizePip(Rect rect, Rect rect2, Rect rect3, int i, int i2) {
        SurfaceControl surfaceControl;
        if (Looper.myLooper() != this.mUpdateHandler.getLooper()) {
            throw new RuntimeException("Callers should call scheduleAnimateResizePip() instead of this directly");
        } else if (this.mToken == null || (surfaceControl = this.mLeash) == null) {
            Log.w(TAG, "Abort animation, invalid leash");
        } else {
            PipAnimationController.PipTransitionAnimator animator = this.mPipAnimationController.getAnimator(surfaceControl, rect, rect2, rect3);
            animator.setTransitionDirection(i);
            animator.setPipAnimationCallback(this.mPipAnimationCallback);
            animator.setDuration((long) i2).start();
        }
    }

    private Size getMinimalSize(ActivityInfo activityInfo) {
        ActivityInfo.WindowLayout windowLayout;
        if (activityInfo == null || (windowLayout = activityInfo.windowLayout) == null || windowLayout.minWidth <= 0 || windowLayout.minHeight <= 0) {
            return null;
        }
        return new Size(windowLayout.minWidth, windowLayout.minHeight);
    }

    private float getAspectRatioOrDefault(PictureInPictureParams pictureInPictureParams) {
        if (pictureInPictureParams == null || !pictureInPictureParams.hasSetAspectRatio()) {
            return this.mPipBoundsHandler.getDefaultAspectRatio();
        }
        return pictureInPictureParams.getAspectRatio();
    }

    private boolean shouldBlockResizeRequest() {
        return !this.mInPip || this.mExitingPip;
    }

    private boolean syncWithSplitScreenBounds(Rect rect) {
        Divider divider = this.mSplitDivider;
        if (divider == null || !divider.isDividerVisible()) {
            return false;
        }
        rect.set(this.mSplitDivider.getView().getNonMinimizedSplitScreenSecondaryBounds());
        return true;
    }

    public void dump(PrintWriter printWriter, String str) {
        String str2 = str + "  ";
        printWriter.println(str + TAG);
        printWriter.println(str2 + "mTaskInfo=" + this.mTaskInfo);
        StringBuilder sb = new StringBuilder();
        sb.append(str2);
        sb.append("mToken=");
        sb.append(this.mToken);
        sb.append(" binder=");
        WindowContainerToken windowContainerToken = this.mToken;
        sb.append(windowContainerToken != null ? windowContainerToken.asBinder() : null);
        printWriter.println(sb.toString());
        printWriter.println(str2 + "mLeash=" + this.mLeash);
        printWriter.println(str2 + "mInPip=" + this.mInPip);
        printWriter.println(str2 + "mOneShotAnimationType=" + this.mOneShotAnimationType);
        printWriter.println(str2 + "mPictureInPictureParams=" + this.mPictureInPictureParams);
        printWriter.println(str2 + "mLastReportedBounds=" + this.mLastReportedBounds);
        StringBuilder sb2 = new StringBuilder();
        sb2.append(str2);
        sb2.append("mInitialState:");
        printWriter.println(sb2.toString());
        for (Map.Entry<IBinder, Configuration> entry : this.mInitialState.entrySet()) {
            printWriter.println(str2 + "  binder=" + entry.getKey() + " winConfig=" + entry.getValue().windowConfiguration);
        }
    }
}
