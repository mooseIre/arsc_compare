package com.android.keyguard;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.ImageView;
import com.android.keyguard.BaseKeyguardMoveController;
import com.android.keyguard.analytics.AnalyticsHelper;
import com.android.keyguard.clock.MiuiKeyguardBaseClock;
import com.android.keyguard.faceunlock.MiuiKeyguardFaceUnlockView;
import com.android.keyguard.fod.MiuiGxzwManager;
import com.android.keyguard.injector.KeyguardBottomAreaInjector;
import com.android.keyguard.injector.KeyguardNegative1PageInjector;
import com.android.keyguard.magazine.LockScreenMagazineController;
import com.android.keyguard.magazine.LockScreenMagazinePreView;
import com.android.keyguard.magazine.utils.LockScreenMagazineUtils;
import com.android.keyguard.negative.MiuiKeyguardMoveLeftViewContainer;
import com.android.systemui.C0012R$dimen;
import com.android.systemui.Dependency;
import com.android.systemui.plugins.FalsingManager;
import com.android.systemui.plugins.IntentButtonProvider;
import com.android.systemui.statusbar.KeyguardAffordanceView;
import com.android.systemui.statusbar.phone.KeyguardBottomAreaView;
import com.android.systemui.statusbar.phone.MiuiNotificationPanelViewController;
import com.miui.systemui.DebugConfig;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import miui.view.animation.CubicEaseOutInterpolator;

