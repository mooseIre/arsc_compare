package com.android.systemui.recents;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import androidx.dynamicanimation.animation.SpringAnimation;
import com.android.systemui.Interpolators;
import com.android.systemui.plugins.R;
import com.android.systemui.recents.misc.SpringAnimationUtils;
import com.android.systemui.recents.views.TaskView;
import java.util.HashMap;

public class SwipeHelperForRecents {
    private int DEFAULT_ESCAPE_ANIMATION_DURATION = 300;
    private int MAX_DISMISS_VELOCITY = 4000;
    /* access modifiers changed from: private */
    public Callback mCallback;
    /* access modifiers changed from: private */
    public View mCurrView;
    /* access modifiers changed from: private */
    public SpringAnimation mCurrentAnim;
    private float mDensityScale;
    /* access modifiers changed from: private */
    public boolean mDisableHwLayers;
    /* access modifiers changed from: private */
    public HashMap<View, Animator> mDismissPendingMap = new HashMap<>();
    private boolean mDragging;
    private int mFalsingThreshold;
    private Handler mHandler;
    private float mInitialTouchPos;
    private boolean mLastSwipedFarEnough;
    /* access modifiers changed from: private */
    public LongPressListener mLongPressListener;
    /* access modifiers changed from: private */
    public boolean mLongPressSent;
    private long mLongPressTimeout;
    private float mMaxSwipeProgress = 1.0f;
    private float mMinSwipeProgress = 0.0f;
    private float mPagingTouchSlop;
    private float mPerpendicularInitialTouchPos;
    /* access modifiers changed from: private */
    public boolean mSnappingChild;
    private int mSwipeDirection;
    /* access modifiers changed from: private */
    public final int[] mTmpPos = new int[2];
    private boolean mTouchAboveFalsingThreshold;
    private float mTranslation = 0.0f;
    private VelocityTracker mVelocityTracker;
    private Runnable mWatchLongPress;

    public interface Callback {
        boolean canChildBeDismissed(View view);

        boolean checkToBeginDrag(View view);

        View getChildAtPosition(MotionEvent motionEvent);

        float getFalsingThresholdFactor();

        boolean isAntiFalsingNeeded();

        void onBeginDrag(View view);

        void onChildDismissed(View view);

        void onChildSnappedBack(View view, float f);

        void onDragCancelled(View view);

        void onDragEnd(View view);

        boolean updateSwipeProgress(View view, boolean z, float f);
    }

    public interface LongPressListener {
        boolean onLongPress(View view, int i, int i2);
    }

    /* access modifiers changed from: protected */
    public abstract float getSize(View view);

    /* access modifiers changed from: protected */
    public abstract float getUnscaledEscapeVelocity();

    /* access modifiers changed from: protected */
    public boolean handleUpEvent(MotionEvent motionEvent, View view, float f, float f2) {
        return false;
    }

    public void onDownUpdate(View view) {
    }

    /* access modifiers changed from: protected */
    public abstract void onMoveUpdate(View view, float f, float f2);

    /* access modifiers changed from: protected */
    public abstract void prepareDismissAnimation(View view, Object obj);

    /* access modifiers changed from: protected */
    public abstract void prepareSnapBackAnimation(View view, Object obj);

    public SwipeHelperForRecents(int i, Callback callback, Context context) {
        this.mCallback = callback;
        this.mHandler = new Handler();
        this.mSwipeDirection = i;
        this.mVelocityTracker = VelocityTracker.obtain();
        this.mDensityScale = context.getResources().getDisplayMetrics().density;
        this.mPagingTouchSlop = (float) ViewConfiguration.get(context).getScaledPagingTouchSlop();
        this.mLongPressTimeout = (long) (((float) ViewConfiguration.getLongPressTimeout()) * 1.5f);
        this.mFalsingThreshold = context.getResources().getDimensionPixelSize(R.dimen.swipe_helper_falsing_threshold);
    }

    public void setDisableHardwareLayers(boolean z) {
        this.mDisableHwLayers = z;
    }

    private float getPos(MotionEvent motionEvent) {
        return this.mSwipeDirection == 0 ? motionEvent.getX() : motionEvent.getY();
    }

