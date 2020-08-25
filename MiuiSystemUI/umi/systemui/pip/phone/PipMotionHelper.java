package com.android.systemui.pip.phone;

import android.app.IActivityTaskManager;
import android.content.ComponentName;
import android.content.Context;
import android.graphics.Rect;
import android.util.ArrayMap;
import androidx.dynamicanimation.animation.FloatPropertyCompat;
import com.android.systemui.SystemUICompat;
import com.android.systemui.pip.PipSnapAlgorithm;
import com.android.systemui.pip.PipTaskOrganizer;
import com.android.systemui.pip.phone.PipAppOpsListener;
import com.android.systemui.statusbar.FlingAnimationUtils;
import com.android.systemui.util.animation.FloatProperties;
import com.android.systemui.util.animation.PhysicsAnimator;
import com.android.systemui.util.magnetictarget.MagnetizedObject;
import java.io.PrintWriter;
import java.util.Objects;
import java.util.function.Consumer;

public class PipMotionHelper implements PipAppOpsListener.Callback {
    private final Rect mAnimatedBounds = new Rect();
    private PhysicsAnimator mAnimatedBoundsPhysicsAnimator = PhysicsAnimator.getInstance(this.mAnimatedBounds);
    private final Rect mAnimatingToBounds = new Rect();
    private final Rect mBounds = new Rect();
    private final Context mContext;
    private PhysicsAnimator.FlingConfig mFlingConfigX;
    private PhysicsAnimator.FlingConfig mFlingConfigY;
    private final Rect mFloatingAllowedArea = new Rect();
    private PipMenuActivityController mMenuController;
    private final Rect mMovementBounds = new Rect();
    private final PipTaskOrganizer mPipTaskOrganizer;
    private final PipTaskOrganizer.PipTransitionCallback mPipTransitionCallback;
    /* access modifiers changed from: private */
    public Runnable mPostPipTransitionCallback;
    final PhysicsAnimator.UpdateListener mResizePipUpdateListener = new PhysicsAnimator.UpdateListener() {
        public final void onAnimationUpdateForProperty(Object obj, ArrayMap arrayMap) {
            PipMotionHelper.this.lambda$new$0$PipMotionHelper(obj, arrayMap);
        }
    };
    private PipSnapAlgorithm mSnapAlgorithm;
    private final PhysicsAnimator.SpringConfig mSpringConfig = new PhysicsAnimator.SpringConfig(1500.0f, 0.75f);
    private boolean mSpringingToTouch;
    private final Rect mStableInsets = new Rect();
    private final Consumer<Rect> mUpdateBoundsCallback;

    /* access modifiers changed from: private */
    /* renamed from: lambda$new$0 */
    public /* synthetic */ void lambda$new$0$PipMotionHelper(Object obj, ArrayMap arrayMap) {
        resizePipUnchecked(this.mAnimatedBounds);
    }

    public PipMotionHelper(Context context, IActivityTaskManager iActivityTaskManager, PipTaskOrganizer pipTaskOrganizer, PipMenuActivityController pipMenuActivityController, PipSnapAlgorithm pipSnapAlgorithm, FlingAnimationUtils flingAnimationUtils) {
        Rect rect = this.mBounds;
        Objects.requireNonNull(rect);
        this.mUpdateBoundsCallback = new Consumer(rect) {
            public final /* synthetic */ Rect f$0;

            {
                this.f$0 = r1;
            }

            public final void accept(Object obj) {
                this.f$0.set((Rect) obj);
            }
        };
        this.mSpringingToTouch = false;
        this.mPipTransitionCallback = new PipTaskOrganizer.PipTransitionCallback() {
            public void onPipTransitionCanceled(ComponentName componentName, int i) {
            }

            public void onPipTransitionStarted(ComponentName componentName, int i) {
            }

            public void onPipTransitionFinished(ComponentName componentName, int i) {
                if (PipMotionHelper.this.mPostPipTransitionCallback != null) {
                    PipMotionHelper.this.mPostPipTransitionCallback.run();
                    Runnable unused = PipMotionHelper.this.mPostPipTransitionCallback = null;
                }
            }
        };
        this.mContext = context;
        this.mPipTaskOrganizer = pipTaskOrganizer;
        this.mMenuController = pipMenuActivityController;
        this.mSnapAlgorithm = pipSnapAlgorithm;
        onConfigurationChanged();
        this.mPipTaskOrganizer.registerPipTransitionCallback(this.mPipTransitionCallback);
    }

    /* access modifiers changed from: package-private */
    public void onConfigurationChanged() {
        this.mSnapAlgorithm.onConfigurationChanged();
        SystemUICompat.getStableInsets(this.mStableInsets);
    }

    /* access modifiers changed from: package-private */
    public void synchronizePinnedStackBounds() {
        cancelAnimations();
        this.mBounds.set(this.mPipTaskOrganizer.getLastReportedBounds());
    }

    /* access modifiers changed from: package-private */
    public void synchronizePinnedStackBoundsForTouchGesture() {
        if (this.mAnimatingToBounds.isEmpty()) {
            synchronizePinnedStackBounds();
        } else {
            this.mBounds.set(this.mAnimatedBounds);
        }
    }

    /* access modifiers changed from: package-private */
    public void movePip(Rect rect) {
        movePip(rect, false);
    }

