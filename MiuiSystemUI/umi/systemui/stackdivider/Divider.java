package com.android.systemui.stackdivider;

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
import android.window.WindowContainerToken;
import android.window.WindowContainerTransaction;
import android.window.WindowOrganizer;
import com.android.systemui.C0012R$dimen;
import com.android.systemui.C0017R$layout;
import com.android.systemui.SystemUI;
import com.android.systemui.TransactionPool;
import com.android.systemui.recents.Recents;
import com.android.systemui.shared.system.ActivityManagerWrapper;
import com.android.systemui.shared.system.TaskStackChangeListener;
import com.android.systemui.stackdivider.DividerView;
import com.android.systemui.stackdivider.SyncTransactionQueue;
import com.android.systemui.statusbar.policy.KeyguardStateController;
import com.android.systemui.wm.DisplayChangeController;
import com.android.systemui.wm.DisplayController;
import com.android.systemui.wm.DisplayImeController;
import com.android.systemui.wm.DisplayLayout;
import com.android.systemui.wm.SystemWindows;
import dagger.Lazy;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class Divider extends SystemUI implements DividerView.DividerCallbacks, DisplayController.OnDisplaysChangedListener {
    private TaskStackChangeListener mActivityRestartListener = new TaskStackChangeListener() {
        /* class com.android.systemui.stackdivider.Divider.AnonymousClass1 */

        @Override // com.android.systemui.shared.system.TaskStackChangeListener
        public void onActivityRestartAttempt(ActivityManager.RunningTaskInfo runningTaskInfo, boolean z, boolean z2, boolean z3) {
            if (z3 && runningTaskInfo.configuration.windowConfiguration.getWindowingMode() == 3 && Divider.this.mSplits.isSplitScreenSupported() && Divider.this.isMinimized()) {
                Divider.this.onUndockingTask();
            }
        }
    };
    private boolean mAdjustedForIme = false;
    private DisplayController mDisplayController;
    private final DividerState mDividerState = new DividerState();
    private final ArrayList<WeakReference<Consumer<Boolean>>> mDockedStackExistsListeners = new ArrayList<>();
    private ForcedResizableInfoActivityController mForcedResizableController;
    private Handler mHandler;
    private boolean mHomeStackResizable = false;
    private DisplayImeController mImeController;
    private final DividerImeController mImePositionProcessor;
    private KeyguardStateController mKeyguardStateController;
    private boolean mMinimized = false;
    private final Optional<Lazy<Recents>> mRecentsOptionalLazy;
    private SplitDisplayLayout mRotateSplitLayout;
    private DisplayChangeController.OnDisplayChangingListener mRotationController = new DisplayChangeController.OnDisplayChangingListener() {
        /* class com.android.systemui.stackdivider.$$Lambda$Divider$0WHTGcDpweqOnqzkpJAQb7brKYs */

        @Override // com.android.systemui.wm.DisplayChangeController.OnDisplayChangingListener
        public final void onRotateDisplay(int i, int i2, int i3, WindowContainerTransaction windowContainerTransaction) {
            Divider.this.lambda$new$0$Divider(i, i2, i3, windowContainerTransaction);
        }
    };
    private SplitDisplayLayout mSplitLayout;
    private SplitScreenTaskOrganizer mSplits = new SplitScreenTaskOrganizer(this);
    private SystemWindows mSystemWindows;
    final TransactionPool mTransactionPool;
    private DividerView mView;
    private boolean mVisible = false;
    private DividerWindowManager mWindowManager;
    private WindowManagerProxy mWindowManagerProxy;

    /* access modifiers changed from: private */
    /* renamed from: lambda$new$0 */
    public /* synthetic */ void lambda$new$0$Divider(int i, int i2, int i3, WindowContainerTransaction windowContainerTransaction) {
        int i4;
        if (this.mSplits.isSplitScreenSupported() && this.mWindowManagerProxy != null) {
            WindowContainerTransaction windowContainerTransaction2 = new WindowContainerTransaction();
            SplitDisplayLayout splitDisplayLayout = new SplitDisplayLayout(this.mContext, new DisplayLayout(this.mDisplayController.getDisplayLayout(i)), this.mSplits);
            splitDisplayLayout.rotateTo(i3);
            this.mRotateSplitLayout = splitDisplayLayout;
            if (!isDividerVisible()) {
                i4 = splitDisplayLayout.getSnapAlgorithm().getMiddleTarget().position;
            } else if (this.mMinimized) {
                i4 = this.mView.mSnapTargetBeforeMinimized.position;
            } else {
                i4 = this.mView.getCurrentPosition();
            }
            DividerSnapAlgorithm snapAlgorithm = splitDisplayLayout.getSnapAlgorithm();
            splitDisplayLayout.resizeSplits(DividerInjector.updateSnapTargetIfNeed(snapAlgorithm.calculateNonDismissingSnapTarget(i4), this.mContext, snapAlgorithm).position, windowContainerTransaction2);
            if (isSplitActive() && this.mHomeStackResizable) {
                WindowManagerProxy.applyHomeTasksMinimized(splitDisplayLayout, this.mSplits.mSecondary.token, windowContainerTransaction2);
            }
            if (this.mWindowManagerProxy.queueSyncTransactionIfWaiting(windowContainerTransaction2)) {
                Slog.w("Divider", "Screen rotated while other operations were pending, this may result in some graphical artifacts.");
            } else {
                windowContainerTransaction.merge(windowContainerTransaction2, true);
            }
        }
    }

    public Divider(Context context, Optional<Lazy<Recents>> optional, DisplayController displayController, SystemWindows systemWindows, DisplayImeController displayImeController, Handler handler, KeyguardStateController keyguardStateController, TransactionPool transactionPool) {
        super(context);
        this.mDisplayController = displayController;
        this.mSystemWindows = systemWindows;
        this.mImeController = displayImeController;
        this.mHandler = handler;
        this.mKeyguardStateController = keyguardStateController;
        this.mRecentsOptionalLazy = optional;
        this.mForcedResizableController = new ForcedResizableInfoActivityController(context, this);
        this.mTransactionPool = transactionPool;
        this.mWindowManagerProxy = new WindowManagerProxy(this.mTransactionPool, this.mHandler);
        this.mImePositionProcessor = new DividerImeController(this.mSplits, this.mTransactionPool, this.mHandler);
    }

    @Override // com.android.systemui.SystemUI
    public void start() {
        this.mWindowManager = new DividerWindowManager(this.mSystemWindows);
        this.mDisplayController.addDisplayWindowListener(this);
        this.mKeyguardStateController.addCallback(new KeyguardStateController.Callback() {
            /* class com.android.systemui.stackdivider.Divider.AnonymousClass2 */

            @Override // com.android.systemui.statusbar.policy.KeyguardStateController.Callback
            public void onKeyguardFadingAwayChanged() {
            }

            @Override // com.android.systemui.statusbar.policy.KeyguardStateController.Callback
            public void onUnlockedChanged() {
            }

            @Override // com.android.systemui.statusbar.policy.KeyguardStateController.Callback
            public void onKeyguardShowingChanged() {
                if (Divider.this.isSplitActive() && Divider.this.mView != null) {
                    Divider.this.mView.setHidden(Divider.this.mKeyguardStateController.isShowing());
                    if (!Divider.this.mKeyguardStateController.isShowing()) {
                        Divider.this.mImePositionProcessor.updateAdjustForIme();
                    }
                }
            }
        });
    }

    @Override // com.android.systemui.wm.DisplayController.OnDisplaysChangedListener
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
                ActivityManagerWrapper.getInstance().registerTaskStackListener(this.mActivityRestartListener);
                DividerInjector.updateSplitScreenFroceNotResizePkgList(this.mContext);
            } catch (Exception e) {
                Slog.e("Divider", "Failed to register docked stack listener", e);
                removeDivider();
            }
        }
    }

    @Override // com.android.systemui.wm.DisplayController.OnDisplaysChangedListener
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
            if (isSplitActive()) {
                update(configuration);
            }
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

    public boolean isDividerVisible() {
        DividerView dividerView = this.mView;
        return dividerView != null && dividerView.getVisibility() == 0;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean isSplitActive() {
        ActivityManager.RunningTaskInfo runningTaskInfo;
        SplitScreenTaskOrganizer splitScreenTaskOrganizer = this.mSplits;
        ActivityManager.RunningTaskInfo runningTaskInfo2 = splitScreenTaskOrganizer.mPrimary;
        return (runningTaskInfo2 == null || (runningTaskInfo = splitScreenTaskOrganizer.mSecondary) == null || (runningTaskInfo2.topActivityType == 0 && runningTaskInfo.topActivityType == 0)) ? false : true;
    }

    private void addDivider(Configuration configuration) {
        int i;
        Context displayContext = this.mDisplayController.getDisplayContext(this.mContext.getDisplayId());
        this.mView = (DividerView) LayoutInflater.from(displayContext).inflate(C0017R$layout.docked_stack_divider, (ViewGroup) null);
        DisplayLayout displayLayout = this.mDisplayController.getDisplayLayout(this.mContext.getDisplayId());
        this.mView.injectDependencies(this.mWindowManager, this.mDividerState, this, this.mSplits, this.mSplitLayout, this.mImePositionProcessor, this.mWindowManagerProxy);
        boolean z = false;
        this.mView.setVisibility(this.mVisible ? 0 : 4);
        this.mView.setMinimizedDockStack(this.mMinimized, this.mHomeStackResizable, (SurfaceControl.Transaction) null);
        int dimensionPixelSize = displayContext.getResources().getDimensionPixelSize(C0012R$dimen.docked_stack_divider_thickness);
        if (configuration.orientation == 2) {
            z = true;
        }
        boolean orientationIfNeed = DividerInjector.getOrientationIfNeed(configuration, z);
        if (orientationIfNeed) {
            i = dimensionPixelSize;
        } else {
            i = displayLayout.width();
        }
        if (orientationIfNeed) {
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
        boolean z = this.mView != null && this.mKeyguardStateController.isShowing();
        removeDivider();
        addDivider(configuration);
        if (this.mMinimized) {
            this.mView.setMinimizedDockStack(true, this.mHomeStackResizable, (SurfaceControl.Transaction) null);
            updateTouchable();
        }
        this.mView.setHidden(z);
    }

    /* access modifiers changed from: package-private */
    public void onTaskVanished() {
        this.mHandler.post(new Runnable() {
            /* class com.android.systemui.stackdivider.$$Lambda$Divider$JQC7s2DcACmP1thtllRZ30N2PIw */

            public final void run() {
                Divider.this.removeDivider();
            }
        });
    }

    private void updateVisibility(boolean z) {
        if (this.mVisible != z) {
            this.mVisible = z;
            this.mView.setVisibility(z ? 0 : 4);
            if (z) {
                this.mView.enterSplitMode(this.mHomeStackResizable);
                this.mWindowManagerProxy.runInSync(new SyncTransactionQueue.TransactionRunnable() {
                    /* class com.android.systemui.stackdivider.$$Lambda$Divider$PFK1wU0r4FZbucBwkJAZvajxzCU */

                    @Override // com.android.systemui.stackdivider.SyncTransactionQueue.TransactionRunnable
                    public final void runWithTransaction(SurfaceControl.Transaction transaction) {
                        Divider.this.lambda$updateVisibility$1$Divider(transaction);
                    }
                });
            } else {
                this.mView.exitSplitMode();
                this.mWindowManagerProxy.runInSync(new SyncTransactionQueue.TransactionRunnable() {
                    /* class com.android.systemui.stackdivider.$$Lambda$Divider$VIerqJCgwVozy2YX14uply8NzWc */

                    @Override // com.android.systemui.stackdivider.SyncTransactionQueue.TransactionRunnable
                    public final void runWithTransaction(SurfaceControl.Transaction transaction) {
                        Divider.this.lambda$updateVisibility$2$Divider(transaction);
                    }
                });
            }
            synchronized (this.mDockedStackExistsListeners) {
                this.mDockedStackExistsListeners.removeIf(new Predicate(z) {
                    /* class com.android.systemui.stackdivider.$$Lambda$Divider$fg71CWqbbHGuQEcI_sx8tNWcJg */
                    public final /* synthetic */ boolean f$0;

                    {
                        this.f$0 = r1;
                    }

                    @Override // java.util.function.Predicate
                    public final boolean test(Object obj) {
                        return Divider.lambda$updateVisibility$3(this.f$0, (WeakReference) obj);
                    }
                });
            }
        }
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$updateVisibility$1 */
    public /* synthetic */ void lambda$updateVisibility$1$Divider(SurfaceControl.Transaction transaction) {
        this.mView.setMinimizedDockStack(this.mMinimized, this.mHomeStackResizable, transaction);
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$updateVisibility$2 */
    public /* synthetic */ void lambda$updateVisibility$2$Divider(SurfaceControl.Transaction transaction) {
        this.mView.setMinimizedDockStack(false, this.mHomeStackResizable, transaction);
    }

    static /* synthetic */ boolean lambda$updateVisibility$3(boolean z, WeakReference weakReference) {
        Consumer consumer = (Consumer) weakReference.get();
        if (consumer != null) {
            consumer.accept(Boolean.valueOf(z));
        }
        return consumer == null;
    }

    public void setMinimized(boolean z) {
        this.mHandler.post(new Runnable(z) {
            /* class com.android.systemui.stackdivider.$$Lambda$Divider$qaeq4YZm8Jheg2TUOpTbHIkGx8 */
            public final /* synthetic */ boolean f$1;

            {
                this.f$1 = r2;
            }

            public final void run() {
                Divider.this.lambda$setMinimized$4$Divider(this.f$1);
            }
        });
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$setMinimized$4 */
    public /* synthetic */ void lambda$setMinimized$4$Divider(boolean z) {
        if (this.mVisible) {
            setHomeMinimized(z, this.mHomeStackResizable);
        }
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
        boolean z5 = this.mHomeStackResizable != z2;
        if (z5) {
            this.mHomeStackResizable = z2;
            if (isDividerVisible()) {
                WindowManagerProxy.applyHomeTasksMinimized(this.mSplitLayout, this.mSplits.mSecondary.token, windowContainerTransaction);
                z3 = false;
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
            if (z4 || z5) {
                this.mView.setMinimizedDockStack(z, getAnimDuration(), z2);
            }
            if (!this.mMinimized) {
                this.mImePositionProcessor.resume(i);
            }
        }
        updateTouchable();
        if (!z3) {
            this.mWindowManagerProxy.applySyncTransaction(windowContainerTransaction);
        } else if (!this.mSplits.mDivider.getWmProxy().queueSyncTransactionIfWaiting(windowContainerTransaction)) {
            WindowOrganizer.applyTransaction(windowContainerTransaction);
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
        this.mWindowManager.setTouchable(!this.mAdjustedForIme);
    }

    public void onRecentsDrawn() {
        DividerView dividerView = this.mView;
        if (dividerView != null) {
            dividerView.onRecentsDrawn();
        }
    }

    public void onUndockingTask() {
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

    public void onDockedTopTask() {
        DividerView dividerView = this.mView;
        if (dividerView != null) {
            dividerView.onDockedTopTask();
        }
    }

    public void onAppTransitionFinished() {
        if (this.mView != null) {
            this.mForcedResizableController.onAppTransitionFinished();
        }
    }

    @Override // com.android.systemui.stackdivider.DividerView.DividerCallbacks
    public void onDraggingStart() {
        this.mForcedResizableController.onDraggingStart();
    }

    @Override // com.android.systemui.stackdivider.DividerView.DividerCallbacks
    public void onDraggingEnd() {
        this.mForcedResizableController.onDraggingEnd();
    }

    @Override // com.android.systemui.stackdivider.DividerView.DividerCallbacks
    public void growRecents() {
        this.mRecentsOptionalLazy.ifPresent($$Lambda$Divider$kUReJvdE1s1BPD9HklZGjPX7dM.INSTANCE);
    }

    @Override // com.android.systemui.SystemUI, com.android.systemui.Dumpable
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
        consumer.accept(Boolean.valueOf(isDividerVisible()));
        synchronized (this.mDockedStackExistsListeners) {
            this.mDockedStackExistsListeners.add(new WeakReference<>(consumer));
        }
    }

    /* access modifiers changed from: package-private */
    public void startEnterSplit() {
        update(this.mDisplayController.getDisplayContext(this.mContext.getDisplayId()).getResources().getConfiguration());
        this.mHomeStackResizable = this.mWindowManagerProxy.applyEnterSplit(this.mSplits, this.mSplitLayout);
    }

    /* access modifiers changed from: package-private */
    public void startDismissSplit() {
        this.mWindowManagerProxy.lambda$dismissOrMaximizeDocked$0(this.mSplits, this.mSplitLayout, true);
        updateVisibility(false);
        this.mMinimized = false;
        removeDivider();
        this.mImePositionProcessor.reset();
    }

    /* access modifiers changed from: package-private */
    public void ensureMinimizedSplit() {
        setHomeMinimized(true, this.mHomeStackResizable);
        if (this.mView != null && !isDividerVisible()) {
            updateVisibility(true);
        }
    }

    /* access modifiers changed from: package-private */
    public void ensureNormalSplit() {
        setHomeMinimized(false, this.mHomeStackResizable);
        if (this.mView != null && !isDividerVisible()) {
            updateVisibility(true);
        }
    }

    /* access modifiers changed from: package-private */
    public SplitDisplayLayout getSplitLayout() {
        return this.mSplitLayout;
    }

    /* access modifiers changed from: package-private */
    public WindowManagerProxy getWmProxy() {
        return this.mWindowManagerProxy;
    }

    public WindowContainerToken getSecondaryRoot() {
        ActivityManager.RunningTaskInfo runningTaskInfo;
        SplitScreenTaskOrganizer splitScreenTaskOrganizer = this.mSplits;
        if (splitScreenTaskOrganizer == null || (runningTaskInfo = splitScreenTaskOrganizer.mSecondary) == null) {
            return null;
        }
        return runningTaskInfo.token;
    }
}
