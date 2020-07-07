package com.android.systemui.pip.phone;

import android.animation.AnimationHandler;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.RectEvaluator;
import android.animation.ValueAnimator;
import android.app.ActivityManager;
import android.app.ActivityTaskManager;
import android.app.IActivityManager;
import android.app.IActivityTaskManager;
import android.content.Context;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.util.Log;
import android.view.animation.Interpolator;
import com.android.internal.graphics.SfVsyncFrameCallbackProvider;
import com.android.internal.os.SomeArgs;
import com.android.systemui.Interpolators;
import com.android.systemui.pip.PipSnapAlgorithm;
import com.android.systemui.recents.misc.ForegroundThread;
import com.android.systemui.recents.misc.SystemServicesProxy;
import com.android.systemui.statusbar.FlingAnimationUtils;
import java.io.PrintWriter;

public class PipMotionHelper implements Handler.Callback {
    private static final RectEvaluator RECT_EVALUATOR = new RectEvaluator(new Rect());
    /* access modifiers changed from: private */
    public IActivityTaskManager mActivityTaskManager;
    /* access modifiers changed from: private */
    public AnimationHandler mAnimationHandler;
    /* access modifiers changed from: private */
    public final Rect mBounds = new Rect();
    private ValueAnimator mBoundsAnimator = null;
    private Context mContext;
    private FlingAnimationUtils mFlingAnimationUtils;
    private Handler mHandler;
    private PipMenuActivityController mMenuController;
    private PipSnapAlgorithm mSnapAlgorithm;
    /* access modifiers changed from: private */
    public final Rect mStableInsets = new Rect();

    public PipMotionHelper(Context context, IActivityManager iActivityManager, PipMenuActivityController pipMenuActivityController, PipSnapAlgorithm pipSnapAlgorithm, FlingAnimationUtils flingAnimationUtils) {
        this.mContext = context;
        this.mHandler = new Handler(ForegroundThread.get().getLooper(), this);
        this.mActivityTaskManager = ActivityTaskManager.getService();
        this.mMenuController = pipMenuActivityController;
        this.mSnapAlgorithm = pipSnapAlgorithm;
        this.mFlingAnimationUtils = flingAnimationUtils;
        AnimationHandler animationHandler = new AnimationHandler();
        this.mAnimationHandler = animationHandler;
        animationHandler.setProvider(new SfVsyncFrameCallbackProvider());
        onConfigurationChanged();
    }

    /* access modifiers changed from: package-private */
    public void onConfigurationChanged() {
        this.mSnapAlgorithm.onConfigurationChanged();
        final SystemServicesProxy instance = SystemServicesProxy.getInstance(this.mContext);
        this.mHandler.post(new Runnable() {
            public void run() {
                instance.getStableInsets(PipMotionHelper.this.mStableInsets);
            }
        });
    }

    /* access modifiers changed from: package-private */
    public void synchronizePinnedStackBounds() {
        cancelAnimations();
        this.mHandler.post(new Runnable() {
            public void run() {
                try {
                    ActivityManager.StackInfo stackInfo = PipMotionHelper.this.mActivityTaskManager.getStackInfo(2, 0);
                    if (stackInfo != null) {
                        PipMotionHelper.this.mBounds.set(stackInfo.bounds);
                    }
                } catch (Exception e) {
                    Log.w("PipMotionHelper", "Failed to get pinned stack bounds", e);
                }
            }
        });
    }

    /* access modifiers changed from: package-private */
    public void movePip(Rect rect) {
        cancelAnimations();
        resizePipUnchecked(rect);
        this.mBounds.set(rect);
    }

    /* access modifiers changed from: package-private */
    public void expandPip() {
        expandPip(false);
    }

    /* access modifiers changed from: package-private */
    public void expandPip(boolean z) {
        cancelAnimations();
        this.mMenuController.hideMenuWithoutResize();
    }

