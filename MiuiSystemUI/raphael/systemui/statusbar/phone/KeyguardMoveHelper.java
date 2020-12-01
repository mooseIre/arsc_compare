package com.android.systemui.statusbar.phone;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import com.android.keyguard.KeyguardHorizontalMoveLeftViewContainer;
import com.android.keyguard.KeyguardHorizontalMoveRightViewContainer;
import com.android.keyguard.KeyguardHorizontalMoveView;
import com.android.keyguard.KeyguardUpdateMonitor;
import com.android.keyguard.KeyguardUpdateMonitorCallback;
import com.android.keyguard.MiuiKeyguardUtils;
import com.android.keyguard.analytics.AnalyticsHelper;
import com.android.keyguard.clock.MiuiKeyguardBaseClock;
import com.android.keyguard.faceunlock.FaceUnlockManager;
import com.android.keyguard.fod.MiuiGxzwManager;
import com.android.keyguard.magazine.LockScreenMagazinePreView;
import com.android.keyguard.magazine.LockScreenMagazineUtils;
import com.android.systemui.classifier.FalsingManager;
import com.android.systemui.plugins.IntentButtonProvider;
import com.android.systemui.plugins.R;
import com.android.systemui.statusbar.FlingAnimationUtils;
import com.android.systemui.statusbar.KeyguardAffordanceView;
import java.util.ArrayList;
import java.util.List;
import miui.view.animation.CubicEaseOutInterpolator;

public class KeyguardMoveHelper {
    private Runnable mAnimationEndRunnable = new Runnable() {
        public void run() {
            KeyguardMoveHelper.this.mCallback.onAnimationToSideEnded();
        }
    };
    /* access modifiers changed from: private */
    public final Callback mCallback;
    private boolean mCanShowGxzw = true;
    private int mCenterScreenTipsTranslation;
    private float mCenterScreenTouchSlopTranslation;
    /* access modifiers changed from: private */
    public final Context mContext;
    /* access modifiers changed from: private */
    public int mCurrentScreen = 1;
    private FalsingManager mFalsingManager;
    private FlingAnimationUtils mFlingAnimationUtils;
    private AnimatorListenerAdapter mFlingEndListener = new AnimatorListenerAdapter() {
        public void onAnimationEnd(Animator animator) {
            Animator unused = KeyguardMoveHelper.this.mSwipeAnimator = null;
            boolean unused2 = KeyguardMoveHelper.this.mSwipingInProgress = false;
        }
    };
    private int mHintGrowAmount;
    private float mInitialTouchX;
    private float mInitialTouchY;
    /* access modifiers changed from: private */
    public boolean mIsLockScreenMagazinePreViewVisible;
    private boolean mIsOnAffordanceRightIconTouchDown;
    /* access modifiers changed from: private */
    public boolean mIsRightMove;
    private int mKeyguardHorizontalGestureSlop;
    private KeyguardUpdateMonitor mKeyguardUpdateMonitor;
    private final KeyguardUpdateMonitorCallback mKeyguardUpdateMonitorCallback = new KeyguardUpdateMonitorCallback() {
        public void onLockScreenMagazinePreViewVisibilityChanged(boolean z) {
            boolean unused = KeyguardMoveHelper.this.mIsLockScreenMagazinePreViewVisible = z;
        }

        public void onStartedWakingUp() {
            KeyguardMoveHelper.this.mCallback.onHorizontalMove(0.0f, true);
        }
    };
    private KeyguardAffordanceView mLeftIcon;
    private KeyguardHorizontalMoveLeftViewContainer mLeftMoveView;
    private int mMinBackgroundRadius;
    private int mMinFlingVelocity;
    private int mMinTranslationAmount;
    private boolean mMotionCancelled;
    private KeyguardHorizontalMoveView.CallBack mMoveViewCallBack = new KeyguardHorizontalMoveView.CallBack() {
        public void onCompletedAnimationEnd(boolean z) {
            if (z) {
                KeyguardMoveHelper.this.mCallback.onHorizontalMove(0.0f, true);
                boolean unused = KeyguardMoveHelper.this.mSwipingInProgress = false;
            }
        }

        public void onCancelAnimationEnd(boolean z, boolean z2) {
            if (z) {
                KeyguardMoveHelper.this.mCallback.onHorizontalMove(0.0f, true);
                if (z2) {
                    boolean unused = KeyguardMoveHelper.this.mSwipingInProgress = false;
                } else {
                    KeyguardMoveHelper.this.mCallback.startBottomButtonLayoutAnimate(false, true);
                }
            }
        }

        public void onBackAnimationEnd(boolean z) {
            if (z) {
                KeyguardMoveHelper.this.mCallback.onHorizontalMove(0.0f, true);
                boolean unused = KeyguardMoveHelper.this.mSwipingInProgress = false;
            }
        }

        public void onAnimUpdate(float f) {
            KeyguardMoveHelper.this.mCallback.onHorizontalMove(f, true);
        }

        public IntentButtonProvider.IntentButton.IconState getMoveIconState(boolean z) {
            return KeyguardMoveHelper.this.mCallback.getBottomButtonIconState(z);
        }

        public ViewGroup getMoveIconLayout(boolean z) {
            if (z) {
                return KeyguardMoveHelper.this.mRightIconLayout;
            }
            return null;
        }

        public boolean isMoveInCenterScreen() {
            return KeyguardMoveHelper.this.isInCenterScreen();
        }

        public boolean isRightMove() {
            return KeyguardMoveHelper.this.mIsRightMove;
        }

        public void updateSwipingInProgress(boolean z) {
            boolean unused = KeyguardMoveHelper.this.mSwipingInProgress = z;
        }

        public void updateCanShowGxzw(boolean z) {
            KeyguardMoveHelper.this.setCanShowGxzw(z);
        }
    };
    private Animator mResetAnimator;
    private KeyguardAffordanceView mRightIcon;
    /* access modifiers changed from: private */
    public ViewGroup mRightIconLayout;
    private KeyguardHorizontalMoveRightViewContainer mRightMoveView;
    /* access modifiers changed from: private */
    public Animator mSwipeAnimator;
    /* access modifiers changed from: private */
    public boolean mSwipingInProgress;
    private int mTouchSlop;
    private int mTouchTargetSize;
    /* access modifiers changed from: private */
    public float mTranslation;
    private float mTranslationOnDown;
    private VelocityTracker mVelocityTracker;

