package com.android.systemui.pip.phone;

import android.content.ComponentName;
import android.content.Context;
import android.graphics.PointF;
import android.graphics.Rect;
import android.util.ArrayMap;
import android.view.Choreographer;
import com.android.internal.graphics.SfVsyncFrameCallbackProvider;
import com.android.systemui.pip.PipSnapAlgorithm;
import com.android.systemui.pip.PipTaskOrganizer;
import com.android.systemui.pip.phone.PipAppOpsListener;
import com.android.systemui.util.FloatingContentCoordinator;
import com.android.systemui.util.animation.FloatProperties;
import com.android.systemui.util.animation.PhysicsAnimator;
import com.android.systemui.util.magnetictarget.MagnetizedObject;
import java.io.PrintWriter;
import java.util.Objects;
import java.util.function.Consumer;
import kotlin.Unit;
import kotlin.jvm.functions.Function0;

public class PipMotionHelper implements PipAppOpsListener.Callback, FloatingContentCoordinator.FloatingContent {
    private final Rect mAnimatingToBounds = new Rect();
    private final Rect mBounds = new Rect();
    private final PhysicsAnimator.SpringConfig mConflictResolutionSpringConfig = new PhysicsAnimator.SpringConfig(200.0f, 0.75f);
    private final Context mContext;
    private boolean mDismissalPending;
    private PhysicsAnimator.FlingConfig mFlingConfigX;
    private PhysicsAnimator.FlingConfig mFlingConfigY;
    private final Rect mFloatingAllowedArea = new Rect();
    private FloatingContentCoordinator mFloatingContentCoordinator;
    private MagnetizedObject<Rect> mMagnetizedPip;
    private PipMenuActivityController mMenuController;
    private final Rect mMovementBounds = new Rect();
    private final PipTaskOrganizer mPipTaskOrganizer;
    private final PipTaskOrganizer.PipTransitionCallback mPipTransitionCallback;
    private Runnable mPostPipTransitionCallback;
    private final PhysicsAnimator.UpdateListener<Rect> mResizePipUpdateListener;
    private final Choreographer.FrameCallback mResizePipVsyncCallback;
    private final SfVsyncFrameCallbackProvider mSfVsyncFrameProvider = new SfVsyncFrameCallbackProvider();
    private PipSnapAlgorithm mSnapAlgorithm;
    private final PhysicsAnimator.SpringConfig mSpringConfig = new PhysicsAnimator.SpringConfig(1500.0f, 0.75f);
    private boolean mSpringingToTouch;
    private final Rect mTemporaryBounds = new Rect();
    private PhysicsAnimator<Rect> mTemporaryBoundsPhysicsAnimator = PhysicsAnimator.getInstance(this.mTemporaryBounds);
    private final Consumer<Rect> mUpdateBoundsCallback;