    private float getPerpendicularPos(MotionEvent motionEvent) {
        return this.mSwipeDirection == 0 ? motionEvent.getY() : motionEvent.getX();
    }

    /* access modifiers changed from: protected */
    public float getTranslation(View view) {
        return this.mSwipeDirection == 0 ? view.getTranslationX() : view.getTranslationY();
    }

    private float getVelocity(VelocityTracker velocityTracker) {
        if (this.mSwipeDirection == 0) {
            return velocityTracker.getXVelocity();
        }
        return velocityTracker.getYVelocity();
    }

    /* access modifiers changed from: protected */
    public ObjectAnimator createTranslationAnimation(View view, float f) {
        return ObjectAnimator.ofFloat(view, this.mSwipeDirection == 0 ? View.TRANSLATION_X : View.TRANSLATION_Y, new float[]{f});
    }

    /* access modifiers changed from: protected */
    public Animator getViewTranslationAnimator(View view, float f, ValueAnimator.AnimatorUpdateListener animatorUpdateListener) {
        ObjectAnimator createTranslationAnimation = createTranslationAnimation(view, f);
        if (animatorUpdateListener != null) {
            createTranslationAnimation.addUpdateListener(animatorUpdateListener);
        }
        return createTranslationAnimation;
    }

    /* access modifiers changed from: protected */
    public void setTranslation(View view, float f) {
        if (view != null) {
            if (this.mSwipeDirection == 0) {
                view.setTranslationX(f);
            } else {
                view.setTranslationY(f);
            }
        }
    }

    private float getViewSize(View view) {
        int i;
        if (this.mSwipeDirection == 0) {
            i = view.getMeasuredWidth();
        } else {
            i = view.getMeasuredHeight();
        }
        return (float) i;
    }

    private float getSwipeProgressForOffset(View view, float f) {
        return Math.min(Math.max(this.mMinSwipeProgress, Math.abs(f / getSize(view))), this.mMaxSwipeProgress);
    }

    private float getSwipeAlpha(float f) {
        return 1.0f - Math.max(0.0f, Math.min(1.0f, f / 0.5f));
    }

    /* access modifiers changed from: private */
    public void updateSwipeProgressFromOffset(View view, boolean z) {
        updateSwipeProgressFromOffset(view, z, getTranslation(view));
    }

    private void updateSwipeProgressFromOffset(View view, boolean z, float f) {
        float swipeProgressForOffset = getSwipeProgressForOffset(view, f);
        if (!this.mCallback.updateSwipeProgress(view, z, f > 0.0f ? swipeProgressForOffset : -swipeProgressForOffset) && z) {
            if (!this.mDisableHwLayers) {
                if (swipeProgressForOffset == 0.0f || swipeProgressForOffset == 1.0f) {
                    view.setLayerType(0, (Paint) null);
                } else {
                    view.setLayerType(2, (Paint) null);
                }
            }
            view.setAlpha(getSwipeAlpha(swipeProgressForOffset));
        }
        invalidateGlobalRegion(view);
    }

    public static void invalidateGlobalRegion(View view) {
        invalidateGlobalRegion(view, new RectF((float) view.getLeft(), (float) view.getTop(), (float) view.getRight(), (float) view.getBottom()));
    }

    public static void invalidateGlobalRegion(View view, RectF rectF) {
        while (view.getParent() != null && (view.getParent() instanceof View)) {
            view = (View) view.getParent();
            view.getMatrix().mapRect(rectF);
            view.invalidate((int) Math.floor((double) rectF.left), (int) Math.floor((double) rectF.top), (int) Math.ceil((double) rectF.right), (int) Math.ceil((double) rectF.bottom));
        }
    }