    public interface Callback {
        IntentButtonProvider.IntentButton.IconState getBottomButtonIconState(boolean z);

        KeyguardAffordanceView getBottomIcon(boolean z);

        ViewGroup getBottomIconLayout(boolean z);

        View getFaceUnlockView();

        View getLeftView();

        View getLeftViewBg();

        List<View> getLockScreenView();

        float getMaxTranslationDistance();

        boolean isKeyguardWallpaperCarouselSwitchAnimating();

        boolean needsAntiFalsing();

        void onAnimationToSideEnded();

        void onAnimationToSideStarted(boolean z, float f, float f2);

        void onHorizontalMove(float f, boolean z);

        void onSwipingAborted();

        void onSwipingStarted();

        void startBottomButtonLayoutAnimate(boolean z, boolean z2);

        void triggerAction(boolean z, float f, float f2);
    }

    KeyguardMoveHelper(Callback callback, Context context) {
        this.mContext = context;
        this.mCallback = callback;
        this.mKeyguardUpdateMonitor = KeyguardUpdateMonitor.getInstance(this.mContext);
        this.mKeyguardUpdateMonitor.registerCallback(this.mKeyguardUpdateMonitorCallback);
        this.mKeyguardHorizontalGestureSlop = context.getResources().getDimensionPixelSize(R.dimen.keyguard_horizontal_gesture_slop);
        initIcons();
        KeyguardAffordanceView keyguardAffordanceView = this.mLeftIcon;
        updateIcon(keyguardAffordanceView, keyguardAffordanceView.getRestingAlpha(), false, true);
        KeyguardAffordanceView keyguardAffordanceView2 = this.mRightIcon;
        updateIcon(keyguardAffordanceView2, keyguardAffordanceView2.getRestingAlpha(), false, true);
        initDimens();
        this.mLeftMoveView = new KeyguardHorizontalMoveLeftViewContainer(context, this.mMoveViewCallBack);
        this.mRightMoveView = new KeyguardHorizontalMoveRightViewContainer(context, this.mMoveViewCallBack);
    }

