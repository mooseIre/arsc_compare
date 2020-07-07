package com.android.keyguard;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import com.android.keyguard.KeyguardUpdateMonitor;
import com.android.keyguard.MiuiKeyguardFaceUnlockView;
import com.android.keyguard.faceunlock.FaceUnlockCallback;
import com.android.keyguard.faceunlock.FaceUnlockManager;
import com.android.keyguard.faceunlock.MiuiFaceUnlockUtils;
import com.android.keyguard.utils.ViewAnimationUtils;
import com.android.systemui.Constants;
import com.android.systemui.Dependency;
import com.android.systemui.HapticFeedBackImpl;
import com.android.systemui.plugins.R;

public class MiuiKeyguardFaceUnlockView extends LinearLayout {
    /* access modifiers changed from: private */
    public Handler mAnimationHandler;
    /* access modifiers changed from: private */
    public Context mContext;
    private final Runnable mDelayedHide;
    /* access modifiers changed from: private */
    public boolean mFaceUnlockAnimationRuning;
    private FaceUnlockCallback mFaceUnlockCallback;
    private View.OnClickListener mFaceUnlockClickListener;
    /* access modifiers changed from: private */
    public FaceUnlockManager mFaceUnlockManager;
    private boolean mIsKeyguardFaceUnlockView;
    private KeyguardUpdateMonitorCallback mKeyguardUpdateMonitorCallback;
    /* access modifiers changed from: private */
    public boolean mLightClock;
    /* access modifiers changed from: private */
    public boolean mLockScreenMagazinePreViewVisibility;
    /* access modifiers changed from: private */
    public KeyguardUpdateMonitor mUpdateMonitor;
    /* access modifiers changed from: private */
    public boolean mWaitWakeupAimation;
    private final KeyguardUpdateMonitor.WallpaperChangeCallback mWallpaperChangeCallback;

    public MiuiKeyguardFaceUnlockView(Context context) {
        this(context, (AttributeSet) null);
    }