public class KeyguardMoveHelper {
    private Runnable mAnimationEndRunnable = new Runnable() {
        public void run() {
            KeyguardMoveHelper.this.mCallback.onAnimationToSideEnded();
        }
    };
    /* access modifiers changed from: private */
    public KeyguardBottomAreaView mBottomAreaView;
    /* access modifiers changed from: private */
    public final Callback mCallback;
    private float mCenterScreenTouchSlopTranslation;
    private final Context mContext;
    /* access modifiers changed from: private */
    public int mCurrentScreen = 1;
    private MiuiKeyguardFaceUnlockView mFaceUnlockView;
    private FalsingManager mFalsingManager;
    private AnimatorListenerAdapter mFlingEndListener = new AnimatorListenerAdapter() {
        public void onAnimationEnd(Animator animator) {
            Animator unused = KeyguardMoveHelper.this.mSwipeAnimator = null;
            boolean unused2 = KeyguardMoveHelper.this.mSwipingInProgress = false;
        }
    };
    private float mInitialTouchX;
    private float mInitialTouchY;
    private boolean mIsCameraPreviewMoving;
    /* access modifiers changed from: private */
    public boolean mIsRightMove;
    private boolean mIsTouchRightIcon;
    private int mKeyguardHorizontalGestureSlop;
    private KeyguardUpdateMonitor mKeyguardUpdateMonitor;
    private KeyguardAffordanceView mLeftIcon;
    private KeyguardMoveLeftController mLeftMoveController;
    private int mMinFlingVelocity;
    private boolean mMotionCancelled;
    private BaseKeyguardMoveController.CallBack mMoveViewCallBack = new BaseKeyguardMoveController.CallBack() {
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
                    KeyguardMoveHelper.this.mBottomAreaView.startButtonLayoutAnimate(false, true);
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
            return KeyguardMoveHelper.this.mBottomAreaView.getIconState(z);
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
            MiuiGxzwManager.getInstance().setCanShowGxzw(z);
        }
    };
    private Animator mResetAnimator;
    private KeyguardAffordanceView mRightIcon;
    /* access modifiers changed from: private */
    public ViewGroup mRightIconLayout;
    private KeyguardMoveRightController mRightMoveController;
    /* access modifiers changed from: private */
    public Animator mSwipeAnimator;
    /* access modifiers changed from: private */
    public boolean mSwipingInProgress;
    /* access modifiers changed from: private */
    public float mTranslation;
    private VelocityTracker mVelocityTracker;

    public interface Callback {
        float getMaxTranslationDistance();

        List<View> getMobileView();

        boolean isKeyguardWallpaperCarouselSwitchAnimating();

        boolean needsAntiFalsing();

        void onAnimationToSideEnded();

        void onAnimationToSideStarted(boolean z, float f, float f2);

        void onHorizontalMove(float f, boolean z);

        void onSwipingAborted();

        void onSwipingStarted();

        void triggerAction(boolean z, float f, float f2);
    }

    public KeyguardMoveHelper(Callback callback, Context context, MiuiNotificationPanelViewController miuiNotificationPanelViewController) {
        this.mContext = context;
        this.mCallback = callback;
        this.mFalsingManager = miuiNotificationPanelViewController.getFalsingManager();
        this.mFaceUnlockView = miuiNotificationPanelViewController.getKeyguardFaceUnlockView();
        this.mKeyguardUpdateMonitor = (KeyguardUpdateMonitor) Dependency.get(KeyguardUpdateMonitor.class);
        this.mKeyguardHorizontalGestureSlop = context.getResources().getDimensionPixelSize(C0012R$dimen.keyguard_horizontal_gesture_slop);
        KeyguardBottomAreaView view = ((KeyguardBottomAreaInjector) Dependency.get(KeyguardBottomAreaInjector.class)).getView();
        this.mBottomAreaView = view;
        updateBottomIcons(view);
        KeyguardAffordanceView keyguardAffordanceView = this.mLeftIcon;
        updateIcon(keyguardAffordanceView, keyguardAffordanceView.getRestingAlpha(), false, true);
        KeyguardAffordanceView keyguardAffordanceView2 = this.mRightIcon;
        updateIcon(keyguardAffordanceView2, keyguardAffordanceView2.getRestingAlpha(), false, true);
        initDimens();
        this.mLeftMoveController = new KeyguardMoveLeftController(context, this.mMoveViewCallBack);
        this.mRightMoveController = new KeyguardMoveRightController(context, this.mMoveViewCallBack);
    }

    private void initDimens() {
        ViewConfiguration viewConfiguration = ViewConfiguration.get(this.mContext);
        this.mCenterScreenTouchSlopTranslation = (float) (viewConfiguration.getScaledPagingTouchSlop() * 2);
        this.mMinFlingVelocity = viewConfiguration.getScaledMinimumFlingVelocity();
        this.mContext.getResources().getDimensionPixelSize(C0012R$dimen.keyguard_min_swipe_amount);
    }

    public void updateBottomIcons(KeyguardBottomAreaView keyguardBottomAreaView) {
        this.mLeftIcon = keyguardBottomAreaView.getLeftView();
        this.mRightIcon = keyguardBottomAreaView.getRightView();
        this.mRightIconLayout = keyguardBottomAreaView.getRightViewLayout();
        this.mBottomAreaView = keyguardBottomAreaView;
    }

    public boolean onTouchEvent(MotionEvent motionEvent) {
        Class cls = LockScreenMagazineController.class;
        int actionMasked = motionEvent.getActionMasked();
        if ((!this.mMotionCancelled || actionMasked == 0) && !((LockScreenMagazineController) Dependency.get(cls)).isPreViewVisible()) {
            float y = motionEvent.getY();
            float x = motionEvent.getX();
            if (actionMasked != 0) {
                if (actionMasked == 1) {
                    finishAction(true, motionEvent, x, y);
                } else if (actionMasked == 2) {
                    trackMovement(motionEvent);
                    float f = x - this.mInitialTouchX;
                    float f2 = y - this.mInitialTouchY;
                    this.mIsRightMove = f > 0.0f;
                    float minusMisTouchOperationDist = minusMisTouchOperationDist(f);
                    float minusMisTouchOperationDist2 = minusMisTouchOperationDist(f2);
                    if (this.mSwipingInProgress) {
                        if (this.mIsCameraPreviewMoving) {
                            KeyguardMoveRightController keyguardMoveRightController = this.mRightMoveController;
                            float f3 = this.mCenterScreenTouchSlopTranslation;
                            keyguardMoveRightController.onTouchMove(x + f3, y + f3);
                        } else if (!isMovingLeftView()) {
                            KeyguardMoveRightController keyguardMoveRightController2 = this.mRightMoveController;
                            float f4 = this.mCenterScreenTouchSlopTranslation;
                            this.mIsCameraPreviewMoving = keyguardMoveRightController2.onTouchMove(x + f4, y + f4);
                        } else if (((LockScreenMagazineController) Dependency.get(cls)).isLockScreenLeftOverlayAvailable()) {
                            KeyguardMoveLeftController keyguardMoveLeftController = this.mLeftMoveController;
                            float f5 = this.mCenterScreenTouchSlopTranslation;
                            keyguardMoveLeftController.onTouchMove(x + f5, y + f5);
                        } else {
                            if (!this.mIsRightMove) {
                                minusMisTouchOperationDist = -minusMisTouchOperationDist;
                            }
                            setTranslation(minusMisTouchOperationDist, false, false, false);
                        }
                    } else if (isValidMovingStart(minusMisTouchOperationDist, minusMisTouchOperationDist2)) {
                        if (isValidHorizontalTouchDown(f, f2)) {
                            startSwiping();
                            KeyguardMoveRightController keyguardMoveRightController3 = this.mRightMoveController;
                            float f6 = this.mCenterScreenTouchSlopTranslation;
                            keyguardMoveRightController3.onTouchDown(x + f6, f6 + y, this.mIsTouchRightIcon);
                            KeyguardMoveLeftController keyguardMoveLeftController2 = this.mLeftMoveController;
                            float f7 = this.mCenterScreenTouchSlopTranslation;
                            keyguardMoveLeftController2.onTouchDown(x + f7, y + f7, true);
                            setTranslation(0.0f, false, false, false);
                            this.mKeyguardUpdateMonitor.cancelFaceAuth();
                        } else {
                            this.mMotionCancelled = true;
                            return false;
                        }
                    }
                } else if (actionMasked == 3) {
                    finishAction(false, motionEvent, x, y);
                } else if (actionMasked == 5) {
                    this.mMotionCancelled = true;
                    endMotion(true, x, y);
                }
                return true;
            }
            this.mInitialTouchX = x;
            this.mInitialTouchY = y;
            initVelocityTracker();
            trackMovement(motionEvent);
            this.mMotionCancelled = false;
            this.mIsTouchRightIcon = isOnIcon(this.mRightIcon, x, y);
            if (isInCenterScreen() && !this.mIsTouchRightIcon) {
                cancelResetAnimation();
            }
            return false;
        }
        if (DebugConfig.DEBUG_KEYGUARD) {
            Log.d("KeyguardMoveHelper", " horizontalMoveEvent is discarded Cancelled：" + this.mMotionCancelled + " isMagazinePreview:" + ((LockScreenMagazineController) Dependency.get(cls)).isPreViewVisible());
        }
        if (actionMasked == 3 || actionMasked == 1) {
            this.mMotionCancelled = false;
        }
        return false;
    }

    private void finishAction(boolean z, MotionEvent motionEvent, float f, float f2) {
        this.mIsCameraPreviewMoving = false;
        this.mMotionCancelled = false;
        trackMovement(motionEvent);
        this.mRightMoveController.onTouchUp(f, f2);
        this.mLeftMoveController.onTouchUp(f, f2);
        if (!((LockScreenMagazineController) Dependency.get(LockScreenMagazineController.class)).isLockScreenLeftOverlayAvailable()) {
            endMotion(!z, f, f2);
        }
    }

    private boolean isMovingLeftView() {
        return (isInCenterScreen() && this.mIsRightMove) || isInLeftView();
    }

    private boolean isValidHorizontalTouchDown(float f, float f2) {
        if ((!(Math.abs(f) > Math.abs(f2)) || (isInCenterScreen() && !canCenter2RightMove() && !canCenter2LeftMove())) && !rightIconPressedAndCanCenter2Left()) {
            return false;
        }
        return true;
    }

    private boolean isValidMovingStart(float f, float f2) {
        return f > 0.0f || f2 > 0.0f || this.mIsTouchRightIcon;
    }

    private boolean rightIconPressedAndCanCenter2Left() {
        return isInCenterScreen() && !this.mIsRightMove && this.mRightIcon.getVisibility() == 0 && this.mIsTouchRightIcon;
    }

    private boolean canCenter2LeftMove() {
        return isInCenterScreen() && !this.mIsRightMove && this.mRightIcon.getVisibility() == 0 && !this.mIsTouchRightIcon;
    }

    private boolean canCenter2RightMove() {
        return isInCenterScreen() && this.mIsRightMove && this.mLeftIcon.getVisibility() == 0;
    }

    private float minusMisTouchOperationDist(float f) {
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

    public boolean onInterceptTouchEvent(MotionEvent motionEvent) {
        if (motionEvent.getActionMasked() != 0) {
            return false;
        }
        onTouchEvent(motionEvent);
        return false;
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
        boolean z3 = this.mCallback.needsAntiFalsing() && this.mFalsingManager.isClassifierEnabled() && this.mFalsingManager.isFalseTouch();
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
        ofFloat.addUpdateListener(new ValueAnimator.AnimatorUpdateListener(z) {
            public final /* synthetic */ boolean f$1;

            {
                this.f$1 = r2;
            }

            public final void onAnimationUpdate(ValueAnimator valueAnimator) {
                KeyguardMoveHelper.this.lambda$fling$0$KeyguardMoveHelper(this.f$1, valueAnimator);
            }
        });
        ofFloat.addListener(this.mFlingEndListener);
        ofFloat.addListener(new AnimatorListenerAdapter() {
            public void onAnimationEnd(Animator animator) {
                if (!z && KeyguardMoveHelper.this.mCurrentScreen == 0 && !((LockScreenMagazineController) Dependency.get(LockScreenMagazineController.class)).isLockScreenLeftOverlayAvailable()) {
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
    /* renamed from: lambda$fling$0 */
    public /* synthetic */ void lambda$fling$0$KeyguardMoveHelper(boolean z, ValueAnimator valueAnimator) {
        float floatValue = ((Float) valueAnimator.getAnimatedValue()).floatValue();
        this.mTranslation = floatValue;
        if (!z) {
            setTranslation(floatValue, false, false, true);
        }
    }

    private void setTranslation(float f, boolean z, boolean z2, boolean z3) {
        setTranslation(f, z, z2, z3, false);
    }

    private void setTranslation(float f, boolean z, boolean z2, boolean z3, boolean z4) {
        float f2;
        float f3;
        float f4 = f;
        Class cls = KeyguardNegative1PageInjector.class;
        Class cls2 = LockScreenMagazineController.class;
        MiuiKeyguardMoveLeftViewContainer leftView = ((KeyguardNegative1PageInjector) Dependency.get(cls)).getLeftView();
        ImageView leftBackgroundView = ((KeyguardNegative1PageInjector) Dependency.get(cls)).getLeftBackgroundView();
        if (z4) {
            MiuiGxzwManager.getInstance().setCanShowGxzw(true);
            this.mFaceUnlockView.setTranslationX(0.0f);
            this.mFaceUnlockView.setAlpha(1.0f);
            this.mCurrentScreen = 1;
            leftView.setTranslationX(-getScreenWidth());
            leftBackgroundView.setVisibility(4);
            animateShowLeftRightIcon();
            for (View next : this.mCallback.getMobileView()) {
                next.setTranslationX(0.0f);
                next.setAlpha(1.0f);
            }
            return;
        }
        if (this.mCurrentScreen != 1 || f4 <= 0.0f) {
            f2 = this.mCurrentScreen == 0 ? (f4 / getScreenWidth()) + 1.0f : 0.0f;
        } else {
            leftBackgroundView.setVisibility(0);
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
                leftBackgroundView.setAlpha(f2);
                leftView.setAlpha(((LockScreenMagazineController) Dependency.get(cls2)).isSupportLockScreenMagazineLeft() ? f2 : 1.0f);
                for (View next2 : this.mCallback.getMobileView()) {
                    next2.setTranslationX(f3);
                    next2.setAlpha(1.0f - f2);
                }
                this.mFaceUnlockView.setTranslationX(f3);
                this.mFaceUnlockView.setAlpha(1.0f - f2);
            } else {
                AnimatorSet animatorSet = new AnimatorSet();
                ArrayList arrayList = new ArrayList();
                arrayList.add(ObjectAnimator.ofFloat(leftView, View.TRANSLATION_X, new float[]{leftView.getTranslationX(), f3 - getScreenWidth()}));
                MiuiKeyguardFaceUnlockView miuiKeyguardFaceUnlockView = this.mFaceUnlockView;
                arrayList.add(ObjectAnimator.ofFloat(miuiKeyguardFaceUnlockView, View.TRANSLATION_X, new float[]{miuiKeyguardFaceUnlockView.getTranslationX(), f3}));
                arrayList.add(ObjectAnimator.ofFloat(leftBackgroundView, "alpha", new float[]{leftBackgroundView.getAlpha(), f2}));
                MiuiKeyguardFaceUnlockView miuiKeyguardFaceUnlockView2 = this.mFaceUnlockView;
                arrayList.add(ObjectAnimator.ofFloat(miuiKeyguardFaceUnlockView2, "alpha", new float[]{miuiKeyguardFaceUnlockView2.getAlpha(), 1.0f - f2}));
                if (((LockScreenMagazineController) Dependency.get(cls2)).isSupportLockScreenMagazineLeft()) {
                    arrayList.add(ObjectAnimator.ofFloat(leftView, "alpha", new float[]{leftView.getAlpha(), f2}));
                }
                this.mCallback.getMobileView().forEach(new Consumer(arrayList, f3, f2) {
                    public final /* synthetic */ List f$1;
                    public final /* synthetic */ float f$2;
                    public final /* synthetic */ float f$3;

                    {
                        this.f$1 = r2;
                        this.f$2 = r3;
                        this.f$3 = r4;
                    }

                    public final void accept(Object obj) {
                        KeyguardMoveHelper.this.lambda$setTranslation$1$KeyguardMoveHelper(this.f$1, this.f$2, this.f$3, (View) obj);
                    }
                });
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
            leftBackgroundView.setVisibility(4);
            MiuiGxzwManager.getInstance().setCanShowGxzw(true);
            this.mKeyguardUpdateMonitor.requestFaceAuth();
            AnalyticsHelper.getInstance(this.mContext).trackPageStart("keyguard_view_main_lock_screen");
            LockScreenMagazineUtils.sendLockScreenMagazineEventBroadcast(this.mContext, "Wallpaper_Uncovered");
        } else if (this.mCurrentScreen == 1 && f4 == (-getScreenWidth())) {
            this.mCurrentScreen = 2;
            MiuiGxzwManager.getInstance().setCanShowGxzw(true);
            AnalyticsHelper.getInstance(this.mContext).trackPageEnd("keyguard_view_main_lock_screen", "enter_right_view");
        } else if (this.mCurrentScreen == 1 && f4 == getScreenWidth()) {
            this.mCurrentScreen = 0;
            MiuiGxzwManager.getInstance().setCanShowGxzw(true);
            this.mKeyguardUpdateMonitor.cancelFaceAuth();
            AnalyticsHelper.getInstance(this.mContext).trackPageEnd("keyguard_view_main_lock_screen", "enter_left_view");
            AnalyticsHelper.getInstance(this.mContext).recordKeyguardAction("action_enter_left_view");
            AnalyticsHelper.getInstance(this.mContext).recordNegativeStatus();
            if (!((LockScreenMagazineController) Dependency.get(cls2)).isSupportLockScreenMagazineLeft()) {
                LockScreenMagazineUtils.sendLockScreenMagazineEventBroadcast(this.mContext, "Wallpaper_Covered");
            }
        } else if (this.mCurrentScreen == 1 && f4 == 0.0f) {
            MiuiGxzwManager.getInstance().setCanShowGxzw(true);
        } else {
            MiuiGxzwManager.getInstance().setCanShowGxzw(false);
        }
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$setTranslation$1 */
    public /* synthetic */ void lambda$setTranslation$1$KeyguardMoveHelper(List list, float f, float f2, View view) {
        list.add(ObjectAnimator.ofFloat(view, View.TRANSLATION_X, new float[]{view.getTranslationX(), f}));
        if ((!(view instanceof LockScreenMagazinePreView) && !(view instanceof MiuiKeyguardBaseClock)) || !this.mCallback.isKeyguardWallpaperCarouselSwitchAnimating()) {
            list.add(ObjectAnimator.ofFloat(view, "alpha", new float[]{view.getAlpha(), 1.0f - f2}));
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

    public void updateResource(boolean z) {
        if (z) {
            initDimens();
            updateBottomIcons(this.mBottomAreaView);
        }
    }

    public void reset(boolean z) {
        reset(z, false);
    }

    private void reset(boolean z, boolean z2, boolean z3) {
        cancelAnimation();
        setTranslation(0.0f, true, z, z2, z3);
        this.mMotionCancelled = false;
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

    public KeyguardMoveLeftController getLeftMovementController() {
        return this.mLeftMoveController;
    }

    public void launchAffordance(boolean z, boolean z2) {
        if (!this.mSwipingInProgress) {
            KeyguardAffordanceView keyguardAffordanceView = z2 ? this.mLeftIcon : this.mRightIcon;
            KeyguardAffordanceView keyguardAffordanceView2 = z2 ? this.mRightIcon : this.mLeftIcon;
            if (z) {
                fling(0.0f, false, !z2);
                updateIcon(keyguardAffordanceView2, 0.0f, true, true);
                return;
            }
            this.mCallback.onAnimationToSideStarted(!z2, this.mTranslation, 0.0f);
            this.mTranslation = this.mCallback.getMaxTranslationDistance();
            updateIcon(keyguardAffordanceView2, 0.0f, false, true);
            keyguardAffordanceView.instantFinishAnimation();
            this.mFlingEndListener.onAnimationEnd((Animator) null);
            this.mAnimationEndRunnable.run();
        }
    }

    public void onStartedWakingUp() {
        Callback callback = this.mCallback;
        if (callback != null) {
            callback.onHorizontalMove(0.0f, true);
        }
        KeyguardMoveLeftController keyguardMoveLeftController = this.mLeftMoveController;
        if (keyguardMoveLeftController != null) {
            keyguardMoveLeftController.onStartedWakingUp();
        }
    }

    public void onFinishedGoingToSleep() {
        KeyguardMoveLeftController keyguardMoveLeftController = this.mLeftMoveController;
        if (keyguardMoveLeftController != null) {
            keyguardMoveLeftController.onFinishedGoingToSleep();
        }
    }
}
