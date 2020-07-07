package com.android.systemui;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import com.android.systemui.classifier.FalsingManager;
import com.android.systemui.plugins.R;
import com.android.systemui.plugins.statusbar.NotificationMenuRowPlugin;
import com.android.systemui.statistic.ScenarioConstants;
import com.android.systemui.statistic.ScenarioTrackUtil;
import com.android.systemui.statusbar.ExpandableNotificationRow;
import com.android.systemui.statusbar.FlingAnimationUtils;
import java.util.HashMap;
import java.util.List;

public class SwipeHelper {
    /* access modifiers changed from: private */
    public Callback mCallback;
    private boolean mCanCurrViewBeDimissed;
    /* access modifiers changed from: private */
    public Context mContext;
    /* access modifiers changed from: private */
    public View mCurrView;
    private float mDensityScale;
    /* access modifiers changed from: private */
    public boolean mDisableHwLayers;
    private AnimatorSet mDismissAllAnimatorSet;
    /* access modifiers changed from: private */
    public HashMap<View, Animator> mDismissPendingMap = new HashMap<>();
    private boolean mDragging;
    private FalsingManager mFalsingManager;
    private int mFalsingThreshold;
    private FlingAnimationUtils mFlingAnimationUtils;
    private Handler mHandler;
    private float mInitialTouchPos;
    /* access modifiers changed from: private */
    public LongPressListener mLongPressListener;
    /* access modifiers changed from: private */
    public boolean mLongPressSent;
    private long mLongPressTimeout;
    private int mMaxSwipeTranslation;
    private int mMenuShownSize;
    private float mPagingTouchSlop;
    private float mPerpendicularInitialTouchPos;
    private int mScreenWidth;
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

        View getChildAtPosition(MotionEvent motionEvent);

        float getFalsingThresholdFactor();

        boolean isAntiFalsingNeeded();

        void onBeginDrag(View view);

        void onChildDismissed(View view);

        void onChildSnappedBack(View view, float f);