    public MiuiKeyguardFaceUnlockView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.mAnimationHandler = new Handler();
        this.mLightClock = false;
        this.mFaceUnlockCallback = new FaceUnlockCallback() {
            public void onFaceAuthStart() {
                MiuiKeyguardFaceUnlockView.this.updateFaceUnlockView();
            }

            public void onFaceAuthHelp(int i) {
                if (MiuiKeyguardFaceUnlockView.this.shouldFaceUnlockViewExecuteAnimation()) {
                    MiuiKeyguardFaceUnlockView.this.startFaceUnlockAnimation();
                }
            }

            public void onFaceAuthenticated() {
                if (MiuiKeyguardFaceUnlockView.this.shouldFaceUnlockViewExecuteAnimation() && MiuiKeyguardFaceUnlockView.this.mFaceUnlockManager.isStayScreenWhenFaceUnlockSuccess() && !MiuiKeyguardFaceUnlockView.this.mUpdateMonitor.isBouncerShowing()) {
                    ((HapticFeedBackImpl) Dependency.get(HapticFeedBackImpl.class)).getHapticFeedbackUtil().performHapticFeedback("mesh_light", false);
                    MiuiKeyguardFaceUnlockView.this.startFaceUnlockSuccessAnimation();
                }
            }

            public void onFaceAuthFailed() {
                MiuiKeyguardFaceUnlockView.this.stopShakeHeadAnimation();
                MiuiKeyguardFaceUnlockView.this.updateFaceUnlockView();
            }

            public void onFaceAuthTimeOut(boolean z) {
                MiuiKeyguardFaceUnlockView.this.stopShakeHeadAnimation();
                MiuiKeyguardFaceUnlockView.this.updateFaceUnlockView();
            }

            public void onFaceAuthLocked() {
                MiuiKeyguardFaceUnlockView.this.stopShakeHeadAnimation();
                MiuiKeyguardFaceUnlockView.this.updateFaceUnlockView();
            }
        };
        this.mKeyguardUpdateMonitorCallback = new KeyguardUpdateMonitorCallback() {
            public void onKeyguardBouncerChanged(boolean z) {
                MiuiKeyguardFaceUnlockView.this.updateFaceUnlockView();
            }

            public void onStartedWakingUp() {
                super.onStartedWakingUp();
                if (MiuiKeyguardFaceUnlockView.this.getVisibility() == 0) {
                    boolean unused = MiuiKeyguardFaceUnlockView.this.mWaitWakeupAimation = false;
                    MiuiKeyguardFaceUnlockView.this.startAnimation(ViewAnimationUtils.generalWakeupScaleAimation());
                    return;
                }
                boolean unused2 = MiuiKeyguardFaceUnlockView.this.mWaitWakeupAimation = true;
                MiuiKeyguardFaceUnlockView.this.mAnimationHandler.postDelayed(new Runnable() {
                    public final void run() {
                        MiuiKeyguardFaceUnlockView.AnonymousClass2.this.lambda$onStartedWakingUp$0$MiuiKeyguardFaceUnlockView$2();
                    }
                }, 200);
            }

            public /* synthetic */ void lambda$onStartedWakingUp$0$MiuiKeyguardFaceUnlockView$2() {
                boolean unused = MiuiKeyguardFaceUnlockView.this.mWaitWakeupAimation = false;
            }

            public void onLockScreenMagazinePreViewVisibilityChanged(boolean z) {
                boolean unused = MiuiKeyguardFaceUnlockView.this.mLockScreenMagazinePreViewVisibility = z;
                MiuiKeyguardFaceUnlockView.this.updateFaceUnlockView();
                if (MiuiKeyguardFaceUnlockView.this.shouldFaceUnlockViewExecuteAnimation() && z) {
                    MiuiKeyguardFaceUnlockView.this.mFaceUnlockManager.stopFaceUnlock();
                }
            }

            public void onFinishedGoingToSleep(int i) {
                MiuiKeyguardFaceUnlockView.this.updateFaceUnlockView();
            }
        };
        this.mWallpaperChangeCallback = new KeyguardUpdateMonitor.WallpaperChangeCallback() {
            public void onWallpaperChange(boolean z) {
                if (z) {
                    MiuiKeyguardFaceUnlockView miuiKeyguardFaceUnlockView = MiuiKeyguardFaceUnlockView.this;
                    KeyguardUpdateMonitor unused = miuiKeyguardFaceUnlockView.mUpdateMonitor;
                    boolean unused2 = miuiKeyguardFaceUnlockView.mLightClock = KeyguardUpdateMonitor.isWallpaperColorLight(MiuiKeyguardFaceUnlockView.this.mContext);
                    MiuiKeyguardFaceUnlockView.this.updateFaceUnlockView();
                }
            }
        };
        this.mFaceUnlockClickListener = new View.OnClickListener() {
            public void onClick(View view) {
                if (!MiuiKeyguardFaceUnlockView.this.mFaceUnlockManager.isFaceUnlockStarted()) {
                    ObjectAnimator ofFloat = ObjectAnimator.ofFloat(MiuiKeyguardFaceUnlockView.this, "scaleX", new float[]{1.0f, 1.2f, 0.9f, 1.0f});
                    ObjectAnimator ofFloat2 = ObjectAnimator.ofFloat(MiuiKeyguardFaceUnlockView.this, "scaleY", new float[]{1.0f, 1.2f, 0.9f, 1.0f});
                    AnimatorSet animatorSet = new AnimatorSet();
                    animatorSet.setInterpolator(Ease$Sine.easeInOut);
                    animatorSet.setDuration(400);
                    animatorSet.playTogether(new Animator[]{ofFloat, ofFloat2});
                    animatorSet.addListener(new AnimatorListenerAdapter() {
                        public void onAnimationEnd(Animator animator) {
                            MiuiKeyguardFaceUnlockView.this.mFaceUnlockManager.startFaceUnlock(2);
                            MiuiKeyguardUtils.userActivity(MiuiKeyguardFaceUnlockView.this.mContext);
                        }
                    });
                    animatorSet.start();
                }
            }
        };
        this.mDelayedHide = new Runnable() {
            public void run() {
                boolean unused = MiuiKeyguardFaceUnlockView.this.mFaceUnlockAnimationRuning = false;
            }
        };
        this.mContext = context;
        this.mUpdateMonitor = KeyguardUpdateMonitor.getInstance(context);
        this.mUpdateMonitor.registerCallback(this.mKeyguardUpdateMonitorCallback);
        this.mUpdateMonitor.registerWallpaperChangeCallback(this.mWallpaperChangeCallback);
        this.mFaceUnlockManager = FaceUnlockManager.getInstance();
        this.mFaceUnlockManager.registerFaceUnlockCallback(this.mFaceUnlockCallback);
        this.mFaceUnlockManager.addFaceUnlockView(this);
    }

    /* access modifiers changed from: protected */
    public void onFinishInflate() {
        super.onFinishInflate();
        setOnClickListener(this.mFaceUnlockClickListener);
        updateFaceUnlockViewForNotch();
    }

    /* access modifiers changed from: protected */
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        this.mUpdateMonitor.removeCallback(this.mKeyguardUpdateMonitorCallback);
        this.mUpdateMonitor.unregisterWallpaperChangeCallback(this.mWallpaperChangeCallback);
        this.mFaceUnlockManager.removeFaceUnlockCallback(this.mFaceUnlockCallback);
        this.mFaceUnlockManager.removeFaceUnlockView(this);
    }

    private void updateFaceUnlockViewForNotch() {
        ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) getLayoutParams();
        marginLayoutParams.topMargin = this.mContext.getResources().getDimensionPixelSize(Constants.IS_NOTCH ? R.dimen.miui_face_unlock_view_notch_top : R.dimen.miui_face_unlock_view_top);
        setLayoutParams(marginLayoutParams);
    }

    public void setKeyguardFaceUnlockView(boolean z) {
        this.mIsKeyguardFaceUnlockView = z;
    }

    /* access modifiers changed from: private */
    public boolean shouldFaceUnlockViewExecuteAnimation() {
        return (!this.mUpdateMonitor.isBouncerShowing() && this.mIsKeyguardFaceUnlockView) || (this.mUpdateMonitor.isBouncerShowing() && !this.mIsKeyguardFaceUnlockView);
    }

    /* access modifiers changed from: private */
    public void startFaceUnlockAnimation() {
        if (!this.mFaceUnlockAnimationRuning) {
            this.mFaceUnlockAnimationRuning = true;
            AnimationDrawable animationDrawable = new AnimationDrawable();
            for (int i = 1; i <= 30; i++) {
                String str = (this.mUpdateMonitor.isBouncerShowing() || !this.mLightClock) ? "face_unlock_error" : "face_unlock_black_error";
                animationDrawable.addFrame(getResources().getDrawable(this.mContext.getResources().getIdentifier(str + i, "drawable", this.mContext.getPackageName())), 16);
            }
            setBackground(animationDrawable);
            animationDrawable.setOneShot(true);
            animationDrawable.start();
            this.mAnimationHandler.postDelayed(this.mDelayedHide, 1480);
        }
    }

    /* access modifiers changed from: private */
    public void stopShakeHeadAnimation() {
        this.mAnimationHandler.removeCallbacks(this.mDelayedHide);
        this.mFaceUnlockAnimationRuning = false;
    }

    /* access modifiers changed from: private */
    public void startFaceUnlockSuccessAnimation() {
        AnimationDrawable animationDrawable = new AnimationDrawable();
        for (int i = 1; i <= 20; i++) {
            String str = (this.mUpdateMonitor.isBouncerShowing() || !this.mLightClock) ? "face_unlock_success" : "face_unlock_black_success";
            animationDrawable.addFrame(getResources().getDrawable(this.mContext.getResources().getIdentifier(str + i, "drawable", this.mContext.getPackageName())), 16);
        }
        animationDrawable.setOneShot(true);
        setBackground(animationDrawable);
        animationDrawable.start();
    }

    private boolean shouldShowFaceUnlockImage() {
        boolean z = this.mFaceUnlockManager.shouldListenForFaceUnlock() && !this.mUpdateMonitor.mustPasswordUnlockDevice() && this.mUpdateMonitor.isKeyguardShowing() && !this.mFaceUnlockManager.isFaceLocked() && !this.mUpdateMonitor.isSimPinSecure();
        if (this.mUpdateMonitor.isBouncerShowing()) {
            if (!z || !MiuiFaceUnlockUtils.isSlideCoverOpened(this.mContext) || ((!this.mUpdateMonitor.isKeyguardOccluded() || MiuiKeyguardUtils.isTopActivityCameraApp(this.mContext)) && this.mUpdateMonitor.isKeyguardOccluded())) {
                return false;
            }
            return true;
        } else if (!z || this.mUpdateMonitor.isKeyguardOccluded() || MiuiFaceUnlockUtils.isSupportLiftingCamera(this.mContext) || (((!this.mFaceUnlockManager.isFaceUnlockStarted() || !MiuiFaceUnlockUtils.isSlideCoverOpened(this.mContext)) && !this.mUpdateMonitor.isFaceUnlock()) || this.mLockScreenMagazinePreViewVisibility)) {
            return false;
        } else {
            return true;
        }
    }

    public void setVisibility(int i) {
        if (i == 0 && getVisibility() != i && this.mWaitWakeupAimation) {
            startAnimation(ViewAnimationUtils.generalWakeupScaleAimation());
            this.mWaitWakeupAimation = false;
        }
        super.setVisibility(i);
    }

    public void updateFaceUnlockView() {
        if (MiuiFaceUnlockUtils.isSupportFaceUnlock(this.mContext)) {
            if (!shouldFaceUnlockViewExecuteAnimation() || this.mFaceUnlockManager.isDisableLockScreenFaceUnlockAnim() || !shouldShowFaceUnlockImage()) {
                setVisibility(4);
            } else {
                setVisibility(0);
            }
            if (this.mUpdateMonitor.isBouncerShowing() || !this.mLightClock) {
                setBackground(getResources().getDrawable(this.mUpdateMonitor.isFaceUnlock() ? R.drawable.face_unlock_success20 : R.drawable.face_unlock_error1));
            } else {
                setBackground(getResources().getDrawable(this.mUpdateMonitor.isFaceUnlock() ? R.drawable.face_unlock_black_success20 : R.drawable.face_unlock_black_error1));
            }
        }
    }
}