    private void initDimens() {
        ViewConfiguration viewConfiguration = ViewConfiguration.get(this.mContext);
        this.mTouchSlop = viewConfiguration.getScaledPagingTouchSlop();
        this.mMinFlingVelocity = viewConfiguration.getScaledMinimumFlingVelocity();
        this.mMinTranslationAmount = this.mContext.getResources().getDimensionPixelSize(R.dimen.keyguard_min_swipe_amount);
        this.mMinBackgroundRadius = this.mContext.getResources().getDimensionPixelSize(R.dimen.keyguard_affordance_min_background_radius);
        this.mTouchTargetSize = this.mContext.getResources().getDimensionPixelSize(R.dimen.keyguard_affordance_touch_target_size);
        this.mHintGrowAmount = this.mContext.getResources().getDimensionPixelSize(R.dimen.hint_grow_amount_sideways);
        this.mFlingAnimationUtils = new FlingAnimationUtils(this.mContext, 0.4f);
        this.mFalsingManager = FalsingManager.getInstance(this.mContext);
        this.mCenterScreenTipsTranslation = this.mContext.getResources().getDimensionPixelSize(R.dimen.keyguard_horizontal_move_mis_operation_translation);
        this.mCenterScreenTouchSlopTranslation = (float) (this.mTouchSlop * 2);
    }

    private void initIcons() {
        this.mLeftIcon = this.mCallback.getBottomIcon(false);
        this.mRightIcon = this.mCallback.getBottomIcon(true);
        this.mRightIconLayout = this.mCallback.getBottomIconLayout(true);
    }

    public boolean onTouchEvent(MotionEvent motionEvent) {
        int actionMasked = motionEvent.getActionMasked();
        boolean z = false;
        if ((this.mMotionCancelled && actionMasked != 0) || this.mIsLockScreenMagazinePreViewVisible) {
            return false;
        }
        float y = motionEvent.getY();
        float x = motionEvent.getX();
        if (actionMasked != 0) {
            if (actionMasked == 1) {
                z = true;
            } else if (actionMasked == 2) {
                trackMovement(motionEvent);
                float f = x - this.mInitialTouchX;
                float f2 = y - this.mInitialTouchY;
                float realDist = getRealDist(f);
                float realDist2 = getRealDist(f2);
                this.mIsRightMove = f > 0.0f;
                if (this.mSwipingInProgress) {
                    KeyguardHorizontalMoveRightViewContainer keyguardHorizontalMoveRightViewContainer = this.mRightMoveView;
                    float f3 = this.mCenterScreenTouchSlopTranslation;
                    if (!keyguardHorizontalMoveRightViewContainer.onTouchMove(x + f3, f3 + y) && ((isInCenterScreen() && this.mIsRightMove) || isInLeftView())) {
                        if (this.mKeyguardUpdateMonitor.isLockScreenLeftOverlayAvailable()) {
                            KeyguardHorizontalMoveLeftViewContainer keyguardHorizontalMoveLeftViewContainer = this.mLeftMoveView;
                            float f4 = this.mCenterScreenTouchSlopTranslation;
                            keyguardHorizontalMoveLeftViewContainer.onTouchMove(x + f4, y + f4);
                        } else {
                            if (!this.mIsRightMove) {
                                realDist = -realDist;
                            }
                            setTranslation(realDist, false, false, false);
                        }
                    }
                } else if (realDist > 0.0f || realDist2 > 0.0f || this.mIsOnAffordanceRightIconTouchDown) {
                    if ((Math.abs(f) <= Math.abs(f2) || (!isCenterRightMove() && !isCenterLeftMove() && isInCenterScreen())) && !pressRightIconLeftMove()) {
                        this.mMotionCancelled = true;
                    } else {
                        startSwiping();
                        KeyguardHorizontalMoveRightViewContainer keyguardHorizontalMoveRightViewContainer2 = this.mRightMoveView;
                        float f5 = this.mCenterScreenTouchSlopTranslation;
                        keyguardHorizontalMoveRightViewContainer2.onTouchDown(x + f5, f5 + y, this.mIsOnAffordanceRightIconTouchDown);
                        KeyguardHorizontalMoveLeftViewContainer keyguardHorizontalMoveLeftViewContainer2 = this.mLeftMoveView;
                        float f6 = this.mCenterScreenTouchSlopTranslation;
                        keyguardHorizontalMoveLeftViewContainer2.onTouchDown(x + f6, y + f6, true);
                        setTranslation(0.0f, false, false, false);
                        FaceUnlockManager.getInstance().stopFaceUnlock();
                    }
                }
            } else if (actionMasked != 3) {
                if (actionMasked == 5) {
                    this.mMotionCancelled = true;
                    endMotion(true, x, y);
                }
            }
            trackMovement(motionEvent);
            this.mRightMoveView.onTouchUp(x, y);
            this.mLeftMoveView.onTouchUp(x, y);
            if (!this.mKeyguardUpdateMonitor.isLockScreenLeftOverlayAvailable()) {
                endMotion(!z, x, y);
            }
        } else {
            this.mInitialTouchX = x;
            this.mInitialTouchY = y;
            this.mTranslationOnDown = this.mTranslation;
            initVelocityTracker();
            trackMovement(motionEvent);
            this.mMotionCancelled = false;
            this.mIsOnAffordanceRightIconTouchDown = isOnIcon(this.mRightIcon, x, y);
            if (isInCenterScreen() && !this.mIsOnAffordanceRightIconTouchDown) {
                cancelResetAnimation();
            }
        }
        return true;
    }