    /* access modifiers changed from: package-private */
    public void movePip(Rect rect, boolean z) {
        if (!this.mSpringingToTouch) {
            cancelAnimations();
            resizePipUnchecked(rect);
            this.mBounds.set(rect);
            return;
        }
        PhysicsAnimator physicsAnimator = this.mAnimatedBoundsPhysicsAnimator;
        physicsAnimator.spring(FloatProperties.RECT_X, (float) rect.left, this.mSpringConfig);
        physicsAnimator.spring(FloatProperties.RECT_Y, (float) rect.top, this.mSpringConfig);
        physicsAnimator.withEndActions(new Runnable() {
            public final void run() {
                PipMotionHelper.this.lambda$movePip$1$PipMotionHelper();
            }
        });
        startBoundsAnimator((float) rect.left, (float) rect.top, false);
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$movePip$1 */
    public /* synthetic */ void lambda$movePip$1$PipMotionHelper() {
        this.mSpringingToTouch = false;
    }

    /* access modifiers changed from: package-private */
    public void setSpringingToTouch(boolean z) {
        if (z) {
            this.mAnimatedBounds.set(this.mBounds);
        }
        this.mSpringingToTouch = z;
    }

    /* access modifiers changed from: package-private */
    public void prepareForAnimation() {
        this.mAnimatedBounds.set(this.mBounds);
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
    public void flingToSnapTarget(float f, float f2, Runnable runnable, Runnable runnable2) {
        this.mAnimatedBounds.set(this.mBounds);
        PhysicsAnimator physicsAnimator = this.mAnimatedBoundsPhysicsAnimator;
        physicsAnimator.flingThenSpring(FloatProperties.RECT_X, f, this.mFlingConfigX, this.mSpringConfig, true);
        physicsAnimator.flingThenSpring(FloatProperties.RECT_Y, f2, this.mFlingConfigY, this.mSpringConfig);
        physicsAnimator.withEndActions(runnable2);
        if (runnable != null) {
            this.mAnimatedBoundsPhysicsAnimator.addUpdateListener(new PhysicsAnimator.UpdateListener(runnable) {
                public final /* synthetic */ Runnable f$0;

                {
                    this.f$0 = r1;
                }

                public final void onAnimationUpdateForProperty(Object obj, ArrayMap arrayMap) {
                    this.f$0.run();
                }
            });
        }
        startBoundsAnimator((float) (f < 0.0f ? this.mMovementBounds.left : this.mMovementBounds.right), PhysicsAnimator.estimateFlingEndValue((float) this.mBounds.top, f2, this.mFlingConfigY), false);
    }

    /* access modifiers changed from: package-private */
    public void animateDismiss() {
        this.mAnimatedBounds.set(this.mBounds);
        PhysicsAnimator physicsAnimator = this.mAnimatedBoundsPhysicsAnimator;
        FloatPropertyCompat<Rect> floatPropertyCompat = FloatProperties.RECT_Y;
        Rect rect = this.mBounds;
        physicsAnimator.spring(floatPropertyCompat, (float) (rect.bottom + rect.height()), 0.0f, this.mSpringConfig);
        physicsAnimator.withEndActions(new Runnable() {
            public final void run() {
                PipMotionHelper.this.dismissPip();
            }
        });
        Rect rect2 = this.mBounds;
        startBoundsAnimator((float) rect2.left, (float) (rect2.bottom + rect2.height()), true);
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
        this.mAnimatedBoundsPhysicsAnimator.cancel();
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
        PhysicsAnimator physicsAnimator = this.mAnimatedBoundsPhysicsAnimator;
        physicsAnimator.withEndActions(new Runnable(z) {
            public final /* synthetic */ boolean f$1;

            {
                this.f$1 = r2;
            }

            public final void run() {
                PipMotionHelper.this.lambda$startBoundsAnimator$4$PipMotionHelper(this.f$1);
            }
        });
        physicsAnimator.addUpdateListener(this.mResizePipUpdateListener);
        physicsAnimator.start();
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$startBoundsAnimator$4 */
    public /* synthetic */ void lambda$startBoundsAnimator$4$PipMotionHelper(boolean z) {
        if (!z) {
            this.mPipTaskOrganizer.scheduleFinishResizePip(this.mAnimatedBounds);
        }
        this.mAnimatingToBounds.setEmpty();
    }

    private void setAnimatingToBounds(Rect rect) {
        this.mAnimatingToBounds.set(rect);
    }

    private void resizePipUnchecked(Rect rect) {
        if (!rect.equals(this.mBounds)) {
            this.mPipTaskOrganizer.scheduleResizePip(rect, this.mUpdateBoundsCallback);
        }
    }

    private void resizeAndAnimatePipUnchecked(Rect rect, int i) {
        if (!rect.equals(this.mBounds)) {
            this.mPipTaskOrganizer.scheduleAnimateResizePip(rect, i, this.mUpdateBoundsCallback);
            setAnimatingToBounds(rect);
        }
    }

    /* access modifiers changed from: package-private */
    public MagnetizedObject getMagnetizedPip() {
        return new MagnetizedObject(this, this.mContext, this.mAnimatedBounds, FloatProperties.RECT_X, FloatProperties.RECT_Y) {
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

    public void dump(PrintWriter printWriter, String str) {
        String str2 = str + "  ";
        printWriter.println(str + "PipMotionHelper");
        printWriter.println(str2 + "mBounds=" + this.mBounds);
        printWriter.println(str2 + "mStableInsets=" + this.mStableInsets);
    }
}
