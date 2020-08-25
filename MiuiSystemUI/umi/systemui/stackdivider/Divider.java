package com.android.systemui.stackdivider;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.app.ActivityManager;
import android.app.ActivityTaskManager;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Rect;
import android.os.Handler;
import android.provider.Settings;
import android.util.Slog;
import android.view.LayoutInflater;
import android.view.SurfaceControl;
import android.view.ViewGroup;
import android.window.TaskOrganizer;
import android.window.WindowContainerToken;
import android.window.WindowContainerTransaction;
import android.window.WindowOrganizer;
import com.android.systemui.Dependency;
import com.android.systemui.SystemUI;
import com.android.systemui.TransactionPool;
import com.android.systemui.plugins.R;
import com.android.systemui.recents.misc.SystemServicesProxy;
import com.android.systemui.stackdivider.Divider;
import com.android.systemui.stackdivider.DividerView;
import com.android.systemui.statusbar.policy.KeyguardMonitor;
import com.android.systemui.statusbar.policy.KeyguardMonitorImpl;
import com.android.systemui.wm.DisplayChangeController;
import com.android.systemui.wm.DisplayController;
import com.android.systemui.wm.DisplayImeController;
import com.android.systemui.wm.DisplayLayout;
import com.android.systemui.wm.SystemWindows;
import com.miui.systemui.annotation.Inject;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class Divider extends SystemUI implements DividerView.DividerCallbacks, DisplayController.OnDisplaysChangedListener {
    private SystemServicesProxy.TaskStackListener mActivityRestartListener = new SystemServicesProxy.TaskStackListener() {
        public void onActivityRestartAttempt(ActivityManager.RunningTaskInfo runningTaskInfo, boolean z, boolean z2, boolean z3) {
            if (z3 && runningTaskInfo.configuration.windowConfiguration.getWindowingMode() == 3 && Divider.this.mSplits.isSplitScreenSupported() && Divider.this.isMinimized()) {
                Divider.this.onUndockingTask(true);
            }
        }
    };
    private boolean mAdjustedForIme = false;
    @Inject
    private DisplayController mDisplayController;
    private final DividerState mDividerState = new DividerState();
    private DockedStackExistsChangedListener mDockedStackExistsChangedListener;
    private final ArrayList<WeakReference<Consumer<Boolean>>> mDockedStackExistsListeners = new ArrayList<>();
    private ForcedResizableInfoActivityController mForcedResizableController;
    /* access modifiers changed from: private */
    @Inject(tag = "main_handler")
    public Handler mHandler;
    private boolean mHomeStackResizable = false;
    @Inject
    private DisplayImeController mImeController;
    private final DividerImeController mImePositionProcessor = new DividerImeController();
    /* access modifiers changed from: private */
    public KeyguardMonitorImpl mKeyguardMonitor;
    /* access modifiers changed from: private */
    public boolean mMinimized = false;
    private SplitDisplayLayout mRotateSplitLayout;
    private DisplayChangeController.OnDisplayChangingListener mRotationController = new DisplayChangeController.OnDisplayChangingListener() {
        public final void onRotateDisplay(int i, int i2, int i3, WindowContainerTransaction windowContainerTransaction) {
            Divider.this.lambda$new$0$Divider(i, i2, i3, windowContainerTransaction);
        }
    };
    /* access modifiers changed from: private */
    public SplitDisplayLayout mSplitLayout;
    /* access modifiers changed from: private */
    public SplitScreenTaskOrganizer mSplits = new SplitScreenTaskOrganizer(this);
    @Inject
    private SystemWindows mSystemWindows;
    @Inject
    public TransactionPool mTransactionPool;
    /* access modifiers changed from: private */
    public DividerView mView;
    private boolean mVisible = false;
    private DividerWindowManager mWindowManager;

    public interface DockedStackExistsChangedListener {
        void onDockedStackMinimizedChanged(boolean z);
    }

    public void growRecents() {
    }

    public void onMultiWindowStateChanged(boolean z) {
    }

    public void onRecentsActivityStarting() {
    }

    public void registerDockedStackExistsChangedListener(DockedStackExistsChangedListener dockedStackExistsChangedListener) {
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$new$0 */
    public /* synthetic */ void lambda$new$0$Divider(int i, int i2, int i3, WindowContainerTransaction windowContainerTransaction) {
        int i4;
        if (this.mSplits.isSplitScreenSupported()) {
            SplitDisplayLayout splitDisplayLayout = new SplitDisplayLayout(this.mContext, new DisplayLayout(this.mDisplayController.getDisplayLayout(i)), this.mSplits);
            splitDisplayLayout.rotateTo(i3);
            this.mRotateSplitLayout = splitDisplayLayout;
            if (this.mMinimized) {
                i4 = this.mView.mSnapTargetBeforeMinimized.position;
            } else {
                i4 = this.mView.getCurrentPosition();
            }
            splitDisplayLayout.resizeSplits(splitDisplayLayout.getSnapAlgorithm().calculateNonDismissingSnapTarget(i4).position, windowContainerTransaction);
            if (inSplitMode()) {
                WindowManagerProxy.applyHomeTasksMinimized(splitDisplayLayout, this.mSplits.mSecondary.token, windowContainerTransaction);
            }
        }
    }

    private class DividerImeController implements DisplayImeController.ImePositionProcessor {
        private boolean mAdjusted;
        /* access modifiers changed from: private */
        public ValueAnimator mAnimation;
        private int mHiddenTop;
        private boolean mImeWasShown;
        private int mLastAdjustTop;
        private float mLastPrimaryDim;
        private float mLastSecondaryDim;
        private boolean mPaused;
        private boolean mPausedTargetAdjusted;
        private boolean mSecondaryHasFocus;
        private int mShownTop;
        private boolean mTargetAdjusted;
        private float mTargetPrimaryDim;
        private float mTargetSecondaryDim;
        private boolean mTargetShown;

        private DividerImeController() {
            this.mHiddenTop = 0;
            this.mShownTop = 0;
            this.mTargetAdjusted = false;
            this.mTargetShown = false;
            this.mTargetPrimaryDim = 0.0f;
            this.mTargetSecondaryDim = 0.0f;
            this.mSecondaryHasFocus = false;
            this.mLastPrimaryDim = 0.0f;
            this.mLastSecondaryDim = 0.0f;
            this.mLastAdjustTop = -1;
            this.mImeWasShown = false;
            this.mAdjusted = false;
            this.mAnimation = null;
            this.mPaused = true;
            this.mPausedTargetAdjusted = false;
        }

        private boolean getSecondaryHasFocus(int i) {
            WindowContainerToken imeTarget = TaskOrganizer.getImeTarget(i);
            return imeTarget != null && imeTarget.asBinder() == Divider.this.mSplits.mSecondary.token.asBinder();
        }

        private void updateDimTargets() {
            boolean z = !Divider.this.mView.isHidden();
            float f = 0.3f;
            this.mTargetPrimaryDim = (!this.mSecondaryHasFocus || !this.mTargetShown || !z) ? 0.0f : 0.3f;
            if (this.mSecondaryHasFocus || !this.mTargetShown || !z) {
                f = 0.0f;
            }
            this.mTargetSecondaryDim = f;
        }

        public void onImeStartPositioning(int i, int i2, int i3, boolean z, SurfaceControl.Transaction transaction) {
            if (Divider.this.inSplitMode()) {
                boolean z2 = true;
                boolean z3 = !Divider.this.mView.isHidden();
                boolean secondaryHasFocus = getSecondaryHasFocus(i);
                this.mSecondaryHasFocus = secondaryHasFocus;
                if (!z3 || !z || !secondaryHasFocus || Divider.this.mSplitLayout.mDisplayLayout.isLandscape()) {
                    z2 = false;
                }
                this.mHiddenTop = i2;
                this.mShownTop = i3;
                this.mTargetShown = z;
                int i4 = this.mLastAdjustTop;
                if (i4 < 0) {
                    if (!z) {
                        i2 = i3;
                    }
                    this.mLastAdjustTop = i2;
                } else {
                    if (z) {
                        i2 = i3;
                    }
                    if (i4 != i2) {
                        boolean z4 = this.mTargetAdjusted;
                        if (z4 != z2 && z2 == this.mAdjusted) {
                            this.mAdjusted = z4;
                        } else if (z2 && this.mTargetAdjusted && this.mAdjusted) {
                            this.mAdjusted = false;
                        }
                    }
                }
                if (this.mPaused) {
                    this.mPausedTargetAdjusted = z2;
                    return;
                }
                this.mTargetAdjusted = z2;
                updateDimTargets();
                if (this.mAnimation != null || (this.mImeWasShown && z && this.mTargetAdjusted != this.mAdjusted)) {
                    startAsyncAnimation();
                }
                if (z3) {
                    updateImeAdjustState();
                }
            }
        }

        private void updateImeAdjustState() {
            WindowContainerTransaction windowContainerTransaction = new WindowContainerTransaction();
            boolean z = false;
            if (this.mTargetAdjusted) {
                SplitDisplayLayout access$200 = Divider.this.mSplitLayout;
                int i = this.mShownTop;
                access$200.updateAdjustedBounds(i, this.mHiddenTop, i);
                windowContainerTransaction.setBounds(Divider.this.mSplits.mSecondary.token, Divider.this.mSplitLayout.mAdjustedSecondary);
                Rect rect = new Rect(Divider.this.mSplits.mSecondary.configuration.windowConfiguration.getAppBounds());
                rect.offset(0, Divider.this.mSplitLayout.mAdjustedSecondary.top - Divider.this.mSplitLayout.mSecondary.top);
                windowContainerTransaction.setAppBounds(Divider.this.mSplits.mSecondary.token, rect);
                windowContainerTransaction.setScreenSizeDp(Divider.this.mSplits.mSecondary.token, Divider.this.mSplits.mSecondary.configuration.screenWidthDp, Divider.this.mSplits.mSecondary.configuration.screenHeightDp);
                windowContainerTransaction.setBounds(Divider.this.mSplits.mPrimary.token, Divider.this.mSplitLayout.mAdjustedPrimary);
                Rect rect2 = new Rect(Divider.this.mSplits.mPrimary.configuration.windowConfiguration.getAppBounds());
                rect2.offset(0, Divider.this.mSplitLayout.mAdjustedPrimary.top - Divider.this.mSplitLayout.mPrimary.top);
                windowContainerTransaction.setAppBounds(Divider.this.mSplits.mPrimary.token, rect2);
                windowContainerTransaction.setScreenSizeDp(Divider.this.mSplits.mPrimary.token, Divider.this.mSplits.mPrimary.configuration.screenWidthDp, Divider.this.mSplits.mPrimary.configuration.screenHeightDp);
            } else {
                windowContainerTransaction.setBounds(Divider.this.mSplits.mSecondary.token, Divider.this.mSplitLayout.mSecondary);
                windowContainerTransaction.setAppBounds(Divider.this.mSplits.mSecondary.token, (Rect) null);
                windowContainerTransaction.setScreenSizeDp(Divider.this.mSplits.mSecondary.token, 0, 0);
                windowContainerTransaction.setBounds(Divider.this.mSplits.mPrimary.token, Divider.this.mSplitLayout.mPrimary);
                windowContainerTransaction.setAppBounds(Divider.this.mSplits.mPrimary.token, (Rect) null);
                windowContainerTransaction.setScreenSizeDp(Divider.this.mSplits.mPrimary.token, 0, 0);
            }
            WindowOrganizer.applyTransaction(windowContainerTransaction);
            if (!this.mPaused) {
                DividerView access$100 = Divider.this.mView;
                boolean z2 = this.mTargetShown;
                access$100.setAdjustedForIme(z2, z2 ? 275 : 340);
            }
            Divider divider = Divider.this;
            if (this.mTargetShown && !this.mPaused) {
                z = true;
            }
            divider.setAdjustedForIme(z);
        }

        public void onImePositionChanged(int i, int i2, SurfaceControl.Transaction transaction) {
            if (this.mAnimation == null && Divider.this.inSplitMode() && !this.mPaused) {
                float f = (float) i2;
                int i3 = this.mHiddenTop;
                float f2 = (f - ((float) i3)) / ((float) (this.mShownTop - i3));
                if (!this.mTargetShown) {
                    f2 = 1.0f - f2;
                }
                onProgress(f2, transaction);
            }
        }

        public void onImeEndPositioning(int i, boolean z, SurfaceControl.Transaction transaction) {
            if (this.mAnimation == null && Divider.this.inSplitMode() && !this.mPaused) {
                onEnd(z, transaction);
            }
        }

        private void onProgress(float f, SurfaceControl.Transaction transaction) {
            boolean z = this.mTargetAdjusted;
            if (z != this.mAdjusted && !this.mPaused) {
                float f2 = z ? f : 1.0f - f;
                this.mLastAdjustTop = (int) ((((float) this.mShownTop) * f2) + ((1.0f - f2) * ((float) this.mHiddenTop)));
                Divider.this.mSplitLayout.updateAdjustedBounds(this.mLastAdjustTop, this.mHiddenTop, this.mShownTop);
                Divider.this.mView.resizeSplitSurfaces(transaction, Divider.this.mSplitLayout.mAdjustedPrimary, Divider.this.mSplitLayout.mAdjustedSecondary);
            }
            float f3 = 1.0f - f;
            Divider.this.mView.setResizeDimLayer(transaction, true, (this.mLastPrimaryDim * f3) + (this.mTargetPrimaryDim * f));
            Divider.this.mView.setResizeDimLayer(transaction, false, (this.mLastSecondaryDim * f3) + (f * this.mTargetSecondaryDim));
        }

        /* access modifiers changed from: private */
        public void onEnd(boolean z, SurfaceControl.Transaction transaction) {
            if (!z) {
                onProgress(1.0f, transaction);
                boolean z2 = this.mTargetAdjusted;
                this.mAdjusted = z2;
                this.mImeWasShown = this.mTargetShown;
                this.mLastAdjustTop = z2 ? this.mShownTop : this.mHiddenTop;
                this.mLastPrimaryDim = this.mTargetPrimaryDim;
                this.mLastSecondaryDim = this.mTargetSecondaryDim;
            }
        }

        private void startAsyncAnimation() {
            ValueAnimator valueAnimator = this.mAnimation;
            if (valueAnimator != null) {
                valueAnimator.cancel();
            }
            ValueAnimator ofFloat = ValueAnimator.ofFloat(new float[]{0.0f, 1.0f});
            this.mAnimation = ofFloat;
            ofFloat.setDuration(275);
            boolean z = this.mTargetAdjusted;
            if (z != this.mAdjusted) {
                int i = this.mHiddenTop;
                float f = (((float) this.mLastAdjustTop) - ((float) i)) / ((float) (this.mShownTop - i));
                if (!z) {
                    f = 1.0f - f;
                }
                this.mAnimation.setCurrentFraction(f);
            }
            this.mAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                public final void onAnimationUpdate(ValueAnimator valueAnimator) {
                    Divider.DividerImeController.this.lambda$startAsyncAnimation$0$Divider$DividerImeController(valueAnimator);
                }
            });
            this.mAnimation.setInterpolator(DisplayImeController.INTERPOLATOR);
            this.mAnimation.addListener(new AnimatorListenerAdapter() {
                private boolean mCancel = false;

                public void onAnimationCancel(Animator animator) {
                    this.mCancel = true;
                }

                public void onAnimationEnd(Animator animator) {
                    SurfaceControl.Transaction acquire = Divider.this.mTransactionPool.acquire();
                    DividerImeController.this.onEnd(this.mCancel, acquire);
                    acquire.apply();
                    Divider.this.mTransactionPool.release(acquire);
                    ValueAnimator unused = DividerImeController.this.mAnimation = null;
                }
            });
            this.mAnimation.start();
        }

        /* access modifiers changed from: private */
        /* renamed from: lambda$startAsyncAnimation$0 */
        public /* synthetic */ void lambda$startAsyncAnimation$0$Divider$DividerImeController(ValueAnimator valueAnimator) {
            SurfaceControl.Transaction acquire = Divider.this.mTransactionPool.acquire();
            onProgress(((Float) valueAnimator.getAnimatedValue()).floatValue(), acquire);
            acquire.apply();
            Divider.this.mTransactionPool.release(acquire);
        }

        public void pause(int i) {
            Divider.this.mHandler.post(new Runnable() {
                public final void run() {
                    Divider.DividerImeController.this.lambda$pause$1$Divider$DividerImeController();
                }
            });
        }

        /* access modifiers changed from: private */
        /* renamed from: lambda$pause$1 */
        public /* synthetic */ void lambda$pause$1$Divider$DividerImeController() {
            if (!this.mPaused) {
                this.mPaused = true;
                this.mPausedTargetAdjusted = this.mTargetAdjusted;
                this.mTargetAdjusted = false;
                this.mTargetSecondaryDim = 0.0f;
                this.mTargetPrimaryDim = 0.0f;
                updateImeAdjustState();
                startAsyncAnimation();
                ValueAnimator valueAnimator = this.mAnimation;
                if (valueAnimator != null) {
                    valueAnimator.end();
                }
            }
        }

        public void resume(int i) {
            Divider.this.mHandler.post(new Runnable() {
                public final void run() {
                    Divider.DividerImeController.this.lambda$resume$2$Divider$DividerImeController();
                }
            });
        }

        /* access modifiers changed from: private */
        /* renamed from: lambda$resume$2 */
        public /* synthetic */ void lambda$resume$2$Divider$DividerImeController() {
            if (this.mPaused) {
                this.mPaused = false;
                this.mTargetAdjusted = this.mPausedTargetAdjusted;
                updateDimTargets();
                if (!(this.mTargetAdjusted == this.mAdjusted || Divider.this.mMinimized || Divider.this.mView == null)) {
                    Divider.this.mView.finishAnimations();
                }
                updateImeAdjustState();
                startAsyncAnimation();
            }
        }
    }

    public void start() {
        this.mWindowManager = new DividerWindowManager(this.mSystemWindows);
        this.mDisplayController.addDisplayWindowListener(this);
        this.mForcedResizableController = new ForcedResizableInfoActivityController(this.mContext, this);
        putComponent(Divider.class, this);
        KeyguardMonitorImpl keyguardMonitorImpl = (KeyguardMonitorImpl) Dependency.get(KeyguardMonitor.class);
        this.mKeyguardMonitor = keyguardMonitorImpl;
        keyguardMonitorImpl.addCallback((KeyguardMonitor.Callback) new KeyguardMonitor.Callback() {
            public void onKeyguardShowingChanged() {
                if (Divider.this.inSplitMode() && Divider.this.mView != null) {
                    Divider.this.mView.setHidden(Divider.this.mKeyguardMonitor.isShowing());
                }
            }
        });
    }

    public void onDisplayAdded(int i) {
        if (i == 0) {
            this.mSplitLayout = new SplitDisplayLayout(this.mDisplayController.getDisplayContext(i), this.mDisplayController.getDisplayLayout(i), this.mSplits);
            this.mImeController.addPositionProcessor(this.mImePositionProcessor);
            this.mDisplayController.addDisplayChangingController(this.mRotationController);
            if (!ActivityTaskManager.supportsSplitScreenMultiWindow(this.mContext)) {
                removeDivider();
                return;
            }
            try {
                this.mSplits.init();
                WindowContainerTransaction windowContainerTransaction = new WindowContainerTransaction();
                this.mSplitLayout.resizeSplits(this.mSplitLayout.getSnapAlgorithm().getMiddleTarget().position, windowContainerTransaction);
                WindowOrganizer.applyTransaction(windowContainerTransaction);
                SystemServicesProxy.getInstance(this.mContext).registerTaskStackListener(this.mActivityRestartListener);
            } catch (Exception e) {
                Slog.e("Divider", "Failed to register docked stack listener", e);
                removeDivider();
            }
        }
    }

    public void onDisplayConfigurationChanged(int i, Configuration configuration) {
        if (i == 0 && this.mSplits.isSplitScreenSupported()) {
            SplitDisplayLayout splitDisplayLayout = new SplitDisplayLayout(this.mDisplayController.getDisplayContext(i), this.mDisplayController.getDisplayLayout(i), this.mSplits);
            this.mSplitLayout = splitDisplayLayout;
            if (this.mRotateSplitLayout == null) {
                int i2 = splitDisplayLayout.getSnapAlgorithm().getMiddleTarget().position;
                WindowContainerTransaction windowContainerTransaction = new WindowContainerTransaction();
                this.mSplitLayout.resizeSplits(i2, windowContainerTransaction);
                WindowOrganizer.applyTransaction(windowContainerTransaction);
            } else if (splitDisplayLayout.mDisplayLayout.rotation() == this.mRotateSplitLayout.mDisplayLayout.rotation()) {
                this.mSplitLayout.mPrimary = new Rect(this.mRotateSplitLayout.mPrimary);
                this.mSplitLayout.mSecondary = new Rect(this.mRotateSplitLayout.mSecondary);
                this.mRotateSplitLayout = null;
            }
            update(configuration);
        }
    }

    /* access modifiers changed from: package-private */
    public Handler getHandler() {
        return this.mHandler;
    }

    public DividerView getView() {
        return this.mView;
    }

    public boolean isMinimized() {
        return this.mMinimized;
    }

    public boolean isHomeStackResizable() {
        return this.mHomeStackResizable;
    }

    public boolean inSplitMode() {
        DividerView dividerView = this.mView;
        return dividerView != null && dividerView.getVisibility() == 0;
    }

    private void addDivider(Configuration configuration) {
        int i;
        Context displayContext = this.mDisplayController.getDisplayContext(this.mContext.getDisplayId());
        this.mView = (DividerView) LayoutInflater.from(displayContext).inflate(R.layout.docked_stack_divider, (ViewGroup) null);
        DisplayLayout displayLayout = this.mDisplayController.getDisplayLayout(this.mContext.getDisplayId());
        this.mView.injectDependencies(this.mWindowManager, this.mDividerState, this, this.mSplits, this.mSplitLayout);
        boolean z = false;
        this.mView.setVisibility(this.mVisible ? 0 : 4);
        this.mView.setMinimizedDockStack(this.mMinimized, this.mHomeStackResizable);
        int dimensionPixelSize = displayContext.getResources().getDimensionPixelSize(17105177);
        if (configuration.orientation == 2) {
            z = true;
        }
        if (z) {
            i = dimensionPixelSize;
        } else {
            i = displayLayout.width();
        }
        if (z) {
            dimensionPixelSize = displayLayout.height();
        }
        this.mWindowManager.add(this.mView, i, dimensionPixelSize, this.mContext.getDisplayId());
    }

    /* access modifiers changed from: private */
    public void removeDivider() {
        DividerView dividerView = this.mView;
        if (dividerView != null) {
            dividerView.onDividerRemoved();
        }
        this.mWindowManager.remove();
    }

    private void update(Configuration configuration) {
        DividerView dividerView = this.mView;
        boolean z = dividerView != null && dividerView.isHidden();
        removeDivider();
        addDivider(configuration);
        DividerView dividerView2 = this.mView;
        if (dividerView2 != null) {
            if (this.mMinimized) {
                dividerView2.setMinimizedDockStack(true, this.mHomeStackResizable);
                updateTouchable();
            }
            this.mView.setHidden(z);
        }
    }

    /* access modifiers changed from: package-private */
    public void onTaskVanished() {
        this.mHandler.post(new Runnable() {
            public final void run() {
                Divider.this.removeDivider();
            }
        });
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$onTasksReady$1 */
    public /* synthetic */ void lambda$onTasksReady$1$Divider() {
        update(this.mDisplayController.getDisplayContext(this.mContext.getDisplayId()).getResources().getConfiguration());
    }

    /* access modifiers changed from: package-private */
    public void onTasksReady() {
        this.mHandler.post(new Runnable() {
            public final void run() {
                Divider.this.lambda$onTasksReady$1$Divider();
            }
        });
    }

    /* access modifiers changed from: package-private */
    public void updateVisibility(boolean z) {
        if (this.mVisible != z) {
            this.mVisible = z;
            this.mView.setVisibility(z ? 0 : 4);
            if (z) {
                this.mView.enterSplitMode(this.mHomeStackResizable);
                this.mView.setMinimizedDockStack(this.mMinimized, this.mHomeStackResizable);
            } else {
                this.mView.exitSplitMode();
                this.mView.setMinimizedDockStack(false, this.mHomeStackResizable);
            }
            synchronized (this.mDockedStackExistsListeners) {
                this.mDockedStackExistsListeners.removeIf(new Predicate(z) {
                    public final /* synthetic */ boolean f$0;

                    {
                        this.f$0 = r1;
                    }

                    public final boolean test(Object obj) {
                        return Divider.lambda$updateVisibility$2(this.f$0, (WeakReference) obj);
                    }
                });
            }
        }
    }

    static /* synthetic */ boolean lambda$updateVisibility$2(boolean z, WeakReference weakReference) {
        Consumer consumer = (Consumer) weakReference.get();
        if (consumer != null) {
            consumer.accept(Boolean.valueOf(z));
        }
        return consumer == null;
    }

    /* access modifiers changed from: package-private */
    public void onSplitDismissed() {
        this.mMinimized = false;
        updateVisibility(false);
    }

    private void setHomeMinimized(boolean z, boolean z2) {
        WindowContainerTransaction windowContainerTransaction = new WindowContainerTransaction();
        boolean z3 = true;
        int i = 0;
        boolean z4 = this.mMinimized != z;
        if (z4) {
            this.mMinimized = z;
        }
        windowContainerTransaction.setFocusable(this.mSplits.mPrimary.token, !this.mMinimized);
        if (this.mHomeStackResizable == z2) {
            z3 = false;
        }
        if (z3) {
            this.mHomeStackResizable = z2;
            if (inSplitMode()) {
                WindowManagerProxy.applyHomeTasksMinimized(this.mSplitLayout, this.mSplits.mSecondary.token, windowContainerTransaction);
            }
        }
        DividerView dividerView = this.mView;
        if (dividerView != null) {
            if (dividerView.getDisplay() != null) {
                i = this.mView.getDisplay().getDisplayId();
            }
            if (this.mMinimized) {
                this.mImePositionProcessor.pause(i);
            }
            if (z4 || z3) {
                this.mView.setMinimizedDockStack(z, getAnimDuration(), z2);
            }
            if (!this.mMinimized) {
                this.mImePositionProcessor.resume(i);
            }
        }
        updateTouchable();
        WindowOrganizer.applyTransaction(windowContainerTransaction);
        DockedStackExistsChangedListener dockedStackExistsChangedListener = this.mDockedStackExistsChangedListener;
        if (dockedStackExistsChangedListener != null) {
            dockedStackExistsChangedListener.onDockedStackMinimizedChanged(z);
        }
    }

    /* access modifiers changed from: package-private */
    public void setAdjustedForIme(boolean z) {
        if (this.mAdjustedForIme != z) {
            this.mAdjustedForIme = z;
            updateTouchable();
        }
    }

    private void updateTouchable() {
        this.mWindowManager.setTouchable((this.mHomeStackResizable || !this.mMinimized) && !this.mAdjustedForIme);
    }

    public void onRecentsDrawn() {
        DividerView dividerView = this.mView;
        if (dividerView != null) {
            dividerView.onRecentsDrawn();
        }
    }

    public void onUndockingTask(boolean z) {
        DividerView dividerView = this.mView;
        if (dividerView != null) {
            dividerView.onUndockingTask();
        }
    }

    public void onDockedFirstAnimationFrame() {
        DividerView dividerView = this.mView;
        if (dividerView != null) {
            dividerView.onDockedFirstAnimationFrame();
        }
    }

    public void onDockedTopTask(int i, Rect rect) {
        DividerView dividerView = this.mView;
        if (dividerView != null) {
            dividerView.onDockedTopTask();
        }
    }

    public void onDraggingStart() {
        this.mForcedResizableController.onDraggingStart();
    }

    public void onDraggingEnd() {
        this.mForcedResizableController.onDraggingEnd();
    }

    public void dump(FileDescriptor fileDescriptor, PrintWriter printWriter, String[] strArr) {
        printWriter.print("  mVisible=");
        printWriter.println(this.mVisible);
        printWriter.print("  mMinimized=");
        printWriter.println(this.mMinimized);
        printWriter.print("  mAdjustedForIme=");
        printWriter.println(this.mAdjustedForIme);
    }

    /* access modifiers changed from: package-private */
    public long getAnimDuration() {
        return (long) (Settings.Global.getFloat(this.mContext.getContentResolver(), "transition_animation_scale", this.mContext.getResources().getFloat(17105053)) * 336.0f);
    }

    public void registerInSplitScreenListener(Consumer<Boolean> consumer) {
        consumer.accept(Boolean.valueOf(inSplitMode()));
        synchronized (this.mDockedStackExistsListeners) {
            this.mDockedStackExistsListeners.add(new WeakReference(consumer));
        }
    }

    /* access modifiers changed from: package-private */
    public void startEnterSplit() {
        this.mHomeStackResizable = WindowManagerProxy.applyEnterSplit(this.mSplits, this.mSplitLayout);
    }

    /* access modifiers changed from: package-private */
    public void ensureMinimizedSplit() {
        setHomeMinimized(true, Utils.isResizable(this.mSplits.mSecondary));
        if (!inSplitMode()) {
            updateVisibility(true);
        }
    }

    /* access modifiers changed from: package-private */
    public void ensureNormalSplit() {
        setHomeMinimized(false, this.mHomeStackResizable);
        if (!inSplitMode()) {
            updateVisibility(true);
        }
    }

    public boolean isExists() {
        return inSplitMode();
    }
}