    /* access modifiers changed from: package-private */
    public void dismissPip() {
        cancelAnimations();
        this.mMenuController.hideMenuWithoutResize();
        this.mHandler.post(new Runnable() {
            public final void run() {
                PipMotionHelper.this.lambda$dismissPip$0$PipMotionHelper();
            }
        });
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$dismissPip$0 */
    public /* synthetic */ void lambda$dismissPip$0$PipMotionHelper() {
        try {
            this.mActivityTaskManager.removeStacksInWindowingModes(new int[]{2});
        } catch (RemoteException e) {
            Log.e("PipMotionHelper", "Failed to remove PiP", e);
        }
    }

    /* access modifiers changed from: package-private */
    public Rect getBounds() {
        return this.mBounds;
    }

    /* access modifiers changed from: package-private */
    public Rect getClosestMinimizedBounds(Rect rect, Rect rect2) {
        return new Rect();
    }

    /* access modifiers changed from: package-private */
    public boolean shouldDismissPip() {
        Point point = new Point();
        this.mContext.getDisplay().getSize(point);
        Rect rect = this.mBounds;
        int i = rect.bottom;
        int i2 = point.y;
        if (i <= i2 || ((float) (i - i2)) / ((float) rect.height()) < 0.3f) {
            return false;
        }
        return true;
    }

    /* access modifiers changed from: package-private */
    public Rect animateToClosestMinimizedState(Rect rect, ValueAnimator.AnimatorUpdateListener animatorUpdateListener) {
        cancelAnimations();
        Rect closestMinimizedBounds = getClosestMinimizedBounds(this.mBounds, rect);
        if (!this.mBounds.equals(closestMinimizedBounds)) {
            ValueAnimator createAnimationToBounds = createAnimationToBounds(this.mBounds, closestMinimizedBounds, 200, Interpolators.LINEAR_OUT_SLOW_IN);
            this.mBoundsAnimator = createAnimationToBounds;
            if (animatorUpdateListener != null) {
                createAnimationToBounds.addUpdateListener(animatorUpdateListener);
            }
            this.mBoundsAnimator.start();
        }
        return closestMinimizedBounds;
    }

    /* access modifiers changed from: package-private */
    public Rect flingToSnapTarget(float f, float f2, float f3, Rect rect, ValueAnimator.AnimatorUpdateListener animatorUpdateListener, Animator.AnimatorListener animatorListener) {
        return new Rect();
    }

    /* access modifiers changed from: package-private */
    public Rect animateToClosestSnapTarget(Rect rect, ValueAnimator.AnimatorUpdateListener animatorUpdateListener, Animator.AnimatorListener animatorListener) {
        return new Rect();
    }

    /* access modifiers changed from: package-private */
    public float animateToExpandedState(Rect rect, Rect rect2, Rect rect3) {
        float snapFraction = this.mSnapAlgorithm.getSnapFraction(new Rect(this.mBounds), rect2);
        this.mSnapAlgorithm.applySnapFraction(rect, rect3, snapFraction);
        resizeAndAnimatePipUnchecked(rect, 250);
        return snapFraction;
    }

    /* access modifiers changed from: package-private */
    public void animateToUnexpandedState(Rect rect, float f, Rect rect2, Rect rect3, boolean z, boolean z2) {
        if (f < 0.0f) {
            f = this.mSnapAlgorithm.getSnapFraction(new Rect(this.mBounds), rect3);
        }
        this.mSnapAlgorithm.applySnapFraction(rect, rect2, f);
        if (z) {
            rect = getClosestMinimizedBounds(rect, rect2);
        }
        if (z2) {
            movePip(rect);
        } else {
            resizeAndAnimatePipUnchecked(rect, 250);
        }
    }

    /* access modifiers changed from: package-private */
    public Rect animateDismiss(Rect rect, float f, float f2, ValueAnimator.AnimatorUpdateListener animatorUpdateListener) {
        cancelAnimations();
        float length = PointF.length(f, f2);
        boolean z = length > this.mFlingAnimationUtils.getMinVelocityPxPerSecond();
        Point dismissEndPoint = getDismissEndPoint(rect, f, f2, z);
        Rect rect2 = new Rect(rect);
        rect2.offsetTo(dismissEndPoint.x, dismissEndPoint.y);
        ValueAnimator createAnimationToBounds = createAnimationToBounds(this.mBounds, rect2, 175, Interpolators.FAST_OUT_LINEAR_IN);
        this.mBoundsAnimator = createAnimationToBounds;
        createAnimationToBounds.addListener(new AnimatorListenerAdapter() {
            public void onAnimationEnd(Animator animator) {
                PipMotionHelper.this.dismissPip();
            }
        });
        if (z) {
            this.mFlingAnimationUtils.apply((Animator) this.mBoundsAnimator, 0.0f, distanceBetweenRectOffsets(this.mBounds, rect2), length);
        }
        if (animatorUpdateListener != null) {
            this.mBoundsAnimator.addUpdateListener(animatorUpdateListener);
        }
        this.mBoundsAnimator.start();
        return rect2;
    }

    /* access modifiers changed from: package-private */
    public void cancelAnimations() {
        ValueAnimator valueAnimator = this.mBoundsAnimator;
        if (valueAnimator != null) {
            valueAnimator.cancel();
            this.mBoundsAnimator = null;
        }
    }

    private ValueAnimator createAnimationToBounds(Rect rect, Rect rect2, int i, Interpolator interpolator) {
        AnonymousClass4 r0 = new ValueAnimator() {
            public AnimationHandler getAnimationHandler() {
                return PipMotionHelper.this.mAnimationHandler;
            }
        };
        r0.setObjectValues(new Object[]{rect, rect2});
        r0.setEvaluator(RECT_EVALUATOR);
        r0.setDuration((long) i);
        r0.setInterpolator(interpolator);
        r0.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            public final void onAnimationUpdate(ValueAnimator valueAnimator) {
                PipMotionHelper.this.lambda$createAnimationToBounds$1$PipMotionHelper(valueAnimator);
            }
        });
        return r0;
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$createAnimationToBounds$1 */
    public /* synthetic */ void lambda$createAnimationToBounds$1$PipMotionHelper(ValueAnimator valueAnimator) {
        resizePipUnchecked((Rect) valueAnimator.getAnimatedValue());
    }