        void onDragCancelled(View view);
    }

    public interface LongPressListener {
        boolean onLongPress(View view, int i, int i2, NotificationMenuRowPlugin.MenuItem menuItem);
    }

    public interface MenuPressListener {
        boolean onMenuPress(View view, int i, int i2, NotificationMenuRowPlugin.MenuItem menuItem);
    }

    /* access modifiers changed from: protected */
    public long getMaxEscapeAnimDuration() {
        return 400;
    }

    /* access modifiers changed from: protected */
    public abstract float getTranslation(View view);

    /* access modifiers changed from: protected */
    public float getUnscaledEscapeVelocity() {
        return 500.0f;
    }

    /* access modifiers changed from: protected */
    public abstract boolean handleUpEvent(MotionEvent motionEvent, View view, float f, float f2);

    public abstract void onDownUpdate(View view, MotionEvent motionEvent);

    /* access modifiers changed from: protected */
    public abstract void onMoveUpdate(View view, MotionEvent motionEvent, float f, float f2);

    /* access modifiers changed from: protected */
    public void prepareDismissAnimation(View view, Animator animator) {
    }

    /* access modifiers changed from: protected */
    public void prepareSnapBackAnimation(View view, Animator animator) {
    }

    public void resetAnimatingValue() {
    }

    /* access modifiers changed from: protected */
    public abstract void setTranslation(View view, float f);

    public SwipeHelper(int i, Callback callback, Context context) {
        this.mContext = context;
        this.mCallback = callback;
        this.mHandler = new Handler();
        this.mSwipeDirection = i;
        this.mVelocityTracker = VelocityTracker.obtain();
        this.mDensityScale = context.getResources().getDisplayMetrics().density;
        this.mPagingTouchSlop = (float) ViewConfiguration.get(context).getScaledPagingTouchSlop();
        this.mLongPressTimeout = (long) (((float) ViewConfiguration.getLongPressTimeout()) * 1.5f);
        this.mFalsingThreshold = context.getResources().getDimensionPixelSize(R.dimen.swipe_helper_falsing_threshold);
        this.mFalsingManager = FalsingManager.getInstance(context);
        this.mFlingAnimationUtils = new FlingAnimationUtils(context, ((float) getMaxEscapeAnimDuration()) / 1000.0f);
        this.mMenuShownSize = context.getResources().getDimensionPixelSize(R.dimen.notification_menu_space);
        this.mMaxSwipeTranslation = context.getResources().getDimensionPixelSize(R.dimen.notification_swipe_max_distance);
        this.mScreenWidth = context.getResources().getDisplayMetrics().widthPixels;
        initDismissAllAnimation();
    }

    private void initDismissAllAnimation() {
        AnimatorSet animatorSet = new AnimatorSet();
        this.mDismissAllAnimatorSet = animatorSet;
        animatorSet.setDuration(160);
    }

    public void setLongPressListener(LongPressListener longPressListener) {
        this.mLongPressListener = longPressListener;
    }

    public void setDensityScale(float f) {
        this.mDensityScale = f;
    }

    public void setPagingTouchSlop(float f) {
        this.mPagingTouchSlop = f;
    }

    private float getPos(MotionEvent motionEvent) {
        return this.mSwipeDirection == 0 ? motionEvent.getX() : motionEvent.getY();
    }

    private float getPerpendicularPos(MotionEvent motionEvent) {
        return this.mSwipeDirection == 0 ? motionEvent.getY() : motionEvent.getX();
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
    public float getSize(View view) {
        int i;
        if (this.mSwipeDirection == 0) {
            i = view.getMeasuredWidth();
        } else {
            i = view.getMeasuredHeight();
        }
        return (float) i;
    }

    private float getAlphaForOffset(float f) {
        if (this.mMenuShownSize > 0) {
            return Math.max(1.0f - ((Math.abs(f) / ((float) this.mMenuShownSize)) * 0.35000002f), 0.0f);
        }
        return 1.0f;
    }

    /* access modifiers changed from: private */
    public void updateSwipeProgressFromOffset(View view, boolean z) {
        updateSwipeProgressFromOffset(view, z, getTranslation(view));
    }

    private void updateSwipeProgressFromOffset(View view, boolean z, float f) {
        int i = (f > 0.0f ? 1 : (f == 0.0f ? 0 : -1));
        if ((i > 0 && z) || f < 0.0f) {
            float alphaForOffset = getAlphaForOffset(f);
            if (i > 0 && (view instanceof ExpandableNotificationRow)) {
                if (!this.mDisableHwLayers) {
                    if (alphaForOffset == 0.0f || alphaForOffset == 1.0f) {
                        view.setLayerType(0, (Paint) null);
                    } else {
                        view.setLayerType(2, (Paint) null);
                    }
                }
                view.setAlpha(alphaForOffset);
            }
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

    /* JADX WARNING: Code restructure failed: missing block: B:6:0x000f, code lost:
        if (r0 != 3) goto L_0x0101;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean onInterceptTouchEvent(final android.view.MotionEvent r7) {
        /*
            r6 = this;
            int r0 = r7.getAction()
            r1 = 0
            r2 = 1
            r3 = 0
            if (r0 == 0) goto L_0x0078
            if (r0 == r2) goto L_0x0060
            r4 = 2
            if (r0 == r4) goto L_0x0013
            r7 = 3
            if (r0 == r7) goto L_0x0060
            goto L_0x0101
        L_0x0013:
            android.view.View r0 = r6.mCurrView
            if (r0 == 0) goto L_0x0101
            boolean r0 = r6.mLongPressSent
            if (r0 != 0) goto L_0x0101
            android.view.VelocityTracker r0 = r6.mVelocityTracker
            r0.addMovement(r7)
            float r0 = r6.getPos(r7)
            float r1 = r6.getPerpendicularPos(r7)
            float r4 = r6.mInitialTouchPos
            float r0 = r0 - r4
            float r4 = r6.mPerpendicularInitialTouchPos
            float r1 = r1 - r4
            float r4 = java.lang.Math.abs(r0)
            float r5 = r6.mPagingTouchSlop
            int r4 = (r4 > r5 ? 1 : (r4 == r5 ? 0 : -1))
            if (r4 <= 0) goto L_0x0101
            float r0 = java.lang.Math.abs(r0)
            float r1 = java.lang.Math.abs(r1)
            int r0 = (r0 > r1 ? 1 : (r0 == r1 ? 0 : -1))
            if (r0 <= 0) goto L_0x0101
            com.android.systemui.SwipeHelper$Callback r0 = r6.mCallback
            android.view.View r1 = r6.mCurrView
            r0.onBeginDrag(r1)
            r6.mDragging = r2
            float r7 = r6.getPos(r7)
            r6.mInitialTouchPos = r7
            android.view.View r7 = r6.mCurrView
            float r7 = r6.getTranslation(r7)
            r6.mTranslation = r7
            r6.removeLongPressCallback()
            goto L_0x0101
        L_0x0060:
            boolean r7 = r6.mDragging
            if (r7 != 0) goto L_0x006b
            boolean r7 = r6.mLongPressSent
            if (r7 == 0) goto L_0x0069
            goto L_0x006b
        L_0x0069:
            r7 = r3
            goto L_0x006c
        L_0x006b:
            r7 = r2
        L_0x006c:
            r6.mDragging = r3
            r6.mCurrView = r1
            r6.mLongPressSent = r3
            r6.removeLongPressCallback()
            if (r7 == 0) goto L_0x0101
            return r2
        L_0x0078:
            r6.mTouchAboveFalsingThreshold = r3
            r6.mDragging = r3
            r6.mSnappingChild = r3
            r6.mLongPressSent = r3
            android.view.VelocityTracker r0 = r6.mVelocityTracker
            r0.clear()
            com.android.systemui.SwipeHelper$Callback r0 = r6.mCallback
            android.view.View r0 = r0.getChildAtPosition(r7)
            r6.mCurrView = r0
            boolean r4 = r0 instanceof com.android.systemui.statusbar.ExpandableNotificationRow
            if (r4 == 0) goto L_0x00bf
            com.android.systemui.statusbar.ExpandableNotificationRow r0 = (com.android.systemui.statusbar.ExpandableNotificationRow) r0
            com.android.systemui.statusbar.NotificationData$Entry r4 = r0.getEntry()
            com.android.systemui.miui.statusbar.ExpandedNotification r4 = r4.notification
            boolean r4 = r4.isPersistent()
            java.lang.String r5 = "com.android.systemui.SwipeHelper"
            if (r4 == 0) goto L_0x00a8
            r6.mCurrView = r1
            java.lang.String r4 = "ignoring persistent notifications"
            android.util.Log.v(r5, r4)
        L_0x00a8:
            com.android.systemui.statusbar.NotificationGuts r4 = r0.getGuts()
            if (r4 == 0) goto L_0x00bf
            com.android.systemui.statusbar.NotificationGuts r0 = r0.getGuts()
            boolean r0 = r0.isExposed()
            if (r0 == 0) goto L_0x00bf
            r6.mCurrView = r1
            java.lang.String r0 = "ignoring guts exposed"
            android.util.Log.v(r5, r0)
        L_0x00bf:
            android.view.View r0 = r6.mCurrView
            if (r0 == 0) goto L_0x0101
            r6.onDownUpdate(r0, r7)
            com.android.systemui.SwipeHelper$Callback r0 = r6.mCallback
            android.view.View r1 = r6.mCurrView
            boolean r0 = r0.canChildBeDismissed(r1)
            r6.mCanCurrViewBeDimissed = r0
            android.view.VelocityTracker r0 = r6.mVelocityTracker
            r0.addMovement(r7)
            float r0 = r6.getPos(r7)
            r6.mInitialTouchPos = r0
            float r0 = r6.getPerpendicularPos(r7)
            r6.mPerpendicularInitialTouchPos = r0
            android.view.View r0 = r6.mCurrView
            float r0 = r6.getTranslation(r0)
            r6.mTranslation = r0
            com.android.systemui.SwipeHelper$LongPressListener r0 = r6.mLongPressListener
            if (r0 == 0) goto L_0x0101
            java.lang.Runnable r0 = r6.mWatchLongPress
            if (r0 != 0) goto L_0x00f8
            com.android.systemui.SwipeHelper$1 r0 = new com.android.systemui.SwipeHelper$1
            r0.<init>(r7)
            r6.mWatchLongPress = r0
        L_0x00f8:
            android.os.Handler r7 = r6.mHandler
            java.lang.Runnable r0 = r6.mWatchLongPress
            long r4 = r6.mLongPressTimeout
            r7.postDelayed(r0, r4)
        L_0x0101:
            boolean r7 = r6.mDragging
            if (r7 != 0) goto L_0x010b
            boolean r6 = r6.mLongPressSent
            if (r6 == 0) goto L_0x010a
            goto L_0x010b
        L_0x010a:
            r2 = r3
        L_0x010b:
            return r2
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.SwipeHelper.onInterceptTouchEvent(android.view.MotionEvent):boolean");
    }

    public void dispatchDismissAllToChild(List<View> list, Runnable runnable) {
        if (!list.isEmpty()) {
            doRowAnimations(list, (Runnable) null);
            if (runnable != null) {
                runnable.run();
            }
            Log.d("com.android.systemui.SwipeHelper", "dispatchDismissAllToChild onAnimationEnd.");
        }
    }

    private void doRowAnimations(List<View> list, Runnable runnable) {
        int size = list.size() - 1;
        int i = 100;
        int i2 = 0;
        while (size >= 0) {
            dismissChild(list.get(size), 0.0f, size == 0 ? runnable : null, (long) i2, true, 200, true);
            i = Math.max(30, i - 10);
            i2 += i;
            size--;
        }
    }

    public void dismissChild(View view, float f, boolean z) {
        dismissChild(view, f, (Runnable) null, 0, z, 0, false);
    }

    public void dismissChild(final View view, float f, Runnable runnable, long j, boolean z, long j2, boolean z2) {
        float f2;
        long j3;
        View view2 = view;
        long j4 = j;
        final boolean canChildBeDismissed = this.mCallback.canChildBeDismissed(view);
        boolean z3 = false;
        boolean z4 = view.getLayoutDirection() == 1;
        int i = (f > 0.0f ? 1 : (f == 0.0f ? 0 : -1));
        boolean z5 = i == 0 && (getTranslation(view) == 0.0f || z2) && this.mSwipeDirection == 1;
        boolean z6 = i == 0 && (getTranslation(view) == 0.0f || z2) && z4;
        if ((Math.abs(f) > getEscapeVelocity() && f < 0.0f) || (getTranslation(view) < 0.0f && !z2)) {
            z3 = true;
        }
        if (z3 || z6 || z5) {
            f2 = -getSize(view);
        } else {
            f2 = getSize(view);
            if ((view2 instanceof ExpandableNotificationRow) && z2) {
                f2 = Math.max((float) this.mScreenWidth, f2);
            }
        }
        float f3 = f2;
        if (j2 == 0) {
            j3 = i != 0 ? Math.min(400, (long) ((int) ((Math.abs(f3 - getTranslation(view)) * 1000.0f) / Math.abs(f)))) : 200;
        } else {
            j3 = j2;
        }
        if (!this.mDisableHwLayers) {
            view.setLayerType(2, (Paint) null);
        }
        Animator viewTranslationAnimator = getViewTranslationAnimator(view, f3, new ValueAnimator.AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                SwipeHelper.this.onTranslationUpdate(view, ((Float) valueAnimator.getAnimatedValue()).floatValue(), canChildBeDismissed);
            }
        });
        if (viewTranslationAnimator != null) {
            if (z) {
                viewTranslationAnimator.setInterpolator(Interpolators.FAST_OUT_LINEAR_IN);
                viewTranslationAnimator.setDuration(j3);
            } else {
                this.mFlingAnimationUtils.applyDismissing(viewTranslationAnimator, getTranslation(view), f3, f, getSize(view));
            }
            if (j4 > 0) {
                viewTranslationAnimator.setStartDelay(j4);
            }
            final View view3 = view;
            final boolean z7 = z2;
            final boolean z8 = canChildBeDismissed;
            final Runnable runnable2 = runnable;
            viewTranslationAnimator.addListener(new AnimatorListenerAdapter() {
                private boolean mCancelled;

                public void onAnimationCancel(Animator animator) {
                    this.mCancelled = true;
                }

                public void onAnimationEnd(Animator animator) {
                    ScenarioTrackUtil.SystemUIEventScenario systemUIEventScenario;
                    View view = view3;
                    if ((view instanceof ExpandableNotificationRow) && z7) {
                        ((ExpandableNotificationRow) view).setDismissed();
                    }
                    SwipeHelper.this.updateSwipeProgressFromOffset(view3, z8);
                    SwipeHelper.this.mDismissPendingMap.remove(view3);
                    if (!this.mCancelled) {
                        SwipeHelper.this.mCallback.onChildDismissed(view3);
                    }
                    Runnable runnable = runnable2;
                    if (runnable != null) {
                        runnable.run();
                    }
                    if (!SwipeHelper.this.mDisableHwLayers) {
                        view3.setLayerType(0, (Paint) null);
                    }
                    if (z7) {
                        systemUIEventScenario = ScenarioConstants.SCENARIO_CLEAR_ALL_NOTI;
                    } else {
                        systemUIEventScenario = ScenarioConstants.SCENARIO_CLEAR_NOTI;
                    }
                    ScenarioTrackUtil.finishScenario(systemUIEventScenario);
                }
            });
            prepareDismissAnimation(view, viewTranslationAnimator);
            this.mDismissPendingMap.put(view, viewTranslationAnimator);
            viewTranslationAnimator.start();
        }
    }

    public void snapChild(final View view, final float f, float f2) {
        final boolean canChildBeDismissed = this.mCallback.canChildBeDismissed(view);
        Animator viewTranslationAnimator = getViewTranslationAnimator(view, f, new ValueAnimator.AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                SwipeHelper.this.onTranslationUpdate(view, ((Float) valueAnimator.getAnimatedValue()).floatValue(), canChildBeDismissed);
            }
        });
        if (viewTranslationAnimator != null) {
            viewTranslationAnimator.setDuration((long) 150);
            viewTranslationAnimator.addListener(new AnimatorListenerAdapter() {
                boolean wasCancelled = false;

                public void onAnimationCancel(Animator animator) {
                    this.wasCancelled = true;
                }

                public void onAnimationEnd(Animator animator) {
                    boolean unused = SwipeHelper.this.mSnappingChild = false;
                    if (!this.wasCancelled) {
                        SwipeHelper.this.updateSwipeProgressFromOffset(view, canChildBeDismissed);
                        SwipeHelper.this.mCallback.onChildSnappedBack(view, f);
                    }
                }
            });
            prepareSnapBackAnimation(view, viewTranslationAnimator);
            this.mSnappingChild = true;
            viewTranslationAnimator.start();
        }
    }

    public void onTranslationUpdate(View view, float f, boolean z) {
        updateSwipeProgressFromOffset(view, z, f);
    }

    private void snapChildInstantly(View view) {
        boolean canChildBeDismissed = this.mCallback.canChildBeDismissed(view);
        setTranslation(view, 0.0f);
        updateSwipeProgressFromOffset(view, canChildBeDismissed);
    }

    /* JADX WARNING: Code restructure failed: missing block: B:11:0x0025, code lost:
        if (getTranslation(r5) != 0.0f) goto L_0x001d;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void snapChildIfNeeded(android.view.View r5, boolean r6, float r7) {
        /*
            r4 = this;
            boolean r0 = r4.mDragging
            if (r0 == 0) goto L_0x0008
            android.view.View r0 = r4.mCurrView
            if (r0 == r5) goto L_0x000c
        L_0x0008:
            boolean r0 = r4.mSnappingChild
            if (r0 == 0) goto L_0x000d
        L_0x000c:
            return
        L_0x000d:
            r0 = 0
            java.util.HashMap<android.view.View, android.animation.Animator> r1 = r4.mDismissPendingMap
            java.lang.Object r1 = r1.get(r5)
            android.animation.Animator r1 = (android.animation.Animator) r1
            r2 = 1
            r3 = 0
            if (r1 == 0) goto L_0x001f
            r1.cancel()
        L_0x001d:
            r0 = r2
            goto L_0x0028
        L_0x001f:
            float r1 = r4.getTranslation(r5)
            int r1 = (r1 > r3 ? 1 : (r1 == r3 ? 0 : -1))
            if (r1 == 0) goto L_0x0028
            goto L_0x001d
        L_0x0028:
            if (r0 == 0) goto L_0x0033
            if (r6 == 0) goto L_0x0030
            r4.snapChild(r5, r7, r3)
            goto L_0x0033
        L_0x0030:
            r4.snapChildInstantly(r5)
        L_0x0033:
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.SwipeHelper.snapChildIfNeeded(android.view.View, boolean, float):void");
    }

    /* JADX WARNING: Code restructure failed: missing block: B:18:0x002e, code lost:
        if (r0 != 4) goto L_0x00f7;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean onTouchEvent(android.view.MotionEvent r11) {
        /*
            r10 = this;
            boolean r0 = r10.mLongPressSent
            r1 = 1
            if (r0 == 0) goto L_0x0006
            return r1
        L_0x0006:
            boolean r0 = r10.mDragging
            r2 = 0
            if (r0 != 0) goto L_0x001b
            com.android.systemui.SwipeHelper$Callback r0 = r10.mCallback
            android.view.View r0 = r0.getChildAtPosition(r11)
            if (r0 == 0) goto L_0x0017
            r10.onInterceptTouchEvent(r11)
            return r1
        L_0x0017:
            r10.removeLongPressCallback()
            return r2
        L_0x001b:
            android.view.VelocityTracker r0 = r10.mVelocityTracker
            r0.addMovement(r11)
            int r0 = r11.getAction()
            r3 = 0
            if (r0 == r1) goto L_0x00a3
            r4 = 2
            if (r0 == r4) goto L_0x0032
            r4 = 3
            if (r0 == r4) goto L_0x00a3
            r2 = 4
            if (r0 == r2) goto L_0x0032
            goto L_0x00f7
        L_0x0032:
            android.view.View r0 = r10.mCurrView
            if (r0 == 0) goto L_0x00f7
            float r0 = r10.getPos(r11)
            float r2 = r10.mInitialTouchPos
            float r0 = r0 - r2
            float r2 = java.lang.Math.abs(r0)
            int r4 = r10.getFalsingThreshold()
            float r4 = (float) r4
            int r4 = (r2 > r4 ? 1 : (r2 == r4 ? 0 : -1))
            if (r4 < 0) goto L_0x004c
            r10.mTouchAboveFalsingThreshold = r1
        L_0x004c:
            com.android.systemui.SwipeHelper$Callback r4 = r10.mCallback
            android.view.View r5 = r10.mCurrView
            boolean r4 = r4.canChildBeDismissed(r5)
            if (r4 != 0) goto L_0x007a
            android.view.View r4 = r10.mCurrView
            float r4 = r10.getSize(r4)
            r5 = 1050253722(0x3e99999a, float:0.3)
            float r5 = r5 * r4
            int r2 = (r2 > r4 ? 1 : (r2 == r4 ? 0 : -1))
            if (r2 < 0) goto L_0x006c
            int r0 = (r0 > r3 ? 1 : (r0 == r3 ? 0 : -1))
            if (r0 <= 0) goto L_0x006a
            r0 = r5
            goto L_0x007a
        L_0x006a:
            float r0 = -r5
            goto L_0x007a
        L_0x006c:
            float r0 = r0 / r4
            double r6 = (double) r0
            r8 = 4609753056924675352(0x3ff921fb54442d18, double:1.5707963267948966)
            double r6 = r6 * r8
            double r6 = java.lang.Math.sin(r6)
            float r0 = (float) r6
            float r0 = r0 * r5
        L_0x007a:
            float r2 = r10.mTranslation
            float r2 = r2 + r0
            int r3 = (r2 > r3 ? 1 : (r2 == r3 ? 0 : -1))
            if (r3 >= 0) goto L_0x0091
            android.view.View r3 = r10.mCurrView
            boolean r3 = com.android.systemui.statusbar.stack.NotificationStackScrollLayout.isPinnedHeadsUp(r3)
            if (r3 != 0) goto L_0x0091
            int r3 = r10.mMaxSwipeTranslation
            int r3 = -r3
            float r3 = (float) r3
            float r2 = java.lang.Math.max(r2, r3)
        L_0x0091:
            android.view.View r3 = r10.mCurrView
            r10.setTranslation(r3, r2)
            android.view.View r3 = r10.mCurrView
            boolean r4 = r10.mCanCurrViewBeDimissed
            r10.updateSwipeProgressFromOffset(r3, r4)
            android.view.View r3 = r10.mCurrView
            r10.onMoveUpdate(r3, r11, r2, r0)
            goto L_0x00f7
        L_0x00a3:
            android.view.View r0 = r10.mCurrView
            if (r0 != 0) goto L_0x00a8
            goto L_0x00f7
        L_0x00a8:
            android.view.VelocityTracker r0 = r10.mVelocityTracker
            r4 = 1000(0x3e8, float:1.401E-42)
            float r5 = r10.getMaxVelocity()
            r0.computeCurrentVelocity(r4, r5)
            android.view.VelocityTracker r0 = r10.mVelocityTracker
            float r0 = r10.getVelocity(r0)
            android.view.View r4 = r10.mCurrView
            float r5 = r10.getTranslation(r4)
            boolean r4 = r10.handleUpEvent(r11, r4, r0, r5)
            if (r4 != 0) goto L_0x00f5
            boolean r11 = r10.isDismissGesture(r11)
            if (r11 == 0) goto L_0x00e6
            java.lang.Class<com.android.systemui.HapticFeedBackImpl> r11 = com.android.systemui.HapticFeedBackImpl.class
            java.lang.Object r11 = com.android.systemui.Dependency.get(r11)
            com.android.systemui.HapticFeedBackImpl r11 = (com.android.systemui.HapticFeedBackImpl) r11
            r11.clearNotification()
            com.android.systemui.statistic.ScenarioTrackUtil$SystemUIEventScenario r11 = com.android.systemui.statistic.ScenarioConstants.SCENARIO_CLEAR_NOTI
            com.android.systemui.statistic.ScenarioTrackUtil.beginScenario(r11)
            android.view.View r11 = r10.mCurrView
            boolean r3 = r10.swipedFastEnough()
            r3 = r3 ^ r1
            r10.dismissChild(r11, r0, r3)
            goto L_0x00f2
        L_0x00e6:
            com.android.systemui.SwipeHelper$Callback r11 = r10.mCallback
            android.view.View r4 = r10.mCurrView
            r11.onDragCancelled(r4)
            android.view.View r11 = r10.mCurrView
            r10.snapChild(r11, r3, r0)
        L_0x00f2:
            r11 = 0
            r10.mCurrView = r11
        L_0x00f5:
            r10.mDragging = r2
        L_0x00f7:
            return r1
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.SwipeHelper.onTouchEvent(android.view.MotionEvent):boolean");
    }

    private int getFalsingThreshold() {
        return (int) (((float) this.mFalsingThreshold) * this.mCallback.getFalsingThresholdFactor());
    }

    private float getMaxVelocity() {
        return this.mDensityScale * 4000.0f;
    }

    /* access modifiers changed from: protected */
    public float getEscapeVelocity() {
        return getUnscaledEscapeVelocity() * this.mDensityScale;
    }

    /* access modifiers changed from: protected */
    public boolean swipedFarEnough() {
        return Math.abs(getTranslation(this.mCurrView)) > getSize(this.mCurrView) * 0.6f;
    }

    public boolean isDismissGesture(MotionEvent motionEvent) {
        if (motionEvent.getActionMasked() != 1 || isFalseGesture(motionEvent) || ((!swipedFastEnough() && !swipedFarEnough()) || !this.mCallback.canChildBeDismissed(this.mCurrView))) {
            return false;
        }
        return true;
    }

    public boolean isFalseGesture(MotionEvent motionEvent) {
        boolean isAntiFalsingNeeded = this.mCallback.isAntiFalsingNeeded();
        if (this.mFalsingManager.isClassiferEnabled()) {
            if (isAntiFalsingNeeded && this.mFalsingManager.isFalseTouch()) {
                return true;
            }
        } else if (isAntiFalsingNeeded && !this.mTouchAboveFalsingThreshold) {
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