    private boolean pressRightIconLeftMove() {
        return isInCenterScreen() && !this.mIsRightMove && this.mRightIcon.getVisibility() == 0 && this.mIsOnAffordanceRightIconTouchDown;
    }

    private boolean isCenterLeftMove() {
        return isInCenterScreen() && !this.mIsRightMove && this.mRightIcon.getVisibility() == 0 && !this.mIsOnAffordanceRightIconTouchDown;
    }

    private boolean isCenterRightMove() {
        return isInCenterScreen() && this.mIsRightMove && this.mLeftIcon.getVisibility() == 0;
    }

    private float getRealDist(float f) {
        float abs = Math.abs(f) - this.mCenterScreenTouchSlopTranslation;
        if (abs < 0.0f) {
            return 0.0f;
        }
        return abs;
    }

    private void startSwiping() {
        this.mCallback.onSwipingStarted();
        this.mSwipingInProgress = true;
    }

    public boolean isInLeftView() {
        return this.mCurrentScreen == 0;
    }

    public boolean isInCenterScreen() {
        return this.mCurrentScreen == 1;
    }

    public boolean isOnAffordanceIcon(float f, float f2) {
        return isOnIcon(this.mLeftIcon, f, f2) || isOnIcon(this.mRightIcon, f, f2);
    }

    private boolean isOnIcon(View view, float f, float f2) {
        int[] iArr = new int[2];
        view.getLocationOnScreen(iArr);
        float width = ((float) view.getWidth()) / 2.0f;
        if (Math.hypot((double) (f - (((float) iArr[0]) + width)), (double) (f2 - (((float) iArr[1]) + width))) <= ((double) width)) {
            return true;
        }
        return false;
    }

    private void endMotion(boolean z, float f, float f2) {
        if (this.mSwipingInProgress) {
            flingWithCurrentVelocity(z, f, f2);
        }
        VelocityTracker velocityTracker = this.mVelocityTracker;
        if (velocityTracker != null) {
            velocityTracker.recycle();
            this.mVelocityTracker = null;
        }
    }

    private void cancelAnimation() {
        Animator animator = this.mSwipeAnimator;
        if (animator != null) {
            animator.cancel();
        }
    }

    private void cancelResetAnimation() {
        Animator animator = this.mResetAnimator;
        if (animator != null) {
            animator.cancel();
        }
    }

    private void flingWithCurrentVelocity(boolean z, float f, float f2) {
        float currentVelocity = getCurrentVelocity(f, f2);
        boolean z2 = true;
        boolean z3 = this.mCallback.needsAntiFalsing() && this.mFalsingManager.isClassiferEnabled() && this.mFalsingManager.isFalseTouch();
        boolean z4 = this.mTranslation * currentVelocity < 0.0f;
        boolean z5 = ((Math.abs(currentVelocity) > ((float) this.mMinFlingVelocity) && z4) || Math.abs(this.mInitialTouchX - f) < ((float) this.mKeyguardHorizontalGestureSlop)) | z3;
        if (z5 ^ z4) {
            currentVelocity = 0.0f;
        }
        boolean z6 = z5 || z;
        if (this.mTranslation >= 0.0f) {
            z2 = false;
        }
        fling(currentVelocity, z6, z2);
    }