    public void removeLongPressCallback() {
        Runnable runnable = this.mWatchLongPress;
        if (runnable != null) {
            this.mHandler.removeCallbacks(runnable);
            this.mWatchLongPress = null;
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:6:0x000e, code lost:
        if (r0 != 3) goto L_0x00dd;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean onInterceptTouchEvent(final android.view.MotionEvent r7) {
        /*
            r6 = this;
            int r0 = r7.getAction()
            r1 = 1
            r2 = 0
            if (r0 == 0) goto L_0x0082
            if (r0 == r1) goto L_0x0069
            r3 = 2
            if (r0 == r3) goto L_0x0012
            r7 = 3
            if (r0 == r7) goto L_0x0069
            goto L_0x00dd
        L_0x0012:
            android.view.View r0 = r6.mCurrView
            if (r0 == 0) goto L_0x00dd
            boolean r0 = r6.mLongPressSent
            if (r0 != 0) goto L_0x00dd
            android.view.VelocityTracker r0 = r6.mVelocityTracker
            r0.addMovement(r7)
            float r0 = r6.getPos(r7)
            float r3 = r6.getPerpendicularPos(r7)
            float r4 = r6.mInitialTouchPos
            float r0 = r0 - r4
            float r4 = r6.mPerpendicularInitialTouchPos
            float r3 = r3 - r4
            float r4 = java.lang.Math.abs(r0)
            float r5 = r6.mPagingTouchSlop
            int r4 = (r4 > r5 ? 1 : (r4 == r5 ? 0 : -1))
            if (r4 <= 0) goto L_0x00dd
            float r0 = java.lang.Math.abs(r0)
            float r3 = java.lang.Math.abs(r3)
            int r0 = (r0 > r3 ? 1 : (r0 == r3 ? 0 : -1))
            if (r0 <= 0) goto L_0x00dd
            com.android.systemui.recents.SwipeHelperForRecents$Callback r0 = r6.mCallback
            android.view.View r3 = r6.mCurrView
            boolean r0 = r0.checkToBeginDrag(r3)
            if (r0 == 0) goto L_0x00dd
            com.android.systemui.recents.SwipeHelperForRecents$Callback r0 = r6.mCallback
            android.view.View r3 = r6.mCurrView
            r0.onBeginDrag(r3)
            r6.mDragging = r1
            float r7 = r6.getPos(r7)
            r6.mInitialTouchPos = r7
            android.view.View r7 = r6.mCurrView
            float r7 = r6.getTranslation(r7)
            r6.mTranslation = r7
            r6.removeLongPressCallback()
            goto L_0x00dd
        L_0x0069:
            boolean r7 = r6.mDragging
            if (r7 != 0) goto L_0x0074
            boolean r7 = r6.mLongPressSent
            if (r7 == 0) goto L_0x0072
            goto L_0x0074
        L_0x0072:
            r7 = r2
            goto L_0x0075
        L_0x0074:
            r7 = r1
        L_0x0075:
            r6.mDragging = r2
            r0 = 0
            r6.mCurrView = r0
            r6.mLongPressSent = r2
            r6.removeLongPressCallback()
            if (r7 == 0) goto L_0x00dd
            return r1
        L_0x0082:
            r6.mTouchAboveFalsingThreshold = r2
            r6.mDragging = r2
            boolean r0 = r6.mSnappingChild
            if (r0 == 0) goto L_0x0091
            androidx.dynamicanimation.animation.SpringAnimation r0 = r6.mCurrentAnim
            if (r0 == 0) goto L_0x0091
            r0.skipToEnd()
        L_0x0091:
            r6.mLongPressSent = r2
            android.view.VelocityTracker r0 = r6.mVelocityTracker
            r0.clear()
            com.android.systemui.recents.SwipeHelperForRecents$Callback r0 = r6.mCallback
            android.view.View r0 = r0.getChildAtPosition(r7)
            r6.mCurrView = r0
            if (r0 == 0) goto L_0x00dd
            r6.onDownUpdate(r0)
            com.android.systemui.recents.SwipeHelperForRecents$Callback r0 = r6.mCallback
            android.view.View r3 = r6.mCurrView
            r0.canChildBeDismissed(r3)
            android.view.VelocityTracker r0 = r6.mVelocityTracker
            r0.addMovement(r7)
            float r0 = r6.getPos(r7)
            r6.mInitialTouchPos = r0
            float r0 = r6.getPerpendicularPos(r7)
            r6.mPerpendicularInitialTouchPos = r0
            android.view.View r0 = r6.mCurrView
            float r0 = r6.getTranslation(r0)
            r6.mTranslation = r0
            com.android.systemui.recents.SwipeHelperForRecents$LongPressListener r0 = r6.mLongPressListener
            if (r0 == 0) goto L_0x00dd
            java.lang.Runnable r0 = r6.mWatchLongPress
            if (r0 != 0) goto L_0x00d4
            com.android.systemui.recents.SwipeHelperForRecents$2 r0 = new com.android.systemui.recents.SwipeHelperForRecents$2
            r0.<init>(r7)
            r6.mWatchLongPress = r0
        L_0x00d4:
            android.os.Handler r7 = r6.mHandler
            java.lang.Runnable r0 = r6.mWatchLongPress
            long r3 = r6.mLongPressTimeout
            r7.postDelayed(r0, r3)
        L_0x00dd:
            boolean r7 = r6.mDragging
            if (r7 != 0) goto L_0x00e7
            boolean r6 = r6.mLongPressSent
            if (r6 == 0) goto L_0x00e6
            goto L_0x00e7
        L_0x00e6:
            r1 = r2
        L_0x00e7:
            return r1
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.recents.SwipeHelperForRecents.onInterceptTouchEvent(android.view.MotionEvent):boolean");
    }

    public void dismissChild(View view, float f) {
        dismissChild(view, f, (Runnable) null, 0, false);
    }

    public void dismissChild(final View view, float f, final Runnable runnable, long j, boolean z) {
        float f2;
        final boolean canChildBeDismissed = this.mCallback.canChildBeDismissed(view);
        boolean z2 = false;
        boolean z3 = view.getLayoutDirection() == 1;
        int i = (f > 0.0f ? 1 : (f == 0.0f ? 0 : -1));
        boolean z4 = i == 0 && (getTranslation(view) == 0.0f || z) && this.mSwipeDirection == 1;
        boolean z5 = i == 0 && (getTranslation(view) == 0.0f || z) && z3;
        if (Math.abs(f) >= 500.0f ? f < 0.0f : getTranslation(view) < 0.0f) {
            z2 = true;
        }
        if (z2 || z5 || z4) {
            f2 = -getSize(view);
        } else {
            f2 = getSize(view);
        }
        if (!this.mDisableHwLayers) {
            view.setLayerType(2, (Paint) null);
        }
        Animator viewTranslationAnimator = getViewTranslationAnimator(view, f2, new ValueAnimator.AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                SwipeHelperForRecents.this.onTranslationUpdate(view, Float.valueOf(valueAnimator.getAnimatedFraction()).floatValue() * SwipeHelperForRecents.this.getSize(view), canChildBeDismissed);
            }
        });
        if (viewTranslationAnimator != null) {
            viewTranslationAnimator.setInterpolator(Interpolators.EASE_IN_OUT);
            viewTranslationAnimator.setDuration((long) this.DEFAULT_ESCAPE_ANIMATION_DURATION);
            if (j > 0) {
                viewTranslationAnimator.setStartDelay(j);
            }
            viewTranslationAnimator.addListener(new AnimatorListenerAdapter() {
                private boolean mCancelled;

                public void onAnimationCancel(Animator animator) {
                    this.mCancelled = true;
                }

                public void onAnimationEnd(Animator animator) {
                    SwipeHelperForRecents.this.updateSwipeProgressFromOffset(view, canChildBeDismissed);
                    SwipeHelperForRecents.this.mDismissPendingMap.remove(view);
                    if (!this.mCancelled) {
                        SwipeHelperForRecents.this.mCallback.onChildDismissed(view);
                    }
                    Runnable runnable = runnable;
                    if (runnable != null) {
                        runnable.run();
                    }
                    if (!SwipeHelperForRecents.this.mDisableHwLayers) {
                        view.setLayerType(0, (Paint) null);
                    }
                }
            });
            prepareDismissAnimation(view, viewTranslationAnimator);
            this.mDismissPendingMap.put(view, viewTranslationAnimator);
            viewTranslationAnimator.start();
        }
    }

    public void snapChild(final View view, final float f, float f2) {
        SpringAnimation startTaskViewSnapAnim;
        final boolean canChildBeDismissed = this.mCallback.canChildBeDismissed(view);
        if ((view instanceof TaskView) && (startTaskViewSnapAnim = SpringAnimationUtils.getInstance().startTaskViewSnapAnim((TaskView) view, new Runnable() {
            public void run() {
                boolean unused = SwipeHelperForRecents.this.mSnappingChild = false;
                SwipeHelperForRecents.this.updateSwipeProgressFromOffset(view, canChildBeDismissed);
                SwipeHelperForRecents.this.mCallback.onChildSnappedBack(view, f);
                SpringAnimation unused2 = SwipeHelperForRecents.this.mCurrentAnim = null;
            }
        })) != null) {
            prepareSnapBackAnimation(view, startTaskViewSnapAnim);
            this.mSnappingChild = true;
            this.mCurrentAnim = startTaskViewSnapAnim;
        }
    }

    public void onTranslationUpdate(View view, float f, boolean z) {
        updateSwipeProgressFromOffset(view, z, f);
    }

    /* JADX WARNING: Code restructure failed: missing block: B:18:0x002e, code lost:
        if (r0 != 4) goto L_0x00eb;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean onTouchEvent(android.view.MotionEvent r8) {
        /*
            r7 = this;
            boolean r0 = r7.mLongPressSent
            r1 = 1
            if (r0 == 0) goto L_0x0006
            return r1
        L_0x0006:
            boolean r0 = r7.mDragging
            r2 = 0
            if (r0 != 0) goto L_0x001b
            com.android.systemui.recents.SwipeHelperForRecents$Callback r0 = r7.mCallback
            android.view.View r0 = r0.getChildAtPosition(r8)
            if (r0 == 0) goto L_0x0017
            r7.onInterceptTouchEvent(r8)
            return r1
        L_0x0017:
            r7.removeLongPressCallback()
            return r2
        L_0x001b:
            android.view.VelocityTracker r0 = r7.mVelocityTracker
            r0.addMovement(r8)
            int r0 = r8.getAction()
            r3 = 0
            if (r0 == r1) goto L_0x00a3
            r4 = 2
            if (r0 == r4) goto L_0x0032
            r4 = 3
            if (r0 == r4) goto L_0x00a3
            r2 = 4
            if (r0 == r2) goto L_0x0032
            goto L_0x00eb
        L_0x0032:
            android.view.View r0 = r7.mCurrView
            if (r0 == 0) goto L_0x00eb
            float r8 = r7.getPos(r8)
            float r0 = r7.mInitialTouchPos
            float r8 = r8 - r0
            float r0 = java.lang.Math.abs(r8)
            int r2 = r7.getFalsingThreshold()
            float r2 = (float) r2
            int r2 = (r0 > r2 ? 1 : (r0 == r2 ? 0 : -1))
            if (r2 < 0) goto L_0x004c
            r7.mTouchAboveFalsingThreshold = r1
        L_0x004c:
            com.android.systemui.recents.SwipeHelperForRecents$Callback r2 = r7.mCallback
            android.view.View r4 = r7.mCurrView
            boolean r2 = r2.canChildBeDismissed(r4)
            if (r2 != 0) goto L_0x0079
            android.view.View r2 = r7.mCurrView
            float r2 = r7.getSize(r2)
            r4 = 1048576000(0x3e800000, float:0.25)
            float r4 = r4 * r2
            int r0 = (r0 > r2 ? 1 : (r0 == r2 ? 0 : -1))
            if (r0 < 0) goto L_0x006b
            int r8 = (r8 > r3 ? 1 : (r8 == r3 ? 0 : -1))
            if (r8 <= 0) goto L_0x0069
            r8 = r4
            goto L_0x0079
        L_0x0069:
            float r8 = -r4
            goto L_0x0079
        L_0x006b:
            float r8 = r8 / r2
            double r2 = (double) r8
            r5 = 4609753056924675352(0x3ff921fb54442d18, double:1.5707963267948966)
            double r2 = r2 * r5
            double r2 = java.lang.Math.sin(r2)
            float r8 = (float) r2
            float r8 = r8 * r4
        L_0x0079:
            android.view.View r0 = r7.mCurrView
            float r2 = r7.mTranslation
            float r2 = r2 + r8
            r7.setTranslation(r0, r2)
            android.view.View r0 = r7.mCurrView
            if (r0 == 0) goto L_0x009a
            boolean r0 = r7.mLastSwipedFarEnough
            if (r0 != 0) goto L_0x0094
            boolean r0 = r7.swipedFarEnough()
            if (r0 == 0) goto L_0x0094
            android.view.View r0 = r7.mCurrView
            r0.performHapticFeedback(r1)
        L_0x0094:
            boolean r0 = r7.swipedFarEnough()
            r7.mLastSwipedFarEnough = r0
        L_0x009a:
            android.view.View r0 = r7.mCurrView
            float r2 = r7.mTranslation
            float r2 = r2 + r8
            r7.onMoveUpdate(r0, r2, r8)
            goto L_0x00eb
        L_0x00a3:
            android.view.View r0 = r7.mCurrView
            if (r0 != 0) goto L_0x00a8
            goto L_0x00eb
        L_0x00a8:
            android.view.VelocityTracker r0 = r7.mVelocityTracker
            r4 = 1000(0x3e8, float:1.401E-42)
            float r5 = r7.getMaxVelocity()
            r0.computeCurrentVelocity(r4, r5)
            android.view.VelocityTracker r0 = r7.mVelocityTracker
            float r0 = r7.getVelocity(r0)
            com.android.systemui.recents.SwipeHelperForRecents$Callback r4 = r7.mCallback
            android.view.View r5 = r7.mCurrView
            r4.onDragEnd(r5)
            android.view.View r4 = r7.mCurrView
            float r5 = r7.getTranslation(r4)
            boolean r4 = r7.handleUpEvent(r8, r4, r0, r5)
            if (r4 != 0) goto L_0x00e7
            boolean r8 = r7.isDismissGesture(r8)
            if (r8 == 0) goto L_0x00d8
            android.view.View r8 = r7.mCurrView
            r7.dismissChild(r8, r0)
            goto L_0x00e4
        L_0x00d8:
            com.android.systemui.recents.SwipeHelperForRecents$Callback r8 = r7.mCallback
            android.view.View r4 = r7.mCurrView
            r8.onDragCancelled(r4)
            android.view.View r8 = r7.mCurrView
            r7.snapChild(r8, r3, r0)
        L_0x00e4:
            r8 = 0
            r7.mCurrView = r8
        L_0x00e7:
            r7.mDragging = r2
            r7.mLastSwipedFarEnough = r2
        L_0x00eb:
            return r1
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.recents.SwipeHelperForRecents.onTouchEvent(android.view.MotionEvent):boolean");
    }

    private int getFalsingThreshold() {
        return (int) (((float) this.mFalsingThreshold) * this.mCallback.getFalsingThresholdFactor());
    }

    private float getMaxVelocity() {
        return ((float) this.MAX_DISMISS_VELOCITY) * this.mDensityScale;
    }

    /* access modifiers changed from: protected */
    public float getEscapeVelocity() {
        return getUnscaledEscapeVelocity() * this.mDensityScale;
    }

    /* access modifiers changed from: protected */
    public boolean swipedFarEnough() {
        return ((double) Math.abs(getTranslation(this.mCurrView))) > ((double) getViewSize(this.mCurrView)) * 0.4d;
    }

    /* access modifiers changed from: protected */
    public boolean isDismissGesture(MotionEvent motionEvent) {
        if (this.mCallback.isAntiFalsingNeeded() && !this.mTouchAboveFalsingThreshold) {
            return false;
        }
        if ((swipedFastEnough() || swipedFarEnough()) && motionEvent.getActionMasked() == 1 && this.mCallback.canChildBeDismissed(this.mCurrView)) {
            return true;
        }
        return false;
    }

    /* access modifiers changed from: protected */
    public boolean swipedFastEnough() {
        float velocity = getVelocity(this.mVelocityTracker);
        float translation = getTranslation(this.mCurrView);
        if (Math.abs(velocity) > getEscapeVelocity()) {
            if ((velocity > 0.0f) == (translation > 0.0f)) {
                return true;
            }
        }
        return false;
    }
}
