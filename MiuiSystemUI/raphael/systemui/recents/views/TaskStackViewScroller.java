package com.android.systemui.recents.views;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.os.Handler;
import android.os.Vibrator;
import android.util.FloatProperty;
import android.util.Property;
import android.view.ViewDebug;
import android.widget.OverScroller;
import com.android.systemui.Interpolators;
import com.android.systemui.plugins.R;
import com.android.systemui.recents.events.RecentsEventBus;
import com.android.systemui.recents.events.activity.HideRecentsEvent;
import com.android.systemui.recents.events.activity.ScrollerFlingFinishEvent;
import com.android.systemui.recents.misc.RecentsPushEventHelper;
import com.android.systemui.recents.misc.Utilities;
import java.io.PrintWriter;

public class TaskStackViewScroller {
    private static final Property<TaskStackViewScroller, Float> STACK_SCROLL = new FloatProperty<TaskStackViewScroller>("stackScroll") {
        public void setValue(TaskStackViewScroller taskStackViewScroller, float f) {
            taskStackViewScroller.setStackScroll(f);
        }

        public Float get(TaskStackViewScroller taskStackViewScroller) {
            return Float.valueOf(taskStackViewScroller.getStackScroll());
        }
    };
    private final long VIBRATOR_DURATION = 10;
    TaskStackViewScrollerCallbacks mCb;
    Context mContext;
    float mExitRecentOverscrollThreshold = 1.0f;
    private int mExitRecentVelocityThreshold = 1200;
    float mFinalAnimatedScroll;
    float mFlingDownScrollP;
    int mFlingDownY;
    @ViewDebug.ExportedProperty(category = "recents")
    float mLastDeltaP = 0.0f;
    TaskStackLayoutAlgorithm mLayoutAlgorithm;
    ObjectAnimator mScrollAnimator;
    OverScroller mScroller;
    @ViewDebug.ExportedProperty(category = "recents")
    float mStackScrollP;
    private Vibrator mVibrator;

    public interface TaskStackViewScrollerCallbacks {
        void onStackScrollChanged(float f, float f2, AnimationProps animationProps);
    }

    public TaskStackViewScroller(Context context, TaskStackViewScrollerCallbacks taskStackViewScrollerCallbacks, TaskStackLayoutAlgorithm taskStackLayoutAlgorithm) {
        this.mContext = context;
        this.mCb = taskStackViewScrollerCallbacks;
        this.mScroller = new OverScroller(context);
        this.mLayoutAlgorithm = taskStackLayoutAlgorithm;
        this.mExitRecentOverscrollThreshold = this.mContext.getResources().getFloat(R.dimen.exit_recent_overscroll_threshold);
        this.mExitRecentVelocityThreshold = (int) (((float) this.mExitRecentVelocityThreshold) * this.mContext.getResources().getDisplayMetrics().density);
        this.mVibrator = (Vibrator) this.mContext.getSystemService("vibrator");
    }

    /* access modifiers changed from: package-private */
    public void reset() {
        this.mStackScrollP = 0.0f;
        this.mLastDeltaP = 0.0f;
    }

    /* access modifiers changed from: package-private */
    public void resetDeltaScroll() {
        this.mLastDeltaP = 0.0f;
    }

    public float getStackScroll() {
        return this.mStackScrollP;
    }

    public void setStackScroll(float f) {
        setStackScroll(f, AnimationProps.IMMEDIATE);
    }

    public float setDeltaStackScroll(float f, float f2) {
        float f3 = f + f2;
        this.mLayoutAlgorithm.updateFocusStateOnScroll(f + this.mLastDeltaP, f3, this.mStackScrollP);
        setStackScroll(f3, AnimationProps.IMMEDIATE);
        this.mLastDeltaP = f2;
        return f3 - f3;
    }

    public void setStackScroll(float f, AnimationProps animationProps) {
        float f2 = this.mStackScrollP;
        this.mStackScrollP = f;
        TaskStackViewScrollerCallbacks taskStackViewScrollerCallbacks = this.mCb;
        if (taskStackViewScrollerCallbacks != null) {
            taskStackViewScrollerCallbacks.onStackScrollChanged(f2, this.mStackScrollP, animationProps);
        }
    }

    public boolean setStackScrollToInitialState() {
        float f = this.mStackScrollP;
        setStackScroll(this.mLayoutAlgorithm.mInitialScrollP);
        return Float.compare(f, this.mStackScrollP) != 0;
    }

    public void fling(float f, int i, int i2, int i3, int i4, int i5, int i6) {
        this.mFlingDownScrollP = f;
        this.mFlingDownY = i;
        this.mScroller.fling(0, i2, 0, i3, 0, 0, i4, i5, 0, i6);
        new Handler().postDelayed(new Runnable() {
            public void run() {
                RecentsEventBus.getDefault().send(new ScrollerFlingFinishEvent());
            }
        }, (long) (this.mScroller.getDuration() + 25));
    }