    private void fling(final float f, final boolean z, final boolean z2) {
        float screenWidth = getScreenWidth();
        if (z2) {
            screenWidth = -screenWidth;
        }
        if (z) {
            screenWidth = 0.0f;
        }
        ValueAnimator ofFloat = ValueAnimator.ofFloat(new float[]{this.mTranslation, screenWidth});
        ofFloat.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                float unused = KeyguardMoveHelper.this.mTranslation = ((Float) valueAnimator.getAnimatedValue()).floatValue();
                if (!z) {
                    KeyguardMoveHelper keyguardMoveHelper = KeyguardMoveHelper.this;
                    keyguardMoveHelper.setTranslation(keyguardMoveHelper.mTranslation, false, false, true);
                }
            }
        });
        ofFloat.addListener(this.mFlingEndListener);
        ofFloat.addListener(new AnimatorListenerAdapter() {
            public void onAnimationEnd(Animator animator) {
                if (!z && KeyguardMoveHelper.this.mCurrentScreen == 0 && !KeyguardUpdateMonitor.getInstance(KeyguardMoveHelper.this.mContext).isLockScreenLeftOverlayAvailable()) {
                    KeyguardMoveHelper.this.mCallback.triggerAction(z2, KeyguardMoveHelper.this.mTranslation, f);
                }
            }
        });
        if (z) {
            reset(true);
        }
        ofFloat.setDuration(100);
        ofFloat.setInterpolator(new CubicEaseOutInterpolator());
        ofFloat.start();
        this.mSwipeAnimator = ofFloat;
        if (z) {
            this.mCallback.onSwipingAborted();
        }
    }

    /* access modifiers changed from: private */
    public void setTranslation(float f, boolean z, boolean z2, boolean z3) {
        setTranslation(f, z, z2, z3, false);
    }

    private void setTranslation(float f, boolean z, boolean z2, boolean z3, boolean z4) {
        float f2;
        float f3;
        float f4 = f;
        View leftView = this.mCallback.getLeftView();
        View leftViewBg = this.mCallback.getLeftViewBg();
        View faceUnlockView = this.mCallback.getFaceUnlockView();
        if (z4) {
            if (MiuiKeyguardUtils.isGxzwSensor()) {
                setCanShowGxzw(true);
            }
            faceUnlockView.setTranslationX(0.0f);
            faceUnlockView.setAlpha(1.0f);
            this.mCurrentScreen = 1;
            leftView.setTranslationX(-getScreenWidth());
            leftViewBg.setVisibility(4);
            animateShowLeftRightIcon();
            for (View next : this.mCallback.getLockScreenView()) {
                next.setTranslationX(0.0f);
                next.setAlpha(1.0f);
            }
            return;
        }
        if (this.mCurrentScreen != 1 || f4 <= 0.0f) {
            f2 = this.mCurrentScreen == 0 ? (f4 / getScreenWidth()) + 1.0f : 0.0f;
        } else {
            leftViewBg.setVisibility(0);
            f2 = f4 / getScreenWidth();
        }
        if (f2 < 0.0f) {
            f2 = 0.0f;
        } else if (f2 > 1.0f) {
            f2 = 1.0f;
        }
        if (this.mCurrentScreen != 0) {
            f3 = f4;
        } else if (f4 <= 0.0f) {
            f3 = getScreenWidth() + f4;
        } else {
            return;
        }
        if (f4 != this.mTranslation || z || z3) {
            if (!z2) {
                leftView.setTranslationX(f3 - getScreenWidth());
                leftViewBg.setAlpha(f2);
                leftView.setAlpha(this.mKeyguardUpdateMonitor.isSupportLockScreenMagazineLeft() ? f2 : 1.0f);
                for (View next2 : this.mCallback.getLockScreenView()) {
                    next2.setTranslationX(f3);
                    next2.setAlpha(1.0f - f2);
                }
                faceUnlockView.setTranslationX(f3);
                faceUnlockView.setAlpha(1.0f - f2);
            } else {
                AnimatorSet animatorSet = new AnimatorSet();
                ArrayList arrayList = new ArrayList();
                arrayList.add(ObjectAnimator.ofFloat(leftView, View.TRANSLATION_X, new float[]{leftView.getTranslationX(), f3 - getScreenWidth()}));
                arrayList.add(ObjectAnimator.ofFloat(faceUnlockView, View.TRANSLATION_X, new float[]{faceUnlockView.getTranslationX(), f3}));
                arrayList.add(ObjectAnimator.ofFloat(leftViewBg, "alpha", new float[]{leftViewBg.getAlpha(), f2}));
                float f5 = 1.0f - f2;
                arrayList.add(ObjectAnimator.ofFloat(faceUnlockView, "alpha", new float[]{faceUnlockView.getAlpha(), f5}));
                if (this.mKeyguardUpdateMonitor.isSupportLockScreenMagazineLeft()) {
                    arrayList.add(ObjectAnimator.ofFloat(leftView, "alpha", new float[]{leftView.getAlpha(), f2}));
                }
                for (View next3 : this.mCallback.getLockScreenView()) {
                    arrayList.add(ObjectAnimator.ofFloat(next3, View.TRANSLATION_X, new float[]{next3.getTranslationX(), f3}));
                    if ((!(next3 instanceof LockScreenMagazinePreView) && !(next3 instanceof MiuiKeyguardBaseClock)) || !this.mCallback.isKeyguardWallpaperCarouselSwitchAnimating()) {
                        arrayList.add(ObjectAnimator.ofFloat(next3, "alpha", new float[]{next3.getAlpha(), f5}));
                    }
                }
                animatorSet.playTogether(arrayList);
                animatorSet.setDuration(300);
                animatorSet.setInterpolator(new CubicEaseOutInterpolator());
                animatorSet.start();
                this.mResetAnimator = animatorSet;
            }
            this.mTranslation = f4;
        }
        if (this.mCurrentScreen == 0 && f4 == (-getScreenWidth())) {
            this.mCurrentScreen = 1;
            leftViewBg.setVisibility(4);
            if (MiuiKeyguardUtils.isGxzwSensor()) {
                setCanShowGxzw(true);
            }
            FaceUnlockManager.getInstance().startFaceUnlock();
            AnalyticsHelper.getInstance(this.mContext).trackPageStart("keyguard_view_main_lock_screen");
            LockScreenMagazineUtils.sendLockScreenMagazineEventBroadcast(this.mContext, "Wallpaper_Uncovered");
        } else if (this.mCurrentScreen == 1 && f4 == (-getScreenWidth())) {
            this.mCurrentScreen = 2;
            if (MiuiKeyguardUtils.isGxzwSensor()) {
                setCanShowGxzw(false);
            }
            AnalyticsHelper.getInstance(this.mContext).trackPageEnd("keyguard_view_main_lock_screen", "enter_right_view");
        } else if (this.mCurrentScreen == 1 && f4 == getScreenWidth()) {
            this.mCurrentScreen = 0;
            if (MiuiKeyguardUtils.isGxzwSensor()) {
                setCanShowGxzw(false);
            }
            AnalyticsHelper.getInstance(this.mContext).trackPageEnd("keyguard_view_main_lock_screen", "enter_left_view");
            AnalyticsHelper.getInstance(this.mContext).recordKeyguardAction("action_enter_left_view");
            AnalyticsHelper.getInstance(this.mContext).recordNegativeStatus();
            FaceUnlockManager.getInstance().stopFaceUnlock();
            if (!this.mKeyguardUpdateMonitor.isSupportLockScreenMagazineLeft()) {
                LockScreenMagazineUtils.sendLockScreenMagazineEventBroadcast(this.mContext, "Wallpaper_Covered");
            }
        } else if (this.mCurrentScreen == 1 && f4 == 0.0f) {
            if (MiuiKeyguardUtils.isGxzwSensor()) {
                setCanShowGxzw(true);
            }
        } else if (MiuiKeyguardUtils.isGxzwSensor()) {
            setCanShowGxzw(false);
        }
    }

    private float getScreenWidth() {
        return (float) this.mContext.getResources().getDisplayMetrics().widthPixels;
    }

    public void animateShowLeftRightIcon() {
        cancelAnimation();
        KeyguardAffordanceView keyguardAffordanceView = this.mRightIcon;
        updateIcon(keyguardAffordanceView, keyguardAffordanceView.getRestingAlpha(), true, false);
        KeyguardAffordanceView keyguardAffordanceView2 = this.mLeftIcon;
        updateIcon(keyguardAffordanceView2, keyguardAffordanceView2.getRestingAlpha(), true, false);
    }

    private void updateIcon(KeyguardAffordanceView keyguardAffordanceView, float f, boolean z, boolean z2) {
        if (keyguardAffordanceView.getVisibility() == 0 || z2) {
            updateIconAlpha(keyguardAffordanceView, f, z);
        }
    }

    private void updateIconAlpha(KeyguardAffordanceView keyguardAffordanceView, float f, boolean z) {
        float scale = getScale(f, keyguardAffordanceView);
        keyguardAffordanceView.setImageAlpha(Math.min(1.0f, f), z);
        keyguardAffordanceView.setImageScale(scale, z);
    }

    private float getScale(float f, KeyguardAffordanceView keyguardAffordanceView) {
        return Math.min(((f / keyguardAffordanceView.getRestingAlpha()) * 0.2f) + 0.8f, 1.5f);
    }

    private void trackMovement(MotionEvent motionEvent) {
        VelocityTracker velocityTracker = this.mVelocityTracker;
        if (velocityTracker != null) {
            velocityTracker.addMovement(motionEvent);
        }
    }

    private void initVelocityTracker() {
        VelocityTracker velocityTracker = this.mVelocityTracker;
        if (velocityTracker != null) {
            velocityTracker.recycle();
        }
        this.mVelocityTracker = VelocityTracker.obtain();
    }

    private float getCurrentVelocity(float f, float f2) {
        VelocityTracker velocityTracker = this.mVelocityTracker;
        if (velocityTracker == null) {
            return 0.0f;
        }
        velocityTracker.computeCurrentVelocity(1000);
        return this.mVelocityTracker.getXVelocity();
    }

    public void onConfigurationChanged() {
        initDimens();
        initIcons();
    }

    public void onRtlPropertiesChanged() {
        initIcons();
    }

    public void reset(boolean z) {
        reset(z, false);
    }

    private void reset(boolean z, boolean z2, boolean z3) {
        cancelAnimation();
        setTranslation(0.0f, true, z, z2, z3);
        this.mMotionCancelled = true;
        if (this.mSwipingInProgress) {
            this.mCallback.onSwipingAborted();
            this.mSwipingInProgress = false;
        }
    }

    public void reset(boolean z, boolean z2) {
        reset(z, z2, false);
    }

    public void resetImmediately() {
        reset(false, true, true);
    }

    public boolean isSwipingInProgress() {
        return this.mSwipingInProgress;
    }

    public void launchAffordance(boolean z, boolean z2) {
        float f;
        if (!this.mSwipingInProgress) {
            KeyguardAffordanceView keyguardAffordanceView = z2 ? this.mLeftIcon : this.mRightIcon;
            KeyguardAffordanceView keyguardAffordanceView2 = z2 ? this.mRightIcon : this.mLeftIcon;
            if (z) {
                fling(0.0f, false, !z2);
                updateIcon(keyguardAffordanceView2, 0.0f, true, true);
                return;
            }
            this.mCallback.onAnimationToSideStarted(!z2, this.mTranslation, 0.0f);
            if (z2) {
                f = this.mCallback.getMaxTranslationDistance();
            } else {
                f = this.mCallback.getMaxTranslationDistance();
            }
            this.mTranslation = f;
            updateIcon(keyguardAffordanceView2, 0.0f, false, true);
            keyguardAffordanceView.instantFinishAnimation();
            this.mFlingEndListener.onAnimationEnd((Animator) null);
            this.mAnimationEndRunnable.run();
        }
    }

    /* access modifiers changed from: private */
    public void setCanShowGxzw(boolean z) {
        this.mCanShowGxzw = z;
        if (MiuiKeyguardUtils.isGxzwSensor()) {
            MiuiGxzwManager.getInstance().updateGxzwState();
        }
    }

    public boolean canShowGxzw() {
        return this.mCanShowGxzw;
    }

    public KeyguardHorizontalMoveLeftViewContainer getLeftMoveView() {
        return this.mLeftMoveView;
    }

    public KeyguardHorizontalMoveRightViewContainer getRightMoveView() {
        return this.mRightMoveView;
    }
}