    private void resizePipUnchecked(Rect rect) {
        if (!rect.equals(this.mBounds)) {
            SomeArgs obtain = SomeArgs.obtain();
            obtain.arg1 = rect;
            Handler handler = this.mHandler;
            handler.sendMessage(handler.obtainMessage(1, obtain));
        }
    }

    private void resizeAndAnimatePipUnchecked(Rect rect, int i) {
        if (!rect.equals(this.mBounds)) {
            SomeArgs obtain = SomeArgs.obtain();
            obtain.arg1 = rect;
            obtain.argi1 = i;
            Handler handler = this.mHandler;
            handler.sendMessage(handler.obtainMessage(2, obtain));
        }
    }

    private Point getDismissEndPoint(Rect rect, float f, float f2, boolean z) {
        Point point = new Point();
        this.mContext.getDisplay().getRealSize(point);
        float height = ((float) point.y) + (((float) rect.height()) * 0.1f);
        if (!z || f == 0.0f || f2 == 0.0f) {
            return new Point(rect.left, (int) height);
        }
        float f3 = f2 / f;
        return new Point((int) ((height - (((float) rect.top) - (((float) rect.left) * f3))) / f3), (int) height);
    }

    private float distanceBetweenRectOffsets(Rect rect, Rect rect2) {
        return PointF.length((float) (rect.left - rect2.left), (float) (rect.top - rect2.top));
    }

    public boolean handleMessage(Message message) {
        int i = message.what;
        return i == 1 || i == 2;
    }

    public void dump(PrintWriter printWriter, String str) {
        String str2 = str + "  ";
        printWriter.println(str + "PipMotionHelper");
        printWriter.println(str2 + "mBounds=" + this.mBounds);
        printWriter.println(str2 + "mStableInsets=" + this.mStableInsets);
    }
}