    public boolean boundScroll() {
        float stackScroll = getStackScroll();
        float boundedStackScroll = getBoundedStackScroll(stackScroll);
        if (Float.compare(boundedStackScroll, stackScroll) == 0) {
            return false;
        }
        setStackScroll(boundedStackScroll);
        return true;
    }

    /* access modifiers changed from: package-private */
    public float getBoundedStackScroll(float f) {
        TaskStackLayoutAlgorithm taskStackLayoutAlgorithm = this.mLayoutAlgorithm;
        return Utilities.clamp(f, taskStackLayoutAlgorithm.mMinScrollP, taskStackLayoutAlgorithm.mMaxScrollP);
    }

    /* access modifiers changed from: package-private */
    public float getScrollAmountOutOfBounds(float f) {
        TaskStackLayoutAlgorithm taskStackLayoutAlgorithm = this.mLayoutAlgorithm;
        float f2 = taskStackLayoutAlgorithm.mMinScrollP;
        if (f < f2) {
            return Math.abs(f - f2);
        }
        float f3 = taskStackLayoutAlgorithm.mMaxScrollP;
        if (f > f3) {
            return Math.abs(f - f3);
        }
        return 0.0f;
    }

    /* access modifiers changed from: package-private */
    public boolean isScrollOutOfBounds() {
        return Float.compare(getScrollAmountOutOfBounds(this.mStackScrollP), 0.0f) != 0;
    }

    /* access modifiers changed from: package-private */
    public ObjectAnimator animateBoundScroll(int i) {
        float stackScroll = getStackScroll();
        float boundedStackScroll = getBoundedStackScroll(stackScroll);
        if (Float.compare(boundedStackScroll, stackScroll) != 0) {
            float f = this.mExitRecentOverscrollThreshold;
            if (stackScroll < (-f) || (i > this.mExitRecentVelocityThreshold && ((double) stackScroll) < ((double) (-f)) * 0.3d)) {
                RecentsPushEventHelper.sendHideRecentsEvent("dropDown");
                RecentsEventBus.getDefault().send(new HideRecentsEvent(false, false, false, true));
            } else {
                animateScroll(boundedStackScroll, (Runnable) null);
            }
        }
        return this.mScrollAnimator;
    }

    /* access modifiers changed from: package-private */
    public void animateScroll(float f, Runnable runnable) {
        animateScroll(f, this.mContext.getResources().getInteger(R.integer.recents_animate_task_stack_scroll_duration), runnable);
    }

    /* access modifiers changed from: package-private */
    public void animateScroll(float f, int i, final Runnable runnable) {
        ObjectAnimator objectAnimator = this.mScrollAnimator;
        if (objectAnimator != null && objectAnimator.isRunning()) {
            setStackScroll(this.mFinalAnimatedScroll);
            this.mScroller.forceFinished(true);
        }
        stopScroller();
        stopBoundScrollAnimation();
        if (Float.compare(this.mStackScrollP, f) != 0) {
            this.mFinalAnimatedScroll = f;
            this.mScrollAnimator = ObjectAnimator.ofFloat(this, STACK_SCROLL, new float[]{getStackScroll(), f});
            this.mScrollAnimator.setDuration((long) i);
            this.mScrollAnimator.setInterpolator(Interpolators.LINEAR_OUT_SLOW_IN);
            this.mScrollAnimator.addListener(new AnimatorListenerAdapter() {
                public void onAnimationEnd(Animator animator) {
                    Runnable runnable = runnable;
                    if (runnable != null) {
                        runnable.run();
                    }
                    TaskStackViewScroller.this.mScrollAnimator.removeAllListeners();
                }
            });
            this.mScrollAnimator.start();
        } else if (runnable != null) {
            runnable.run();
        }
    }

    /* access modifiers changed from: package-private */
    public void stopBoundScrollAnimation() {
        Utilities.cancelAnimationWithoutCallbacks(this.mScrollAnimator);
    }

    /* access modifiers changed from: package-private */
    public boolean computeScroll() {
        if (!this.mScroller.computeScrollOffset()) {
            return false;
        }
        float deltaPForX = this.mLayoutAlgorithm.getDeltaPForX(this.mFlingDownY, this.mScroller.getCurrY());
        float f = this.mFlingDownScrollP;
        this.mFlingDownScrollP = f + setDeltaStackScroll(f, deltaPForX);
        return true;
    }

    /* access modifiers changed from: package-private */
    public void stopScroller() {
        if (!this.mScroller.isFinished()) {
            this.mScroller.abortAnimation();
        }
    }

    public void dump(String str, PrintWriter printWriter) {
        printWriter.print(str);
        printWriter.print("TaskStackViewScroller");
        printWriter.print(" stackScroll:");
        printWriter.print(this.mStackScrollP);
        printWriter.println();
    }
}