    public PipMotionHelper(Context context, PipTaskOrganizer pipTaskOrganizer, PipMenuActivityController pipMenuActivityController, PipSnapAlgorithm pipSnapAlgorithm, FloatingContentCoordinator floatingContentCoordinator) {
        Rect rect = this.mBounds;
        Objects.requireNonNull(rect);
        this.mUpdateBoundsCallback = new Consumer(rect) {
            /* class com.android.systemui.pip.phone.$$Lambda$9ryw0tgRGCMDitW4U_PfPc0I9v4 */
            public final /* synthetic */ Rect f$0;

            {
                this.f$0 = r1;
            }

            @Override // java.util.function.Consumer
            public final void accept(Object obj) {
                this.f$0.set((Rect) obj);
            }
        };
        this.mSpringingToTouch = false;
        this.mDismissalPending = false;
        AnonymousClass1 r0 = new PipTaskOrganizer.PipTransitionCallback() {
            /* class com.android.systemui.pip.phone.PipMotionHelper.AnonymousClass1 */

            @Override // com.android.systemui.pip.PipTaskOrganizer.PipTransitionCallback
            public void onPipTransitionCanceled(ComponentName componentName, int i) {
            }

            @Override // com.android.systemui.pip.PipTaskOrganizer.PipTransitionCallback
            public void onPipTransitionStarted(ComponentName componentName, int i) {
            }

            @Override // com.android.systemui.pip.PipTaskOrganizer.PipTransitionCallback
            public void onPipTransitionFinished(ComponentName componentName, int i) {
                if (PipMotionHelper.this.mPostPipTransitionCallback != null) {
                    PipMotionHelper.this.mPostPipTransitionCallback.run();
                    PipMotionHelper.this.mPostPipTransitionCallback = null;
                }
            }
        };
        this.mPipTransitionCallback = r0;
        this.mContext = context;
        this.mPipTaskOrganizer = pipTaskOrganizer;
        this.mMenuController = pipMenuActivityController;
        this.mSnapAlgorithm = pipSnapAlgorithm;
        this.mFloatingContentCoordinator = floatingContentCoordinator;
        pipTaskOrganizer.registerPipTransitionCallback(r0);
        this.mResizePipVsyncCallback = new Choreographer.FrameCallback() {
            /* class com.android.systemui.pip.phone.$$Lambda$PipMotionHelper$C_POQP400vMSArwTIak6Td06o */

            public final void doFrame(long j) {
                PipMotionHelper.this.lambda$new$0$PipMotionHelper(j);
            }
        };
        this.mResizePipUpdateListener = new PhysicsAnimator.UpdateListener() {
            /* class com.android.systemui.pip.phone.$$Lambda$PipMotionHelper$i9NRbFmJlv_VucqYs_q6_ejqw */

            @Override // com.android.systemui.util.animation.PhysicsAnimator.UpdateListener
            public final void onAnimationUpdateForProperty(Object obj, ArrayMap arrayMap) {
                PipMotionHelper.this.lambda$new$1$PipMotionHelper((Rect) obj, arrayMap);
            }
        };
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$new$0 */
    public /* synthetic */ void lambda$new$0$PipMotionHelper(long j) {
        if (!this.mTemporaryBounds.isEmpty()) {
            this.mPipTaskOrganizer.scheduleUserResizePip(this.mBounds, this.mTemporaryBounds, null);
        }
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$new$1 */
    public /* synthetic */ void lambda$new$1$PipMotionHelper(Rect rect, ArrayMap arrayMap) {
        this.mSfVsyncFrameProvider.postFrameCallback(this.mResizePipVsyncCallback);
    }

    @Override // com.android.systemui.util.FloatingContentCoordinator.FloatingContent
    public Rect getFloatingBoundsOnScreen() {
        return !this.mAnimatingToBounds.isEmpty() ? this.mAnimatingToBounds : this.mBounds;
    }

    @Override // com.android.systemui.util.FloatingContentCoordinator.FloatingContent
    public Rect getAllowedFloatingBoundsRegion() {
        return this.mFloatingAllowedArea;
    }

    @Override // com.android.systemui.util.FloatingContentCoordinator.FloatingContent
    public void moveToBounds(Rect rect) {
        animateToBounds(rect, this.mConflictResolutionSpringConfig);
    }

    /* access modifiers changed from: package-private */
    public void synchronizePinnedStackBounds() {
        cancelAnimations();
        this.mBounds.set(this.mPipTaskOrganizer.getLastReportedBounds());
        this.mTemporaryBounds.setEmpty();
        if (this.mPipTaskOrganizer.isInPip()) {
            this.mFloatingContentCoordinator.onContentMoved(this);
        }
    }

    /* access modifiers changed from: package-private */
    public void movePip(Rect rect) {
        movePip(rect, false);
    }

    /* access modifiers changed from: package-private */
    public void movePip(Rect rect, boolean z) {
        if (!z) {
            this.mFloatingContentCoordinator.onContentMoved(this);
        }
        if (!this.mSpringingToTouch) {
            cancelAnimations();
            if (!z) {
                resizePipUnchecked(rect);
                this.mBounds.set(rect);
                return;
            }
            this.mTemporaryBounds.set(rect);
            this.mPipTaskOrganizer.scheduleUserResizePip(this.mBounds, this.mTemporaryBounds, null);
            return;
        }
        PhysicsAnimator<Rect> physicsAnimator = this.mTemporaryBoundsPhysicsAnimator;
        physicsAnimator.spring(FloatProperties.RECT_WIDTH, (float) this.mBounds.width(), this.mSpringConfig);
        physicsAnimator.spring(FloatProperties.RECT_HEIGHT, (float) this.mBounds.height(), this.mSpringConfig);
        physicsAnimator.spring(FloatProperties.RECT_X, (float) rect.left, this.mSpringConfig);
        physicsAnimator.spring(FloatProperties.RECT_Y, (float) rect.top, this.mSpringConfig);
        startBoundsAnimator((float) rect.left, (float) rect.top, false);
    }

    /* access modifiers changed from: package-private */
    public void animateIntoDismissTarget(MagnetizedObject.MagneticTarget magneticTarget, float f, float f2, boolean z, Function0<Unit> function0) {
        PointF centerOnScreen = magneticTarget.getCenterOnScreen();
        float width = (float) (this.mBounds.width() / 2);
        float height = (float) (this.mBounds.height() / 2);
        float f3 = centerOnScreen.x - (width / 2.0f);
        float f4 = centerOnScreen.y - (height / 2.0f);
        if (this.mTemporaryBounds.isEmpty()) {
            this.mTemporaryBounds.set(this.mBounds);
        }
        PhysicsAnimator<Rect> physicsAnimator = this.mTemporaryBoundsPhysicsAnimator;
        physicsAnimator.spring(FloatProperties.RECT_X, f3, f, this.mSpringConfig);
        physicsAnimator.spring(FloatProperties.RECT_Y, f4, f2, this.mSpringConfig);
        physicsAnimator.spring(FloatProperties.RECT_WIDTH, width, this.mSpringConfig);
        physicsAnimator.spring(FloatProperties.RECT_HEIGHT, height, this.mSpringConfig);
        physicsAnimator.withEndActions(function0);
        startBoundsAnimator(f3, f4, false);
    }

    /* access modifiers changed from: package-private */
    public void setSpringingToTouch(boolean z) {
        this.mSpringingToTouch = z;
    }

    /* access modifiers changed from: package-private */
    public void expandPipToFullscreen() {
        expandPipToFullscreen(false);
    }

    /* access modifiers changed from: package-private */
    public void expandPipToFullscreen(boolean z) {
        cancelAnimations();
        this.mMenuController.hideMenuWithoutResize();
        this.mPipTaskOrganizer.getUpdateHandler().post(new Runnable(z) {
            /* class com.android.systemui.pip.phone.$$Lambda$PipMotionHelper$Wmfum8DiHO44sGCBrLFpc_A8U44 */
            public final /* synthetic */ boolean f$1;

            {
                this.f$1 = r2;
            }

            public final void run() {
                PipMotionHelper.this.lambda$expandPipToFullscreen$2$PipMotionHelper(this.f$1);
            }
        });
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$expandPipToFullscreen$2 */
    public /* synthetic */ void lambda$expandPipToFullscreen$2$PipMotionHelper(boolean z) {
        this.mPipTaskOrganizer.exitPip(z ? 0 : 300);
    }

    @Override // com.android.systemui.pip.phone.PipAppOpsListener.Callback
    public void dismissPip() {
        cancelAnimations();
        this.mMenuController.hideMenuWithoutResize();
        this.mPipTaskOrganizer.removePip();
    }

    /* access modifiers changed from: package-private */
    public void setCurrentMovementBounds(Rect rect) {
        this.mMovementBounds.set(rect);
        rebuildFlingConfigs();
        this.mFloatingAllowedArea.set(this.mMovementBounds);
        this.mFloatingAllowedArea.right += this.mBounds.width();
        this.mFloatingAllowedArea.bottom += this.mBounds.height();
    }

    /* access modifiers changed from: package-private */
    public Rect getBounds() {
        return this.mBounds;
    }

    /* access modifiers changed from: package-private */
    public Rect getPossiblyAnimatingBounds() {
        return this.mTemporaryBounds.isEmpty() ? this.mBounds : this.mTemporaryBounds;
    }

    /* access modifiers changed from: package-private */
    public void flingToSnapTarget(float f, float f2, Runnable runnable, Runnable runnable2) {
        this.mSpringingToTouch = false;
        PhysicsAnimator<Rect> physicsAnimator = this.mTemporaryBoundsPhysicsAnimator;
        physicsAnimator.spring(FloatProperties.RECT_WIDTH, (float) this.mBounds.width(), this.mSpringConfig);
        physicsAnimator.spring(FloatProperties.RECT_HEIGHT, (float) this.mBounds.height(), this.mSpringConfig);
        physicsAnimator.flingThenSpring(FloatProperties.RECT_X, f, this.mFlingConfigX, this.mSpringConfig, true);
        physicsAnimator.flingThenSpring(FloatProperties.RECT_Y, f2, this.mFlingConfigY, this.mSpringConfig);
        physicsAnimator.withEndActions(runnable2);
        if (runnable != null) {
            this.mTemporaryBoundsPhysicsAnimator.addUpdateListener(new PhysicsAnimator.UpdateListener(runnable) {
                /* class com.android.systemui.pip.phone.$$Lambda$PipMotionHelper$AiW2VQGjSOah3v_re0mTkvmkPLc */
                public final /* synthetic */ Runnable f$0;

                {
                    this.f$0 = r1;
                }

                @Override // com.android.systemui.util.animation.PhysicsAnimator.UpdateListener
                public final void onAnimationUpdateForProperty(Object obj, ArrayMap arrayMap) {
                    Rect rect = (Rect) obj;
                    this.f$0.run();
                }
            });
        }
        startBoundsAnimator((float) (f < 0.0f ? this.mMovementBounds.left : this.mMovementBounds.right), PhysicsAnimator.estimateFlingEndValue((float) this.mTemporaryBounds.top, f2, this.mFlingConfigY), false);
    }

    /* access modifiers changed from: package-private */
    public void animateToBounds(Rect rect, PhysicsAnimator.SpringConfig springConfig) {
        if (!this.mTemporaryBoundsPhysicsAnimator.isRunning()) {
            this.mTemporaryBounds.set(this.mBounds);
        }
        PhysicsAnimator<Rect> physicsAnimator = this.mTemporaryBoundsPhysicsAnimator;
        physicsAnimator.spring(FloatProperties.RECT_X, (float) rect.left, springConfig);
        physicsAnimator.spring(FloatProperties.RECT_Y, (float) rect.top, springConfig);
        startBoundsAnimator((float) rect.left, (float) rect.top, false);
    }

    /* access modifiers changed from: package-private */
    public void animateDismiss() {
        PhysicsAnimator<Rect> physicsAnimator = this.mTemporaryBoundsPhysicsAnimator;
        physicsAnimator.spring(FloatProperties.RECT_Y, (float) (this.mMovementBounds.bottom + (this.mBounds.height() * 2)), 0.0f, this.mSpringConfig);
        physicsAnimator.withEndActions(new Runnable() {
            /* class com.android.systemui.pip.phone.$$Lambda$kQFaBNknFROC8D1C4ywIb9w3JTU */

            public final void run() {
                PipMotionHelper.this.dismissPip();
            }
        });
        Rect rect = this.mBounds;
        startBoundsAnimator((float) rect.left, (float) (rect.bottom + rect.height()), true);
        this.mDismissalPending = false;
    }

    /* access modifiers changed from: package-private */
    public float animateToExpandedState(Rect rect, Rect rect2, Rect rect3, Runnable runnable) {
        float snapFraction = this.mSnapAlgorithm.getSnapFraction(new Rect(this.mBounds), rect2);
        this.mSnapAlgorithm.applySnapFraction(rect, rect3, snapFraction);
        this.mPostPipTransitionCallback = runnable;
        resizeAndAnimatePipUnchecked(rect, 250);
        return snapFraction;
    }

    /* access modifiers changed from: package-private */
    public void animateToUnexpandedState(Rect rect, float f, Rect rect2, Rect rect3, boolean z) {
        if (f < 0.0f) {
            f = this.mSnapAlgorithm.getSnapFraction(new Rect(this.mBounds), rect3);
        }
        this.mSnapAlgorithm.applySnapFraction(rect, rect2, f);
        if (z) {
            movePip(rect);
        } else {
            resizeAndAnimatePipUnchecked(rect, 250);
        }
    }

    /* access modifiers changed from: package-private */
    public void animateToOffset(Rect rect, int i) {
        cancelAnimations();
        this.mPipTaskOrganizer.scheduleOffsetPip(rect, i, 300, this.mUpdateBoundsCallback);
    }

    private void cancelAnimations() {
        this.mTemporaryBoundsPhysicsAnimator.cancel();
        this.mAnimatingToBounds.setEmpty();
        this.mSpringingToTouch = false;
    }

    private void rebuildFlingConfigs() {
        Rect rect = this.mMovementBounds;
        this.mFlingConfigX = new PhysicsAnimator.FlingConfig(2.0f, (float) rect.left, (float) rect.right);
        Rect rect2 = this.mMovementBounds;
        this.mFlingConfigY = new PhysicsAnimator.FlingConfig(2.0f, (float) rect2.top, (float) rect2.bottom);
    }

    private void startBoundsAnimator(float f, float f2, boolean z) {
        if (!this.mSpringingToTouch) {
            cancelAnimations();
        }
        int i = (int) f;
        int i2 = (int) f2;
        this.mAnimatingToBounds.set(i, i2, this.mBounds.width() + i, this.mBounds.height() + i2);
        setAnimatingToBounds(this.mAnimatingToBounds);
        if (!this.mTemporaryBoundsPhysicsAnimator.isRunning()) {
            PhysicsAnimator<Rect> physicsAnimator = this.mTemporaryBoundsPhysicsAnimator;
            physicsAnimator.addUpdateListener(this.mResizePipUpdateListener);
            physicsAnimator.withEndActions(new Runnable() {
                /* class com.android.systemui.pip.phone.$$Lambda$PipMotionHelper$D4DUlGhMZMGgehn4agfdjwJxbmQ */

                public final void run() {
                    PipMotionHelper.this.onBoundsAnimationEnd();
                }
            });
        }
        this.mTemporaryBoundsPhysicsAnimator.start();
    }

    /* access modifiers changed from: package-private */
    public void notifyDismissalPending() {
        this.mDismissalPending = true;
    }

    /* access modifiers changed from: private */
    public void onBoundsAnimationEnd() {
        if (!this.mDismissalPending && !this.mSpringingToTouch && !this.mMagnetizedPip.getObjectStuckToTarget()) {
            this.mBounds.set(this.mTemporaryBounds);
            if (!this.mDismissalPending) {
                this.mPipTaskOrganizer.scheduleFinishResizePip(this.mBounds);
            }
            this.mTemporaryBounds.setEmpty();
        }
        this.mAnimatingToBounds.setEmpty();
        this.mSpringingToTouch = false;
        this.mDismissalPending = false;
    }

    private void setAnimatingToBounds(Rect rect) {
        this.mAnimatingToBounds.set(rect);
        this.mFloatingContentCoordinator.onContentMoved(this);
    }

    private void resizePipUnchecked(Rect rect) {
        if (!rect.equals(this.mBounds)) {
            this.mPipTaskOrganizer.scheduleResizePip(rect, this.mUpdateBoundsCallback);
        }
    }

    private void resizeAndAnimatePipUnchecked(Rect rect, int i) {
        this.mPipTaskOrganizer.scheduleAnimateResizePip(rect, i, this.mUpdateBoundsCallback);
        setAnimatingToBounds(rect);
    }

    /* access modifiers changed from: package-private */
    public MagnetizedObject<Rect> getMagnetizedPip() {
        if (this.mMagnetizedPip == null) {
            this.mMagnetizedPip = new MagnetizedObject<Rect>(this, this.mContext, this.mTemporaryBounds, FloatProperties.RECT_X, FloatProperties.RECT_Y) {
                /* class com.android.systemui.pip.phone.PipMotionHelper.AnonymousClass2 */

                public float getWidth(Rect rect) {
                    return (float) rect.width();
                }

                public float getHeight(Rect rect) {
                    return (float) rect.height();
                }

                public void getLocationOnScreen(Rect rect, int[] iArr) {
                    iArr[0] = rect.left;
                    iArr[1] = rect.top;
                }
            };
        }
        return this.mMagnetizedPip;
    }

    public void dump(PrintWriter printWriter, String str) {
        printWriter.println(str + "PipMotionHelper");
        printWriter.println((str + "  ") + "mBounds=" + this.mBounds);
    }
}
